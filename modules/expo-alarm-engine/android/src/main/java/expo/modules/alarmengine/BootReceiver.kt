package expo.modules.alarmengine

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import org.json.JSONObject
import java.util.Calendar

/**
 * BroadcastReceiver that listens for BOOT_COMPLETED to re-register all
 * saved alarms after device restart. Alarms set via AlarmManager are lost
 * on reboot, so we persist them in SharedPreferences and restore here.
 */
class BootReceiver : BroadcastReceiver() {

  companion object {
    private const val TAG = "BootReceiver"
  }

  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

    Log.d(TAG, "Boot completed - re-registering alarms")
    rescheduleAllAlarms(context)
  }

  private fun rescheduleAllAlarms(context: Context) {
    val prefs = context.getSharedPreferences(
      AlarmEngineModule.PREFS_NAME,
      Context.MODE_PRIVATE
    )
    val alarmsJson = prefs.getString(AlarmEngineModule.ALARMS_KEY, "{}") ?: "{}"
    val alarms = JSONObject(alarmsJson)

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val keys = alarms.keys()

    while (keys.hasNext()) {
      val alarmId = keys.next()
      try {
        val alarmData = alarms.getJSONObject(alarmId)
        val hour = alarmData.getInt("hour")
        val minute = alarmData.getInt("minute")
        val repeatDays = alarmData.optJSONArray("repeatDays")

        if (repeatDays != null && repeatDays.length() > 0) {
          for (i in 0 until repeatDays.length()) {
            val day = repeatDays.getInt(i)
            val triggerTime = getNextTriggerTimeForDay(hour, minute, day)
            val compositeId = "${alarmId}_day_$day"
            scheduleExactAlarm(context, alarmManager, compositeId, alarmId, triggerTime)
          }
        } else {
          val triggerTime = getNextTriggerTime(hour, minute)
          scheduleExactAlarm(context, alarmManager, alarmId, alarmId, triggerTime)
        }

        Log.d(TAG, "Re-scheduled alarm: $alarmId")
      } catch (e: Exception) {
        Log.e(TAG, "Failed to re-schedule alarm: $alarmId", e)
      }
    }
  }

  private fun scheduleExactAlarm(
    context: Context,
    alarmManager: AlarmManager,
    compositeId: String,
    alarmId: String,
    triggerTimeMillis: Long
  ) {
    val intent = Intent(context, AlarmReceiver::class.java).apply {
      action = "expo.modules.alarmengine.ALARM_TRIGGERED"
      putExtra("alarm_id", alarmId)
      putExtra("composite_id", compositeId)
    }

    val requestCode = compositeId.hashCode()
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      requestCode,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      if (alarmManager.canScheduleExactAlarms()) {
        alarmManager.setExactAndAllowWhileIdle(
          AlarmManager.RTC_WAKEUP,
          triggerTimeMillis,
          pendingIntent
        )
      } else {
        alarmManager.setAndAllowWhileIdle(
          AlarmManager.RTC_WAKEUP,
          triggerTimeMillis,
          pendingIntent
        )
      }
    } else {
      alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        triggerTimeMillis,
        pendingIntent
      )
    }
  }

  private fun getNextTriggerTime(hour: Int, minute: Int): Long {
    val calendar = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, hour)
      set(Calendar.MINUTE, minute)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    if (calendar.timeInMillis <= System.currentTimeMillis()) {
      calendar.add(Calendar.DAY_OF_YEAR, 1)
    }
    return calendar.timeInMillis
  }

  private fun getNextTriggerTimeForDay(hour: Int, minute: Int, dayOfWeek: Int): Long {
    val calendarDay = dayOfWeek + 1

    val calendar = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, hour)
      set(Calendar.MINUTE, minute)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }

    val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    var daysUntil = calendarDay - today
    if (daysUntil < 0) {
      daysUntil += 7
    } else if (daysUntil == 0 && calendar.timeInMillis <= System.currentTimeMillis()) {
      daysUntil = 7
    }

    calendar.add(Calendar.DAY_OF_YEAR, daysUntil)
    return calendar.timeInMillis
  }
}
