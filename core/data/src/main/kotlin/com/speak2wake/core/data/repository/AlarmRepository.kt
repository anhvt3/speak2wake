package com.speak2wake.core.data.repository

import com.speak2wake.core.model.Alarm
import kotlinx.coroutines.flow.Flow

interface AlarmRepository {
    fun getAlarms(): Flow<List<Alarm>>
    suspend fun getAlarmById(id: Long): Alarm?
    suspend fun createAlarm(alarm: Alarm): Long
    suspend fun updateAlarm(alarm: Alarm)
    suspend fun deleteAlarm(id: Long)
    suspend fun setAlarmEnabled(id: Long, enabled: Boolean)
    /** Re-schedule a repeating alarm for its next occurrence, or disable if one-shot. */
    suspend fun rescheduleAfterFire(alarmId: Long)
}
