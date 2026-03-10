package com.speak2wake.feature.challenge.impl

import com.speak2wake.core.model.VocabularyWord

sealed interface ChallengeUiState {
    data object Loading : ChallengeUiState
    data class Error(val message: String) : ChallengeUiState
    data class Active(
        val word: VocabularyWord,
        val attempt: Int,         // current attempt (1-based)
        val maxAttempts: Int = 5,
        val phase: ChallengePhase = ChallengePhase.READY,
        val lastScore: Float? = null,
        val lastPassed: Boolean? = null,
        val lastTranscript: String? = null,
        val showFailsafe: Boolean = false,
        val languageCode: String = "en",
        val challengeLanguage: String = "de",
        val alwaysPronounce: Boolean = false,
        val currentWordIndex: Int = 1,
        val totalWords: Int = 1,
        val micPermissionNeeded: Boolean = false,
    ) : ChallengeUiState
    data object Passed : ChallengeUiState
    data object AlarmDismissed : ChallengeUiState
}

enum class ChallengePhase {
    READY,      // waiting to start
    LISTENING,  // mic active, recording
    SCORING,    // computing result
    FEEDBACK,   // showing pass/fail result
}
