package com.speak2wake.core.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.speak2wake.core.model.Alarm
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : AlarmScheduler {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    companion object {
        // CRITICAL: Custom action — only Speak2Wake can receive this, not Google Clock or any other app
        const val ACTION_ALARM_TRIGGER = "com.speak2wake.ACTION_ALARM_TRIGGER"
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_ALARM_LABEL = "extra_alarm_label"
    }

    override fun schedule(alarm: Alarm) {
        if (!alarm.isEnabled) return
        val triggerAtMillis = computeNextTriggerMillis(alarm) ?: return

        val pendingIntent = buildPendingIntent(alarm)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && 
            !alarmManager.canScheduleExactAlarms()) {
            // Fallback to inexact if permission not granted
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            return
        }

        // setExactAndAllowWhileIdle bypasses Doze mode — required for reliable alarms
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent,
        )
    }

    override fun scheduleSnooze(alarmId: Long, label: String, snoozeMinutes: Int) {
        val triggerAtMillis = System.currentTimeMillis() + snoozeMinutes * 60_000L
        val intent = Intent(context, AlarmBroadcastReceiver::class.java).apply {
            action = ACTION_ALARM_TRIGGER
            setPackage(context.packageName)
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_ALARM_LABEL, label)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            return
        }
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }

    override fun cancel(alarmId: Long) {
        val pendingIntent = buildPendingIntentForId(alarmId)
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    override fun cancelAll() {
        // Called on app uninstall / data clear — BootReceiver handles re-registration
    }

    private fun buildPendingIntent(alarm: Alarm): PendingIntent {
        val intent = Intent(context, AlarmBroadcastReceiver::class.java).apply {
            // CRITICAL FIX: Use custom action so only our receiver responds.
            // This prevents Google Clock or any other alarm app from triggering our service.
            action = ACTION_ALARM_TRIGGER
            // CRITICAL FIX: Explicit package — Android won't deliver to other apps
            setPackage(context.packageName)
            putExtra(EXTRA_ALARM_ID, alarm.id)
            putExtra(EXTRA_ALARM_LABEL, alarm.label)
        }
        return PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(), // unique requestCode per alarm
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun buildPendingIntentForId(alarmId: Long): PendingIntent? {
        val intent = Intent(context, AlarmBroadcastReceiver::class.java).apply {
            action = ACTION_ALARM_TRIGGER
            setPackage(context.packageName)
        }
        return PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun computeNextTriggerMillis(alarm: Alarm): Long? {
        val now = LocalDateTime.now()
        val alarmTime = alarm.time

        if (alarm.repeatDays.isEmpty()) {
            // One-shot: fire today if time hasn't passed, else tomorrow
            val candidate = LocalDateTime.of(now.toLocalDate(), alarmTime)
            val triggerDate = if (candidate.isAfter(now)) candidate else candidate.plusDays(1)
            return triggerDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }

        // Repeating: find the next matching day
        var daysAhead = 0
        while (daysAhead < 8) {
            val candidate = LocalDateTime.of(now.toLocalDate().plusDays(daysAhead.toLong()), alarmTime)
            val dayOfWeek = candidate.dayOfWeek
            if (dayOfWeek in alarm.repeatDays && candidate.isAfter(now)) {
                return candidate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            }
            daysAhead++
        }
        return null
    }
}
