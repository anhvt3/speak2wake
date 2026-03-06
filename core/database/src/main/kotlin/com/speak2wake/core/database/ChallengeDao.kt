package com.speak2wake.core.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChallengeDao {

    @Insert suspend fun insertSession(session: ChallengeSessionEntity): Long

    @Insert suspend fun insertAttempt(attempt: ChallengeAttemptEntity)

    @Update suspend fun updateSession(session: ChallengeSessionEntity)

    @Query("SELECT * FROM challenge_sessions ORDER BY startedAtEpochMs DESC")
    fun getAllSessions(): Flow<List<ChallengeSessionEntity>>

    @Query(
            "SELECT * FROM challenge_sessions WHERE alarmId = :alarmId ORDER BY startedAtEpochMs DESC"
    )
    fun getSessionsForAlarm(alarmId: Long): Flow<List<ChallengeSessionEntity>>

    @Query("SELECT * FROM challenge_attempts WHERE sessionId = :sessionId ORDER BY id ASC")
    fun getAttemptsForSession(sessionId: Long): Flow<List<ChallengeAttemptEntity>>

    @Query("SELECT COUNT(*) FROM challenge_sessions WHERE passed = 1")
    fun countPassedSessions(): Flow<Int>

    @Query("SELECT COUNT(*) FROM challenge_sessions") fun countTotalSessions(): Flow<Int>
}
