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
import com.speak2wake.core.model.ChallengeLanguage
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
        private const val TIMEOUT_MS = 7_000L
        private const val TAG = "ChallengeVM"
        private const val MAX_WORD_RETRY = 10 // max retries to find unique word
    }

    private val alarmId = savedStateHandle.toRoute<ChallengeRoute>().alarmId
    private val _uiState = MutableStateFlow<ChallengeUiState>(ChallengeUiState.Loading)
    val uiState: StateFlow<ChallengeUiState> = _uiState.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var timeoutJob: Job? = null
    private var currentAttempt = 1
    private var currentWordIndex = 1
    private var totalWords = 1

    // Challenge language for this alarm (de or vi)
    private var challengeLang: String = "de"

    // Track used word IDs to avoid duplicates in multi-word challenge
    private val usedWordIds = mutableSetOf<Long>()

    // Persistent session tracking
    private var sessionId: Long = 0L

    // TTS for listening to word pronunciation
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private val ttsReadyDeferred = CompletableDeferred<Boolean>()

    // Whether speech recognizer is available on this device
    private val speechRecognizerAvailable: Boolean by lazy {
        SpeechRecognizer.isRecognitionAvailable(context)
    }

    init {
        // TTS init deferred until we know the language from loadWord()
        loadWord()
    }

    private fun initTts(locale: java.util.Locale) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(locale)
                ttsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED
                Log.d(TAG, "TTS initialized for ${locale.language}, ready=$ttsReady")
            } else {
                Log.w(TAG, "TTS init failed with status=$status")
                ttsReady = false
            }
            ttsReadyDeferred.complete(ttsReady)
        }
    }

    fun speakWord() {
        val state = _uiState.value as? ChallengeUiState.Active ?: return
        if (!ttsReady) {
            Log.w(TAG, "TTS not ready, skipping pronunciation — falling back to startListening")
            // Graceful fallback: skip TTS and go straight to listening
            viewModelScope.launch {
                delay(300)
                startListening()
            }
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
            override fun onError(utteranceId: String?) {
                Log.w(TAG, "TTS utterance error, falling back to startListening")
                viewModelScope.launch {
                    delay(300)
                    startListening()
                }
            }
        })

        val params = android.os.Bundle().apply {
            putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_ALARM)
        }
        tts?.speak(state.word.german, TextToSpeech.QUEUE_FLUSH, params, "word_tts")
    }

    /**
     * Get a unique word for challenge, avoiding duplicates from [usedWordIds].
     * Returns null only if no words are available at all.
     */
    private suspend fun getUniqueWord(level: com.speak2wake.core.model.VocabularyLevel): com.speak2wake.core.model.VocabularyWord? {
        repeat(MAX_WORD_RETRY) {
            val word = vocabularyRepository.getWordForChallenge(level, challengeLang) ?: return null
            if (word.id !in usedWordIds) {
                return word
            }
        }
        // Fallback: if we can't find a unique word after retries, accept a duplicate
        return vocabularyRepository.getWordForChallenge(level, challengeLang)
    }

    private fun loadWord() {
        viewModelScope.launch {
            val alarm = alarmRepository.getAlarmById(alarmId)
            if (alarm == null) {
                _uiState.value = ChallengeUiState.Error("Alarm not found")
                return@launch
            }

            // Set challenge language from alarm
            challengeLang = alarm.challengeLanguage.name.lowercase()
            val ttsLocale = when (alarm.challengeLanguage) {
                ChallengeLanguage.DE -> java.util.Locale.GERMAN
                ChallengeLanguage.VI -> java.util.Locale("vi")
            }
            initTts(ttsLocale)

            totalWords = alarm.challengeWordCount.coerceIn(1, 10)
            val word = getUniqueWord(alarm.vocabularyLevel)
            if (word == null) {
                _uiState.value = ChallengeUiState.Error("No vocabulary available")
                return@launch
            }
            usedWordIds.add(word.id)
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
                            challengeLanguage = challengeLang,
                            alwaysPronounce = alarm.alwaysPronounce,
                            currentWordIndex = currentWordIndex,
                            totalWords = totalWords,
                    )
            // Pause alarm sound so mic can hear user speech
            pauseAlarmSound()

            if (alarm.alwaysPronounce) {
                // Wait for TTS to be ready before auto-pronouncing
                withTimeoutOrNull(3000) { ttsReadyDeferred.await() }
                delay(300)
                speakWord() // onDone listener will call startListening()
            } else {
                delay(500)
                startListening()
            }
        }
    }

    private fun loadNextWord() {
        viewModelScope.launch {
            val prevState = _uiState.value as? ChallengeUiState.Active ?: return@launch
            currentAttempt = 1
            currentWordIndex++
            timeoutJob?.cancel()
            speechRecognizer?.destroy()
            speechRecognizer = null

            val alarm = alarmRepository.getAlarmById(alarmId) ?: return@launch
            val word = getUniqueWord(alarm.vocabularyLevel)
            if (word == null) {
                _uiState.value = ChallengeUiState.Error("No vocabulary available")
                return@launch
            }
            usedWordIds.add(word.id)
            vocabularyRepository.recordShown(word.id)

            sessionId =
                    challengeHistoryRepository.createSession(
                            ChallengeSession(
                                    alarmId = alarmId,
                                    wordId = word.id,
                                    wordGerman = word.german,
                            )
                    )

            _uiState.value =
                    ChallengeUiState.Active(
                            word = word,
                            attempt = 1,
                            phase = ChallengePhase.READY,
                            languageCode = prevState.languageCode,
                            challengeLanguage = prevState.challengeLanguage,
                            alwaysPronounce = prevState.alwaysPronounce,
                            currentWordIndex = currentWordIndex,
                            totalWords = totalWords,
                    )
            // Pause alarm sound so mic can hear user speech
            pauseAlarmSound()

            if (prevState.alwaysPronounce) {
                delay(300)
                speakWord()
            } else {
                delay(500)
                startListening()
            }
        }
    }

    fun onMicPermissionResult(granted: Boolean) {
        if (granted) {
            val state = _uiState.value as? ChallengeUiState.Active ?: return
            _uiState.value = state.copy(micPermissionNeeded = false)
            startListening()
        }
    }

    fun startListening() {
        val state = _uiState.value as? ChallengeUiState.Active ?: return
        if (!checkMicPermission()) {
            _uiState.value = state.copy(micPermissionNeeded = true)
            return
        }

        // Check if speech recognizer is available on this device
        if (!speechRecognizerAvailable) {
            Log.w(TAG, "Speech recognizer not available on this device, showing failsafe immediately")
            _uiState.value = state.copy(
                    phase = ChallengePhase.READY,
                    showFailsafe = true,
            )
            return
        }

        // Always pause alarm sound so mic can hear user speech
        pauseAlarmSound()

        _uiState.value = state.copy(phase = ChallengePhase.LISTENING)

        speechRecognizer?.destroy()
        speechRecognizer =
                SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(buildListener(state.word.german))
                }

        val speechLang = when (challengeLang) {
            "vi" -> "vi-VN"
            else -> "de-DE"
        }

        val intent =
                Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, speechLang)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, speechLang)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                    putExtra(
                            RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
                            1500L
                    )
                    putExtra(
                            RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS,
                            1000L
                    )
                    putExtra(
                            RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                            1000L
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
                    val transcripts =
                            results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                                    ?: arrayListOf()
                    val confidences =
                            results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)

                    // Pick the best matching transcript from all results
                    var bestTranscript = transcripts.firstOrNull() ?: ""
                    var bestConfidence = confidences?.firstOrNull() ?: 0.7f
                    var bestScore = ScoringEngine.score(expectedWord, bestTranscript, bestConfidence).totalScore

                    for (i in 1 until transcripts.size) {
                        val t = transcripts[i]
                        val c = confidences?.getOrNull(i) ?: 0.5f
                        val s = ScoringEngine.score(expectedWord, t, c).totalScore
                        if (s > bestScore) {
                            bestTranscript = t
                            bestConfidence = c
                            bestScore = s
                        }
                    }
                    Log.d(TAG, "Speech results: ${transcripts.size} candidates, best='$bestTranscript'")
                    handleTranscript(expectedWord, bestTranscript, bestConfidence)
                }

                override fun onError(error: Int) {
                    timeoutJob?.cancel()
                    val errorName = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "ERROR_AUDIO"
                        SpeechRecognizer.ERROR_CLIENT -> "ERROR_CLIENT"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "ERROR_PERMISSIONS"
                        SpeechRecognizer.ERROR_NETWORK -> "ERROR_NETWORK"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "ERROR_NETWORK_TIMEOUT"
                        SpeechRecognizer.ERROR_NO_MATCH -> "ERROR_NO_MATCH"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "ERROR_BUSY"
                        SpeechRecognizer.ERROR_SERVER -> "ERROR_SERVER"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "ERROR_SPEECH_TIMEOUT"
                        else -> "ERROR_UNKNOWN($error)"
                    }
                    Log.w(TAG, "Speech error: $error ($errorName) lang=$challengeLang")
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

            if (currentWordIndex < totalWords) {
                // More words to go — load next word
                loadNextWord()
            } else {
                // All words passed — dismiss alarm
                dismissAlarm()
                alarmRepository.rescheduleAfterFire(alarmId)
                _uiState.value = ChallengeUiState.Passed
            }
        }
    }

    private fun handleFail() {
        val state = _uiState.value as? ChallengeUiState.Active ?: return

        currentAttempt++
        val showFailsafe = currentAttempt > MAX_ATTEMPTS

        if (showFailsafe && !state.showFailsafe) {
            // First time reaching failsafe — resume alarm sound
            resumeAlarmSound()
        }

        _uiState.value =
                state.copy(
                        attempt = currentAttempt,
                        phase = ChallengePhase.READY,
                        showFailsafe = showFailsafe,
                )

        if (!showFailsafe) {
            // Auto-retry before failsafe threshold
            viewModelScope.launch {
                delay(500)
                if (state.alwaysPronounce) {
                    speakWord()
                } else {
                    startListening()
                }
            }
        }
        // After failsafe shown: user can tap mic manually or type
    }

    /**
     * Normalize German text for comparison.
     * Replaces umlauts with ASCII equivalents so user can type either form.
     * e.g. "Tschüss" -> "tschuess", "ue" -> "ue" (already normalized)
     */
    private fun normalizeGerman(text: String): String {
        return text.lowercase().trim()
                .replace("ä", "ae")
                .replace("ö", "oe")
                .replace("ü", "ue")
                .replace("ß", "ss")
    }

    /**
     * Normalize Vietnamese text for comparison.
     * Strips diacritics so "xin chao" matches "xin chào".
     */
    private fun normalizeVietnamese(text: String): String {
        val normalized = java.text.Normalizer.normalize(text.lowercase().trim(), java.text.Normalizer.Form.NFD)
        return normalized.replace(Regex("[\\p{InCombiningDiacriticalMarks}]+"), "")
                .replace("đ", "d").replace("Đ", "D")
    }

    fun submitFailsafe(typedWord: String) {
        val state = _uiState.value as? ChallengeUiState.Active ?: return
        val isMatch = if (challengeLang == "vi") {
            normalizeVietnamese(typedWord) == normalizeVietnamese(state.word.german)
        } else {
            val typedNormalized = normalizeGerman(typedWord)
            val expectedNormalized = normalizeGerman(state.word.german)
            typedWord.lowercase().trim() == state.word.german.lowercase().trim()
                    || typedNormalized == expectedNormalized
        }

        if (isMatch) {
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

                if (currentWordIndex < totalWords) {
                    loadNextWord()
                } else {
                    dismissAlarm()
                    alarmRepository.rescheduleAfterFire(alarmId)
                    _uiState.value = ChallengeUiState.Passed
                }
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
        // Keep currentWordIndex — only changing the current word, not resetting progress
        val prevState = _uiState.value as? ChallengeUiState.Active
        _uiState.value = ChallengeUiState.Loading
        viewModelScope.launch {
            val alarm = alarmRepository.getAlarmById(alarmId) ?: return@launch
            val word = getUniqueWord(alarm.vocabularyLevel)
            if (word == null) {
                _uiState.value = ChallengeUiState.Error("No vocabulary available")
                return@launch
            }
            usedWordIds.add(word.id)
            vocabularyRepository.recordShown(word.id)

            sessionId =
                    challengeHistoryRepository.createSession(
                            ChallengeSession(
                                    alarmId = alarmId,
                                    wordId = word.id,
                                    wordGerman = word.german,
                            )
                    )

            val languageCode = prevState?.languageCode
                ?: context.settingsDataStore.data.first()[KEY_LANGUAGE]
                ?: if (java.util.Locale.getDefault().language == "vi") "vi" else "en"

            _uiState.value =
                    ChallengeUiState.Active(
                            word = word,
                            attempt = 1,
                            phase = ChallengePhase.READY,
                            languageCode = languageCode,
                            challengeLanguage = prevState?.challengeLanguage ?: challengeLang,
                            alwaysPronounce = alarm.alwaysPronounce,
                            currentWordIndex = currentWordIndex,
                            totalWords = totalWords,
                    )
            if (alarm.alwaysPronounce) {
                delay(300)
                speakWord()
            } else {
                delay(500)
                startListening()
            }
        }
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
