package com.speak2wake.core.model

import java.time.Instant

data class ChallengeSession(
    val id: Long = 0,
    val alarmId: Long,
    val wordId: Long,
    val wordGerman: String,
    val attempts: Int = 0,
    val passed: Boolean = false,
    val failsafeUsed: Boolean = false,
    val finalScore: Float = 0f,
    val startedAt: Instant = Instant.now(),
)

data class ChallengeAttempt(
    val id: Long = 0,
    val sessionId: Long,
    val transcript: String,
    val levenshteinScore: Float,
    val phoneticScore: Float,
    val confidenceScore: Float,
    val totalScore: Float,
    val passed: Boolean,
    val timeoutOccurred: Boolean = false,
)
