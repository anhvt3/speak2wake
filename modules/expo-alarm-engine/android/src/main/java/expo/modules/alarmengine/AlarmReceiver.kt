package expo.modules.alarmengine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log

/**
 * BroadcastReceiver that fires when an alarm triggers.
 * Acquires a WakeLock and starts the AlarmForegroundService.
 */
class AlarmReceiver : BroadcastReceiver() {

  companion object {
    private const val TAG = "AlarmReceiver"
    private const val WAKELOCK_TAG = "expo:alarm_wakelock"
    private const val WAKELOCK_TIMEOUT = 60_000L // 1 minute max
  }

  override fun onReceive(context: Context, intent: Intent) {
    val alarmId = intent.getStringExtra("alarm_id") ?: return
    val isSnooze = intent.getBooleanExtra("is_snooze", false)

    Log.d(TAG, "Alarm received: alarmId=$alarmId, isSnooze=$isSnooze")

    // Acquire a partial WakeLock to ensure the device stays awake
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    val wakeLock = powerManager.newWakeLock(
      PowerManager.PARTIAL_WAKE_LOCK,
      WAKELOCK_TAG
    )
    wakeLock.acquire(WAKELOCK_TIMEOUT)

    try {
      // Start the foreground service to play alarm sound and show notification
      val serviceIntent = Intent(context, AlarmForegroundService::class.java).apply {
        action = AlarmForegroundService.ACTION_START
        putExtra("alarm_id", alarmId)
        putExtra("is_snooze", isSnooze)
      }

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(serviceIntent)
      } else {
        context.startService(serviceIntent)
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed to start AlarmForegroundService", e)
    } finally {
      // The service will manage its own WakeLock; release this one
      if (wakeLock.isHeld) {
        wakeLock.release()
      }
    }
  }
}
