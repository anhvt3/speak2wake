package com.speak2wake.core.data.repository

import com.speak2wake.core.database.ChallengeDao
import com.speak2wake.core.database.toEntity
import com.speak2wake.core.database.toModel
import com.speak2wake.core.model.ChallengeAttempt
import com.speak2wake.core.model.ChallengeSession
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class OfflineFirstChallengeHistoryRepository
@Inject
constructor(
        private val challengeDao: ChallengeDao,
) : ChallengeHistoryRepository {

    override suspend fun createSession(session: ChallengeSession): Long =
            challengeDao.insertSession(session.toEntity())

    override suspend fun updateSession(session: ChallengeSession) =
            challengeDao.updateSession(session.toEntity())

    override suspend fun recordAttempt(attempt: ChallengeAttempt) =
            challengeDao.insertAttempt(attempt.toEntity())

    override fun getAllSessions(): Flow<List<ChallengeSession>> =
            challengeDao.getAllSessions().map { list -> list.map { it.toModel() } }

    override fun getSessionsForAlarm(alarmId: Long): Flow<List<ChallengeSession>> =
            challengeDao.getSessionsForAlarm(alarmId).map { list -> list.map { it.toModel() } }

    override fun countPassedSessions(): Flow<Int> = challengeDao.countPassedSessions()

    override fun countTotalSessions(): Flow<Int> = challengeDao.countTotalSessions()
}
