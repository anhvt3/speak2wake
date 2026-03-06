package com.speak2wake.core.data.repository

import com.speak2wake.core.alarm.AlarmScheduler
import com.speak2wake.core.database.AlarmDao
import com.speak2wake.core.database.toEntity
import com.speak2wake.core.database.toModel
import com.speak2wake.core.model.Alarm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class OfflineFirstAlarmRepository @Inject constructor(
    private val alarmDao: AlarmDao,
    private val alarmScheduler: AlarmScheduler,
) : AlarmRepository {

    override fun getAlarms(): Flow<List<Alarm>> =
        alarmDao.getAlarms().map { entities -> entities.map { it.toModel() } }

    override suspend fun getAlarmById(id: Long): Alarm? =
        alarmDao.getAlarmById(id)?.toModel()

    override suspend fun createAlarm(alarm: Alarm): Long {
        val id = alarmDao.upsertAlarm(alarm.toEntity())
        val savedAlarm = alarm.copy(id = id)
        if (savedAlarm.isEnabled) alarmScheduler.schedule(savedAlarm)
        return id
    }

    override suspend fun updateAlarm(alarm: Alarm) {
        alarmScheduler.cancel(alarm.id)
        alarmDao.upsertAlarm(alarm.toEntity())
        if (alarm.isEnabled) alarmScheduler.schedule(alarm)
    }

    override suspend fun deleteAlarm(id: Long) {
        alarmScheduler.cancel(id)
        alarmDao.deleteAlarm(id)
    }

    override suspend fun setAlarmEnabled(id: Long, enabled: Boolean) {
        alarmDao.setEnabled(id, enabled)
        val alarm = alarmDao.getAlarmById(id)?.toModel() ?: return
        if (enabled) alarmScheduler.schedule(alarm) else alarmScheduler.cancel(id)
    }

    override suspend fun rescheduleAfterFire(alarmId: Long) {
        val alarm = alarmDao.getAlarmById(alarmId)?.toModel() ?: return
        if (alarm.repeatDays.isNotEmpty()) {
            // Repeating alarm — schedule next occurrence
            alarmScheduler.schedule(alarm)
        } else {
            // One-shot alarm — disable after firing
            alarmDao.setEnabled(alarmId, false)
        }
    }
}
