package com.speak2wake.core.data.repository

import com.speak2wake.core.model.ChallengeAttempt
import com.speak2wake.core.model.ChallengeSession
import kotlinx.coroutines.flow.Flow

interface ChallengeHistoryRepository {
    suspend fun createSession(session: ChallengeSession): Long
    suspend fun updateSession(session: ChallengeSession)
    suspend fun recordAttempt(attempt: ChallengeAttempt)
    fun getAllSessions(): Flow<List<ChallengeSession>>
    fun getSessionsForAlarm(alarmId: Long): Flow<List<ChallengeSession>>
    fun countPassedSessions(): Flow<Int>
    fun countTotalSessions(): Flow<Int>
}
