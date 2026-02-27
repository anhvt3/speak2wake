package expo.modules.alarmengine

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.Timer
import java.util.TimerTask

/**
 * Foreground service that plays alarm sound with gradual volume increase,
 * vibrates, and shows a persistent notification with full-screen intent.
 */
class AlarmForegroundService : Service() {

  companion object {
    private const val TAG = "AlarmForegroundService"
    const val ACTION_START = "expo.modules.alarmengine.ACTION_START_ALARM"
    const val ACTION_STOP = "expo.modules.alarmengine.ACTION_STOP_ALARM"
    const val ACTION_PAUSE_SOUND = "expo.modules.alarmengine.ACTION_PAUSE_SOUND"
    const val ACTION_RESUME_SOUND = "expo.modules.alarmengine.ACTION_RESUME_SOUND"
    private const val CHANNEL_ID = "alarm_channel"
    private const val CHANNEL_NAME = "Alarm"
    private const val NOTIFICATION_ID = 9001
    private const val WAKELOCK_TAG = "expo:alarm_service_wakelock"

    // Gradual volume increase settings
    private const val VOLUME_RAMP_DURATION_MS = 30_000L // 30 seconds
    private const val VOLUME_STEP_INTERVAL_MS = 500L // update every 500ms
  }

  private var mediaPlayer: MediaPlayer? = null
  private var vibrator: Vibrator? = null
  private var wakeLock: PowerManager.WakeLock? = null
  private var volumeTimer: Timer? = null
  private var currentAlarmId: String? = null
  private var isAlarmActive = false
  @Volatile private var isSoundPlaying = false

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onCreate() {
    super.onCreate()
    createNotificationChannel()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    when (intent?.action) {
      ACTION_START -> {
        val alarmId = intent.getStringExtra("alarm_id") ?: "unknown"
        currentAlarmId = alarmId
        isAlarmActive = true
        // CRITICAL: call startForeground FIRST to avoid ANR/crash on Android 12+
        startForegroundNotification(alarmId)
        acquireWakeLock()
        startAlarmSound()
        startVibration()
        sendAlarmFiredEvent(alarmId)
      }
      ACTION_STOP -> {
        stopAlarm()
        stopSelf()
      }
      ACTION_PAUSE_SOUND -> {
        if (!isAlarmActive) { stopSelf(); return START_NOT_STICKY }
        try {
          mediaPlayer?.let {
            if (isSoundPlaying) { it.pause(); isSoundPlaying = false }
          }
          vibrator?.cancel()
        } catch (e: Exception) {
          Log.e(TAG, "Error pausing alarm", e)
        }
      }
      ACTION_RESUME_SOUND -> {
        if (!isAlarmActive) { stopSelf(); return START_NOT_STICKY }
        try {
          mediaPlayer?.let {
            if (!isSoundPlaying) { it.start(); isSoundPlaying = true }
          }
          startVibration()
        } catch (e: Exception) {
          Log.e(TAG, "Error resuming alarm", e)
        }
      }
      else -> {
        stopSelf()
      }
    }
    return START_NOT_STICKY
  }

  override fun onDestroy() {
    stopAlarm()
    super.onDestroy()
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        CHANNEL_NAME,
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Alarm notifications"
        setBypassDnd(true)
        lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        setSound(null, null) // We handle sound manually via MediaPlayer
        enableVibration(false) // We handle vibration manually
      }

      val notificationManager = getSystemService(NotificationManager::class.java)
      notificationManager.createNotificationChannel(channel)
    }
  }

  private fun startForegroundNotification(alarmId: String) {
    // Load alarm label from prefs
    val prefs = getSharedPreferences(AlarmEngineModule.PREFS_NAME, Context.MODE_PRIVATE)
    val alarmsJson = prefs.getString(AlarmEngineModule.ALARMS_KEY, "{}") ?: "{}"
    val alarms = org.json.JSONObject(alarmsJson)
    val label = if (alarms.has(alarmId)) {
      alarms.getJSONObject(alarmId).optString("label", "Alarm")
    } else {
      "Alarm"
    }

    // Create full-screen intent for lock screen display
    val fullScreenIntent = Intent(this, AlarmFullScreenActivity::class.java).apply {
      putExtra("alarm_id", alarmId)
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION)
    }
    val fullScreenPendingIntent = PendingIntent.getActivity(
      this,
      alarmId.hashCode(),
      fullScreenIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Dismiss action
    val dismissIntent = Intent(this, AlarmForegroundService::class.java).apply {
      action = ACTION_STOP
      putExtra("alarm_id", alarmId)
    }
    val dismissPendingIntent = PendingIntent.getService(
      this,
      (alarmId + "_dismiss").hashCode(),
      dismissIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(this, CHANNEL_ID)
      .setContentTitle("Speak2Wake")
      .setContentText(label.ifEmpty { "Time to wake up!" })
      .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
      .setPriority(NotificationCompat.PRIORITY_MAX)
      .setCategory(NotificationCompat.CATEGORY_ALARM)
      .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
      .setOngoing(true)
      .setAutoCancel(false)
      .setFullScreenIntent(fullScreenPendingIntent, true)
      .addAction(
        android.R.drawable.ic_menu_close_clear_cancel,
        "Dismiss",
        dismissPendingIntent
      )
      .build()

    startForeground(NOTIFICATION_ID, notification)
  }

  private fun startAlarmSound() {
    try {
      val soundId = getSoundIdForCurrentAlarm()
      val alarmUri = getSoundUri(soundId)

      if (alarmUri == null) {
        Log.e(TAG, "No alarm URI found for soundId=$soundId")
        return
      }

      Log.d(TAG, "Playing alarm sound: soundId=$soundId, uri=$alarmUri")

      mediaPlayer = MediaPlayer().apply {
        setAudioAttributes(
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        )
        setDataSource(this@AlarmForegroundService, alarmUri)
        isLooping = true
        setVolume(0f, 0f) // Start silent for gradual increase
        prepare()
        start()
      }
      isSoundPlaying = true

      // Gradually increase volume from 0 to 1 over VOLUME_RAMP_DURATION_MS
      startGradualVolumeIncrease()
    } catch (e: Exception) {
      Log.e(TAG, "Failed to start alarm sound", e)
    }
  }

  /**
   * Read the soundId for the currently firing alarm from SharedPreferences.
   */
  private fun getSoundIdForCurrentAlarm(): String {
    val alarmId = currentAlarmId ?: return "default"
    try {
      val prefs = getSharedPreferences(AlarmEngineModule.PREFS_NAME, Context.MODE_PRIVATE)
      val alarmsJson = prefs.getString(AlarmEngineModule.ALARMS_KEY, "{}") ?: "{}"
      val alarms = org.json.JSONObject(alarmsJson)
      if (alarms.has(alarmId)) {
        return alarms.getJSONObject(alarmId).optString("soundId", "default")
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error reading soundId from prefs", e)
    }
    return "default"
  }

  /**
   * Map a soundId string to a raw resource URI, falling back to system default.
   */
  private fun getSoundUri(soundId: String): Uri? {
    val resId = when (soundId) {
      "default"   -> getResourceId("alarm_default")
      "gentle"    -> getResourceId("alarm_gentle")
      "energetic" -> getResourceId("alarm_energetic")
      "nature"    -> getResourceId("alarm_nature")
      "digital"   -> getResourceId("alarm_digital")
      else        -> 0
    }

    if (resId != 0) {
      return Uri.parse("android.resource://$packageName/$resId")
    }

    // Fallback to system default alarm sound
    return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
      ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
      ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
  }

  /**
   * Safely look up a raw resource ID by name.
   */
  private fun getResourceId(name: String): Int {
    return try {
      resources.getIdentifier(name, "raw", packageName)
    } catch (e: Exception) {
      Log.w(TAG, "Resource not found: raw/$name")
      0
    }
  }

  private fun startGradualVolumeIncrease() {
    val totalSteps = (VOLUME_RAMP_DURATION_MS / VOLUME_STEP_INTERVAL_MS).toInt()
    var currentStep = 0

    volumeTimer = Timer().apply {
      scheduleAtFixedRate(object : TimerTask() {
        override fun run() {
          currentStep++
          val volume = (currentStep.toFloat() / totalSteps).coerceAtMost(1f)
          try {
            mediaPlayer?.setVolume(volume, volume)
          } catch (e: Exception) {
            Log.e(TAG, "Error setting volume", e)
          }
          if (currentStep >= totalSteps) {
            cancel()
          }
        }
      }, VOLUME_STEP_INTERVAL_MS, VOLUME_STEP_INTERVAL_MS)
    }
  }

  private fun startVibration() {
    vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
      vibratorManager.defaultVibrator
    } else {
      @Suppress("DEPRECATION")
      getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    // Vibration pattern: wait 0ms, vibrate 500ms, pause 500ms, vibrate 500ms, pause 500ms
    val pattern = longArrayOf(0, 500, 500, 500, 500)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      vibrator?.vibrate(
        VibrationEffect.createWaveform(pattern, 0) // repeat from index 0
      )
    } else {
      @Suppress("DEPRECATION")
      vibrator?.vibrate(pattern, 0)
    }
  }

  private fun sendAlarmFiredEvent(alarmId: String) {
    // Send via static bridge to the Expo module's JS event system
    AlarmEngineModule.sendEventToJS(alarmId)
    Log.d(TAG, "Sent alarm fired event for: $alarmId")
  }

  private fun acquireWakeLock() {
    val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
    wakeLock = powerManager.newWakeLock(
      PowerManager.PARTIAL_WAKE_LOCK,
      WAKELOCK_TAG
    ).apply {
      acquire(5 * 60 * 1000L) // Max 5 minutes
    }
  }

  private fun stopAlarm() {
    if (!isAlarmActive) return
    isAlarmActive = false
    isSoundPlaying = false

    // Stop volume ramp timer
    volumeTimer?.cancel()
    volumeTimer = null

    // Stop and release MediaPlayer
    try {
      mediaPlayer?.let {
        try { it.stop() } catch (_: Exception) {}
        it.release()
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error stopping media player", e)
    }
    mediaPlayer = null

    // Stop vibration
    vibrator?.cancel()
    vibrator = null

    // Release WakeLock
    try {
      if (wakeLock?.isHeld == true) {
        wakeLock?.release()
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error releasing wakelock", e)
    }
    wakeLock = null

    currentAlarmId = null
  }
}
