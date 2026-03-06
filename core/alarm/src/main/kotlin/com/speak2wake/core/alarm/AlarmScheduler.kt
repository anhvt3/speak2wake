package com.speak2wake.core.alarm

import com.speak2wake.core.model.Alarm

interface AlarmScheduler {
    fun schedule(alarm: Alarm)
    fun scheduleSnooze(alarmId: Long, label: String, snoozeMinutes: Int)
    fun cancel(alarmId: Long)
    fun cancelAll()
}
