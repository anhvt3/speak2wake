package expo.modules.alarmengine

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class AlarmEngineModule : Module() {

  companion object {
    const val PREFS_NAME = "expo_alarm_engine_prefs"
    const val ALARMS_KEY = "scheduled_alarms"
    const val EVENT_ALARM_FIRED = "onAlarmFired"

    // Static reference for service -> module event bridge
    private var instance: AlarmEngineModule? = null

    fun sendEventToJS(alarmId: String) {
      instance?.sendEvent(EVENT_ALARM_FIRED, mapOf("alarmId" to alarmId))
    }
  }

  private val context: Context
    get() = appContext.reactContext ?: throw IllegalStateException("React context is not available")

  private val alarmManager: AlarmManager
    get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

  override fun definition() = ModuleDefinition {
    Name("ExpoAlarmEngine")

    Events(EVENT_ALARM_FIRED)

    OnCreate {
      instance = this@AlarmEngineModule
    }

    OnDestroy {
      instance = null
    }

    Function("scheduleAlarm") { alarmId: String, hour: Int, minute: Int, repeatDays: List<Int>, soundId: String, label: String ->
      scheduleAlarmInternal(alarmId, hour, minute, repeatDays, soundId, label)
    }

    Function("cancelAlarm") { alarmId: String ->
      cancelAlarmInternal(alarmId)
    }

    Function("snoozeAlarm") { alarmId: String, minutes: Int ->
      snoozeAlarmInternal(alarmId, minutes)
    }

    Function("dismissAlarm") { alarmId: String ->
      dismissAlarmInternal(alarmId)
    }

    Function("getNextAlarmTime") { alarmId: String ->
      getNextAlarmTimeInternal(alarmId)
    }

    Function("pauseAlarmSound") {
      // Send pause intent to foreground service
      val intent = Intent(context, AlarmForegroundService::class.java).apply {
        action = AlarmForegroundService.ACTION_PAUSE_SOUND
      }
      context.startService(intent)
    }

    Function("resumeAlarmSound") {
      // Send resume intent to foreground service
      val intent = Intent(context, AlarmForegroundService::class.java).apply {
        action = AlarmForegroundService.ACTION_RESUME_SOUND
      }
      context.startService(intent)
    }
  }

  private fun scheduleAlarmInternal(
    alarmId: String,
    hour: Int,
    minute: Int,
    repeatDays: List<Int>,
    soundId: String,
    label: String
  ) {
    // Save alarm data to SharedPreferences for persistence across reboots
    saveAlarmToPrefs(alarmId, hour, minute, repeatDays, soundId, label)

    if (repeatDays.isEmpty()) {
      // One-shot alarm: schedule for the next occurrence of this time
      val triggerTime = getNextTriggerTime(hour, minute)
      setExactAlarm(alarmId, triggerTime)
    } else {
      // Repeating alarm: schedule for each day of the week
      for (day in repeatDays) {
        val triggerTime = getNextTriggerTimeForDay(hour, minute, day)
        val compositeId = "${alarmId}_day_$day"
        setExactAlarm(compositeId, triggerTime)
      }
    }
  }

  private fun setExactAlarm(alarmId: String, triggerTimeMillis: Long) {
    val intent = Intent(context, AlarmReceiver::class.java).apply {
      action = "expo.modules.alarmengine.ALARM_TRIGGERED"
      putExtra("alarm_id", alarmId.split("_day_").first())
      putExtra("composite_id", alarmId)
    }

    val requestCode = alarmId.hashCode()
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
        // Fallback: setAndAllowWhileIdle (inexact but still works)
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

  private fun cancelAlarmInternal(alarmId: String) {
    val alarmData = getAlarmFromPrefs(alarmId)
    if (alarmData != null) {
      val repeatDays = alarmData.optJSONArray("repeatDays")
      if (repeatDays != null && repeatDays.length() > 0) {
        for (i in 0 until repeatDays.length()) {
          val compositeId = "${alarmId}_day_${repeatDays.getInt(i)}"
          cancelPendingIntent(compositeId)
        }
      } else {
        cancelPendingIntent(alarmId)
      }
    } else {
      // Fallback: try to cancel directly
      cancelPendingIntent(alarmId)
    }

    removeAlarmFromPrefs(alarmId)
  }

  private fun cancelPendingIntent(alarmId: String) {
    val intent = Intent(context, AlarmReceiver::class.java).apply {
      action = "expo.modules.alarmengine.ALARM_TRIGGERED"
    }
    val requestCode = alarmId.hashCode()
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      requestCode,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    alarmManager.cancel(pendingIntent)
    pendingIntent.cancel()
  }

  private fun snoozeAlarmInternal(alarmId: String, minutes: Int) {
    // First dismiss the current alarm
    dismissAlarmInternal(alarmId)

    // Schedule a new alarm X minutes from now
    val triggerTime = System.currentTimeMillis() + (minutes * 60 * 1000L)
    val snoozeId = "${alarmId}_snooze"

    val intent = Intent(context, AlarmReceiver::class.java).apply {
      action = "expo.modules.alarmengine.ALARM_TRIGGERED"
      putExtra("alarm_id", alarmId)
      putExtra("composite_id", snoozeId)
      putExtra("is_snooze", true)
    }

    val requestCode = snoozeId.hashCode()
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      requestCode,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    alarmManager.setExactAndAllowWhileIdle(
      AlarmManager.RTC_WAKEUP,
      triggerTime,
      pendingIntent
    )
  }

  private fun dismissAlarmInternal(alarmId: String) {
    // Stop the foreground service
    val serviceIntent = Intent(context, AlarmForegroundService::class.java).apply {
      action = AlarmForegroundService.ACTION_STOP
      putExtra("alarm_id", alarmId)
    }
    context.stopService(serviceIntent)

    // Cancel any snooze pending intent
    val snoozeId = "${alarmId}_snooze"
    cancelPendingIntent(snoozeId)

    // Re-schedule for the next occurrence if this is a repeating alarm
    val alarmData = getAlarmFromPrefs(alarmId)
    if (alarmData != null) {
      val repeatDays = alarmData.optJSONArray("repeatDays")
      if (repeatDays != null && repeatDays.length() > 0) {
        val hour = alarmData.getInt("hour")
        val minute = alarmData.getInt("minute")
        val soundId = alarmData.optString("soundId", "default")
        val label = alarmData.optString("label", "")
        val days = mutableListOf<Int>()
        for (i in 0 until repeatDays.length()) {
          days.add(repeatDays.getInt(i))
        }
        // Re-schedule for repeating days (next week's occurrences)
        for (day in days) {
          val triggerTime = getNextTriggerTimeForDay(hour, minute, day)
          val compositeId = "${alarmId}_day_$day"
          setExactAlarm(compositeId, triggerTime)
        }
      }
    }
  }

  private fun getNextAlarmTimeInternal(alarmId: String): Double {
    val alarmData = getAlarmFromPrefs(alarmId) ?: return -1.0

    val hour = alarmData.getInt("hour")
    val minute = alarmData.getInt("minute")
    val repeatDays = alarmData.optJSONArray("repeatDays")

    return if (repeatDays != null && repeatDays.length() > 0) {
      var earliest = Long.MAX_VALUE
      for (i in 0 until repeatDays.length()) {
        val time = getNextTriggerTimeForDay(hour, minute, repeatDays.getInt(i))
        if (time < earliest) earliest = time
      }
      earliest.toDouble()
    } else {
      getNextTriggerTime(hour, minute).toDouble()
    }
  }

  // --- Time calculation helpers ---

  private fun getNextTriggerTime(hour: Int, minute: Int): Long {
    val calendar = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, hour)
      set(Calendar.MINUTE, minute)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    // If the time has already passed today, schedule for tomorrow
    if (calendar.timeInMillis <= System.currentTimeMillis()) {
      calendar.add(Calendar.DAY_OF_YEAR, 1)
    }
    return calendar.timeInMillis
  }

  private fun getNextTriggerTimeForDay(hour: Int, minute: Int, dayOfWeek: Int): Long {
    // dayOfWeek: 0=Sunday, 1=Monday, ..., 6=Saturday
    // Calendar: 1=Sunday, 2=Monday, ..., 7=Saturday
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

  // --- SharedPreferences persistence ---

  private fun saveAlarmToPrefs(
    alarmId: String,
    hour: Int,
    minute: Int,
    repeatDays: List<Int>,
    soundId: String,
    label: String
  ) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val alarmsJson = prefs.getString(ALARMS_KEY, "{}") ?: "{}"
    val alarms = JSONObject(alarmsJson)

    val alarmData = JSONObject().apply {
      put("alarmId", alarmId)
      put("hour", hour)
      put("minute", minute)
      put("repeatDays", JSONArray(repeatDays))
      put("soundId", soundId)
      put("label", label)
    }

    alarms.put(alarmId, alarmData)
    prefs.edit().putString(ALARMS_KEY, alarms.toString()).apply()
  }

  private fun getAlarmFromPrefs(alarmId: String): JSONObject? {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val alarmsJson = prefs.getString(ALARMS_KEY, "{}") ?: "{}"
    val alarms = JSONObject(alarmsJson)
    return if (alarms.has(alarmId)) alarms.getJSONObject(alarmId) else null
  }

  private fun removeAlarmFromPrefs(alarmId: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val alarmsJson = prefs.getString(ALARMS_KEY, "{}") ?: "{}"
    val alarms = JSONObject(alarmsJson)
    alarms.remove(alarmId)
    prefs.edit().putString(ALARMS_KEY, alarms.toString()).apply()
  }
}
