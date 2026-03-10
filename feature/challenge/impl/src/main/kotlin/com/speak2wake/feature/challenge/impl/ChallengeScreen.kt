package com.speak2wake.feature.challenge.impl

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.speak2wake.core.designsystem.strings.LocalStrings
import com.speak2wake.core.designsystem.theme.*
import com.speak2wake.core.model.VocabularyWord

@Composable
internal fun ChallengeRoute(
    onDismissed: () -> Unit,
    viewModel: ChallengeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> viewModel.onMicPermissionResult(granted) }

    // Auto-request mic permission when needed
    LaunchedEffect(uiState) {
        val active = uiState as? ChallengeUiState.Active
        if (active?.micPermissionNeeded == true) {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
        if (uiState is ChallengeUiState.Passed || uiState is ChallengeUiState.AlarmDismissed) {
            onDismissed()
        }
    }

    ChallengeScreen(
        uiState = uiState,
        onRetry = viewModel::startListening,
        onFailsafeSubmit = viewModel::submitFailsafe,
        onListen = viewModel::speakWord,
        onChangeWord = viewModel::changeWord,
    )
}

@Composable
internal fun ChallengeScreen(
    uiState: ChallengeUiState,
    onRetry: () -> Unit,
    onFailsafeSubmit: (String) -> Unit,
    onListen: () -> Unit = {},
    onChangeWord: () -> Unit = {},
) {
    // Block back button — user must complete challenge to dismiss alarm
    BackHandler { /* intentionally empty — prevent challenge bypass */ }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BackgroundMid, BackgroundDeep),
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        when (uiState) {
            ChallengeUiState.Loading -> CircularProgressIndicator(color = OrangeAccent)
            is ChallengeUiState.Active -> ActiveChallenge(
                state = uiState,
                onRetry = onRetry,
                onFailsafeSubmit = onFailsafeSubmit,
                onListen = onListen,
                onChangeWord = onChangeWord,
            )
            is ChallengeUiState.Error -> ErrorState(uiState.message)
            ChallengeUiState.Passed, ChallengeUiState.AlarmDismissed -> PassedState()
        }
    }
}

@Composable
private fun ActiveChallenge(
    state: ChallengeUiState.Active,
    onRetry: () -> Unit,
    onFailsafeSubmit: (String) -> Unit,
    onListen: () -> Unit,
    onChangeWord: () -> Unit,
) {
    val s = LocalStrings.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Word progress (only shown when multiple words)
        if (state.totalWords > 1) {
            Text(
                text = s.wordProgressFormat.format(state.currentWordIndex, state.totalWords),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OrangeAccent,
            )
        }

        // Attempt counter
        Text(
            text = s.attemptFormat.format(state.attempt, state.maxAttempts),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )

        // Word display
        WordDisplay(word = state.word, languageCode = state.languageCode, challengeLanguage = state.challengeLanguage)

        // Listen & Change word buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = onListen,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GlassSurface,
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(
                    Icons.Default.VolumeUp,
                    contentDescription = s.listen,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(s.listen, fontWeight = FontWeight.Bold)
            }

            TextButton(onClick = onChangeWord) {
                Text(s.changeWord, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        // Mic button
        MicButton(phase = state.phase, onRetry = onRetry)

        // Feedback
        state.lastScore?.let { score ->
            ScoreFeedback(
                score = score,
                passed = state.lastPassed ?: false,
                transcript = state.lastTranscript,
            )
        }

        // Failsafe
        if (state.showFailsafe) {
            FailsafeInput(
                expectedWord = state.word.german,
                challengeLanguage = state.challengeLanguage,
                onSubmit = onFailsafeSubmit,
            )
        }
    }
}

@Composable
private fun WordDisplay(word: VocabularyWord, languageCode: String = "en", challengeLanguage: String = "de") {
    // For Vietnamese challenge: german field = Vietnamese word, show English as translation
    // For German challenge: german field = German word, show Vietnamese/English as translations
    val isViChallenge = challengeLanguage == "vi"

    val primaryTranslation: String
    val secondaryTranslation: String

    if (isViChallenge) {
        // VI challenge: gold text = Vietnamese word, translation = English
        primaryTranslation = word.english
        secondaryTranslation = ""
    } else {
        // DE challenge: gold text = German word, translations based on UI language
        primaryTranslation = if (languageCode == "vi" && word.vietnamese.isNotBlank()) {
            word.vietnamese
        } else {
            word.english
        }
        secondaryTranslation = if (languageCode == "vi") {
            word.english
        } else {
            word.vietnamese
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = word.german,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = GoldAccent,
            textAlign = TextAlign.Center,
        )
        if (word.phonetic.isNotBlank()) {
            Text(
                text = word.phonetic,
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = primaryTranslation,
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary,
        )
        if (secondaryTranslation.isNotBlank()) {
            Text(
                text = secondaryTranslation,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
    }
}

@Composable
private fun MicButton(phase: ChallengePhase, onRetry: () -> Unit) {
    val s = LocalStrings.current
    val isListening = phase == ChallengePhase.LISTENING

    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = if (isListening) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            tween(600, easing = EaseInOut), RepeatMode.Reverse,
        ),
        label = "mic_scale",
    )

    Box(contentAlignment = Alignment.Center) {
        // Pulse rings
        if (isListening) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(pulseScale)
                    .border(2.dp, OrangeAccent.copy(alpha = 0.3f), CircleShape),
            )
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(pulseScale)
                    .border(2.dp, OrangeAccent.copy(alpha = 0.5f), CircleShape),
            )
        }

        Button(
            onClick = if (!isListening) onRetry else ({}),
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isListening) OrangeAccent else GlassSurface,
                contentColor = if (isListening) BackgroundDeep else Color.White,
            ),
            contentPadding = PaddingValues(0.dp),
            enabled = phase != ChallengePhase.SCORING,
        ) {
            Icon(
                Icons.Default.Mic,
                contentDescription = s.tapMicToSpeak,
                modifier = Modifier.size(36.dp),
            )
        }
    }

    Text(
        text = when (phase) {
            ChallengePhase.READY -> s.tapMicToSpeak
            ChallengePhase.LISTENING -> s.listeningSpeak
            ChallengePhase.SCORING -> s.scoring
            ChallengePhase.FEEDBACK -> ""
        },
        style = MaterialTheme.typography.bodyMedium,
        color = TextSecondary,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun ScoreFeedback(score: Float, passed: Boolean, transcript: String?) {
    val color = if (passed) GreenSuccess else RedFail
    val emoji = if (passed) "✅" else "❌"
    val pct = (score * 100).toInt()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(12.dp),
    ) {
        Text("$emoji $pct%", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = color)
        if (!transcript.isNullOrBlank()) {
            Text(
                text = "\"$transcript\"",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun FailsafeInput(expectedWord: String, challengeLanguage: String = "de", onSubmit: (String) -> Unit) {
    val s = LocalStrings.current
    var text by remember { mutableStateOf("") }
    val placeholder = if (challengeLanguage == "vi") s.typeInVietnamese else s.typeInGerman

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .background(GlassSurface, RoundedCornerShape(12.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
            .padding(16.dp),
    ) {
        Text(
            s.cantSpeakTypeFormat.format(expectedWord),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text(placeholder, color = TextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrangeAccent,
                unfocusedBorderColor = GlassBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
            ),
            singleLine = true,
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { onSubmit(text) },
            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent, contentColor = BackgroundDeep),
            modifier = Modifier.fillMaxWidth(),
            enabled = text.isNotBlank(),
        ) {
            Text(s.submit, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ErrorState(message: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("⚠️", fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text(message, fontSize = 18.sp, color = TextSecondary, textAlign = TextAlign.Center)
    }
}

@Composable
private fun PassedState() {
    val s = LocalStrings.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("🎉", fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text(s.alarmDismissedMsg, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = GreenSuccess)
        Text(s.goodMorning, style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
    }
}
