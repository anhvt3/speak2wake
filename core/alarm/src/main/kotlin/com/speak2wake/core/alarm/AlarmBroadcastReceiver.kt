package com.speak2wake.core.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlarmBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private var wakeLock: PowerManager.WakeLock? = null

        /** Release wake lock once the service is up. Called from AlarmForegroundService. */
        fun releaseWakeLock() {
            try {
                wakeLock?.let { if (it.isHeld) it.release() }
            } catch (_: Exception) {}
            wakeLock = null
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        // CRITICAL FIX: Guard — only handle our own action.
        if (intent.action != AndroidAlarmScheduler.ACTION_ALARM_TRIGGER) {
            Log.w("AlarmReceiver", "Ignoring unexpected action: ${intent.action}")
            return
        }

        val alarmId = intent.getLongExtra(AndroidAlarmScheduler.EXTRA_ALARM_ID, -1L)
        if (alarmId == -1L) {
            Log.w("AlarmReceiver", "Received alarm with no ID, ignoring")
            return
        }

        Log.d("AlarmReceiver", "Alarm triggered: id=$alarmId")

        // Acquire wake lock to ensure device stays awake until service starts
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "speak2wake:alarm_receiver",
        ).apply { acquire(10_000L) } // 10s timeout safety net

        val serviceIntent = Intent(context, AlarmForegroundService::class.java).apply {
            action = AlarmForegroundService.ACTION_START
            putExtra(AndroidAlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(
                AndroidAlarmScheduler.EXTRA_ALARM_LABEL,
                intent.getStringExtra(AndroidAlarmScheduler.EXTRA_ALARM_LABEL) ?: "",
            )
        }
        try {
            context.startForegroundService(serviceIntent)
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Failed to start foreground service for alarm $alarmId", e)
            releaseWakeLock()
        }
    }
}
