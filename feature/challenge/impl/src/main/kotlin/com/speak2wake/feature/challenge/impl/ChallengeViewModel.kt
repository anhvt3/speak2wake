package com.speak2wake.feature.challenge.impl

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.speak2wake.core.alarm.AlarmForegroundService
import com.speak2wake.core.common.ScoringEngine
import com.speak2wake.core.data.KEY_LANGUAGE
import com.speak2wake.core.data.repository.AlarmRepository
import com.speak2wake.core.data.repository.ChallengeHistoryRepository
import com.speak2wake.core.data.repository.VocabularyRepository
import com.speak2wake.core.data.settingsDataStore
import com.speak2wake.core.model.ChallengeAttempt
import com.speak2wake.core.model.ChallengeSession
import com.speak2wake.feature.challenge.api.ChallengeRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@HiltViewModel
class ChallengeViewModel
@Inject
constructor(
        savedStateHandle: SavedStateHandle,
        private val alarmRepository: AlarmRepository,
        private val vocabularyRepository: VocabularyRepository,
        private val challengeHistoryRepository: ChallengeHistoryRepository,
        @ApplicationContext private val context: Context,
) : ViewModel() {

    companion object {
        private const val MAX_ATTEMPTS = 5
        private const val TIMEOUT_MS = 10_000L
        private const val TAG = "ChallengeVM"
    }

    private val alarmId = savedStateHandle.toRoute<ChallengeRoute>().alarmId
    private val _uiState = MutableStateFlow<ChallengeUiState>(ChallengeUiState.Loading)
    val uiState: StateFlow<ChallengeUiState> = _uiState.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var timeoutJob: Job? = null
    private var currentAttempt = 1

    // Persistent session tracking
    private var sessionId: Long = 0L

    // TTS for listening to word pronunciation
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(java.util.Locale.GERMAN)
                ttsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED
                Log.d(TAG, "TTS initialized, ready=$ttsReady")
            }
        }
        loadWord()
    }

    fun speakWord() {
        val state = _uiState.value as? ChallengeUiState.Active ?: return
        if (!ttsReady) {
            Log.w(TAG, "TTS not ready, cannot speak word")
            return
        }

        // Stop mic to avoid capturing TTS audio as user speech
        timeoutJob?.cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null
        _uiState.value = state.copy(phase = ChallengePhase.READY)

        tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                // TTS finished, resume listening after brief pause
                viewModelScope.launch {
                    delay(500)
                    startListening()
                }
            }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {}
        })

        val params = android.os.Bundle().apply {
            putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_ALARM)
        }
        tts?.speak(state.word.german, TextToSpeech.QUEUE_FLUSH, params, "word_tts")
    }

    private fun loadWord() {
        viewModelScope.launch {
            val alarm = alarmRepository.getAlarmById(alarmId)
            if (alarm == null) {
                _uiState.value = ChallengeUiState.Error("Alarm not found")
                return@launch
            }
            val word = vocabularyRepository.getWordForChallenge(alarm.vocabularyLevel)
            if (word == null) {
                _uiState.value = ChallengeUiState.Error("No vocabulary available")
                return@launch
            }
            vocabularyRepository.recordShown(word.id)

            // Create persistent session
            sessionId =
                    challengeHistoryRepository.createSession(
                            ChallengeSession(
                                    alarmId = alarmId,
                                    wordId = word.id,
                                    wordGerman = word.german,
                            )
                    )

            val languageCode = context.settingsDataStore.data.first()[KEY_LANGUAGE]
                ?: if (java.util.Locale.getDefault().language == "vi") "vi" else "en"

            _uiState.value =
                    ChallengeUiState.Active(
                            word = word,
                            attempt = 1,
                            phase = ChallengePhase.READY,
                            languageCode = languageCode,
                            alwaysPronounce = alarm.alwaysPronounce,
                    )
            delay(500)
            if (alarm.alwaysPronounce) {
                speakWord() // onDone listener will call startListening()
            } else {
                startListening()
            }
        }
    }

    fun startListening() {
        val state = _uiState.value as? ChallengeUiState.Active ?: return
        if (!checkMicPermission()) return

        _uiState.value = state.copy(phase = ChallengePhase.LISTENING)

        speechRecognizer?.destroy()
        speechRecognizer =
                SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(buildListener(state.word.german))
                }

        val intent =
                Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "de-DE")
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                    putExtra(
                            RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
                            1500L
                    )
                }
        speechRecognizer?.startListening(intent)

        // 10s timeout
        timeoutJob?.cancel()
        timeoutJob =
                viewModelScope.launch {
                    delay(TIMEOUT_MS)
                    Log.d(TAG, "Speech timeout")
                    speechRecognizer?.stopListening()
                    handleTimeout()
                }
    }

    private fun buildListener(expectedWord: String) =
            object : RecognitionListener {
                override fun onReadyForSpeech(params: android.os.Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    timeoutJob?.cancel()
                }
                override fun onPartialResults(partialResults: android.os.Bundle?) {}
                override fun onEvent(eventType: Int, params: android.os.Bundle?) {}

                override fun onResults(results: android.os.Bundle?) {
                    timeoutJob?.cancel()
                    val transcript =
                            results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                                    ?.firstOrNull()
                                    ?: ""
                    val confidence =
                            results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                                    ?.firstOrNull()
                                    ?: 0.7f
                    handleTranscript(expectedWord, transcript, confidence)
                }

                override fun onError(error: Int) {
                    timeoutJob?.cancel()
                    Log.w(TAG, "Speech error: $error")
                    handleTimeout()
                }
            }

    private fun handleTranscript(expected: String, transcript: String, confidence: Float) {
        val result = ScoringEngine.score(expected, transcript, confidence)
        Log.d(
                TAG,
                "Score: ${result.totalScore} (threshold ${result.threshold}), transcript: '$transcript'"
        )

        viewModelScope.launch {
            val state = _uiState.value as? ChallengeUiState.Active ?: return@launch

            // Record attempt to DB
            challengeHistoryRepository.recordAttempt(
                    ChallengeAttempt(
                            sessionId = sessionId,
                            transcript = transcript,
                            levenshteinScore = result.levenshteinScore,
                            phoneticScore = result.phoneticScore,
                            confidenceScore = result.confidenceScore,
                            totalScore = result.totalScore,
                            passed = result.passed,
                    )
            )

            _uiState.value =
                    state.copy(
                            phase = ChallengePhase.FEEDBACK,
                            lastScore = result.totalScore,
                            lastPassed = result.passed,
                            lastTranscript = transcript,
                    )

            delay(1500) // show feedback briefly

            if (result.passed) {
                handlePass()
            } else {
                handleFail()
            }
        }
    }

    private fun handleTimeout() {
        viewModelScope.launch {
            val state = _uiState.value as? ChallengeUiState.Active ?: return@launch

            // Record timeout attempt
            challengeHistoryRepository.recordAttempt(
                    ChallengeAttempt(
                            sessionId = sessionId,
                            transcript = "",
                            levenshteinScore = 0f,
                            phoneticScore = 0f,
                            confidenceScore = 0f,
                            totalScore = 0f,
                            passed = false,
                            timeoutOccurred = true,
                    )
            )

            _uiState.value =
                    state.copy(
                            phase = ChallengePhase.FEEDBACK,
                            lastScore = 0f,
                            lastTranscript = null,
                    )
            delay(1000)
            handleFail()
        }
    }

    private fun handlePass() {
        viewModelScope.launch {
            val state = _uiState.value as? ChallengeUiState.Active ?: return@launch
            vocabularyRepository.recordPassed(state.word.id)

            // Update session as passed
            challengeHistoryRepository.updateSession(
                    ChallengeSession(
                            id = sessionId,
                            alarmId = alarmId,
                            wordId = state.word.id,
                            wordGerman = state.word.german,
                            attempts = currentAttempt,
                            passed = true,
                            finalScore = state.lastScore ?: 0f,
                    )
            )

            dismissAlarm()
            alarmRepository.rescheduleAfterFire(alarmId)
            _uiState.value = ChallengeUiState.Passed
        }
    }

    private fun handleFail() {
        val state = _uiState.value as? ChallengeUiState.Active ?: return

        if (currentAttempt >= MAX_ATTEMPTS) {
            // Max attempts reached — show failsafe, resume alarm sound
            resumeAlarmSound()
            _uiState.value =
                    state.copy(
                            attempt = MAX_ATTEMPTS,
                            phase = ChallengePhase.READY,
                            showFailsafe = true,
                    )
        } else {
            currentAttempt++
            _uiState.value =
                    state.copy(
                            attempt = currentAttempt,
                            phase = ChallengePhase.READY,
                    )
            viewModelScope.launch {
                delay(500)
                if (state.alwaysPronounce) {
                    speakWord()
                } else {
                    startListening()
                }
            }
        }
    }

    fun submitFailsafe(typedWord: String) {
        val state = _uiState.value as? ChallengeUiState.Active ?: return
        if (typedWord.lowercase().trim() == state.word.german.lowercase().trim()) {
            viewModelScope.launch {
                // Update session as passed via failsafe
                challengeHistoryRepository.updateSession(
                        ChallengeSession(
                                id = sessionId,
                                alarmId = alarmId,
                                wordId = state.word.id,
                                wordGerman = state.word.german,
                                attempts = currentAttempt,
                                passed = true,
                                failsafeUsed = true,
                                finalScore = 0f,
                        )
                )

                dismissAlarm()
                alarmRepository.rescheduleAfterFire(alarmId)
                _uiState.value = ChallengeUiState.Passed
            }
        }
    }

    private fun dismissAlarm() {
        try {
            val intent =
                    Intent(context, AlarmForegroundService::class.java).apply {
                        action = AlarmForegroundService.ACTION_STOP
                    }
            context.startService(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to dismiss alarm service", e)
        }
    }

    private fun resumeAlarmSound() {
        try {
            val intent = Intent(context, AlarmForegroundService::class.java).apply {
                action = AlarmForegroundService.ACTION_RESUME
            }
            context.startService(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to resume alarm sound", e)
        }
    }

    private fun pauseAlarmSound() {
        try {
            val intent = Intent(context, AlarmForegroundService::class.java).apply {
                action = AlarmForegroundService.ACTION_PAUSE
            }
            context.startService(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pause alarm sound", e)
        }
    }

    fun changeWord() {
        pauseAlarmSound()
        currentAttempt = 1
        timeoutJob?.cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null
        _uiState.value = ChallengeUiState.Loading
        loadWord()
    }

    private fun checkMicPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
    }

    override fun onCleared() {
        super.onCleared()
        timeoutJob?.cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
