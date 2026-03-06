package com.speak2wake.core.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.speak2wake.core.model.ChallengeAttempt
import com.speak2wake.core.model.ChallengeSession

@Entity(tableName = "challenge_sessions")
data class ChallengeSessionEntity(
        @PrimaryKey(autoGenerate = true) val id: Long = 0,
        val alarmId: Long,
        val wordId: Long,
        val wordGerman: String,
        val attempts: Int = 0,
        val passed: Boolean = false,
        val failsafeUsed: Boolean = false,
        val finalScore: Float = 0f,
        val startedAtEpochMs: Long = System.currentTimeMillis(),
)

@Entity(
        tableName = "challenge_attempts",
        foreignKeys =
                [
                        ForeignKey(
                                entity = ChallengeSessionEntity::class,
                                parentColumns = ["id"],
                                childColumns = ["sessionId"],
                                onDelete = ForeignKey.CASCADE,
                        ),
                ],
        indices = [Index("sessionId")],
)
data class ChallengeAttemptEntity(
        @PrimaryKey(autoGenerate = true) val id: Long = 0,
        val sessionId: Long,
        val transcript: String,
        val levenshteinScore: Float,
        val phoneticScore: Float,
        val confidenceScore: Float,
        val totalScore: Float,
        val passed: Boolean,
        val timeoutOccurred: Boolean = false,
)

fun ChallengeSessionEntity.toModel() =
        ChallengeSession(
                id = id,
                alarmId = alarmId,
                wordId = wordId,
                wordGerman = wordGerman,
                attempts = attempts,
                passed = passed,
                failsafeUsed = failsafeUsed,
                finalScore = finalScore,
                startedAt = java.time.Instant.ofEpochMilli(startedAtEpochMs),
        )

fun ChallengeSession.toEntity() =
        ChallengeSessionEntity(
                id = id,
                alarmId = alarmId,
                wordId = wordId,
                wordGerman = wordGerman,
                attempts = attempts,
                passed = passed,
                failsafeUsed = failsafeUsed,
                finalScore = finalScore,
                startedAtEpochMs = startedAt.toEpochMilli(),
        )

fun ChallengeAttemptEntity.toModel() =
        ChallengeAttempt(
                id = id,
                sessionId = sessionId,
                transcript = transcript,
                levenshteinScore = levenshteinScore,
                phoneticScore = phoneticScore,
                confidenceScore = confidenceScore,
                totalScore = totalScore,
                passed = passed,
                timeoutOccurred = timeoutOccurred,
        )

fun ChallengeAttempt.toEntity() =
        ChallengeAttemptEntity(
                id = id,
                sessionId = sessionId,
                transcript = transcript,
                levenshteinScore = levenshteinScore,
                phoneticScore = phoneticScore,
                confidenceScore = confidenceScore,
                totalScore = totalScore,
                passed = passed,
                timeoutOccurred = timeoutOccurred,
        )
