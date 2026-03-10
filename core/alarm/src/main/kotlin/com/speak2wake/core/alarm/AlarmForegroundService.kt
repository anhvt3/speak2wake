package com.speak2wake.core.alarm

import android.app.*
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.speak2wake.core.database.AlarmDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class AlarmForegroundService : Service() {

    companion object {
        const val ACTION_START = "com.speak2wake.alarm.START"
        const val ACTION_STOP = "com.speak2wake.alarm.STOP"
        const val ACTION_PAUSE = "com.speak2wake.alarm.PAUSE"
        const val ACTION_RESUME = "com.speak2wake.alarm.RESUME"
        const val ACTION_SNOOZE = "com.speak2wake.alarm.SNOOZE"
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "speak2wake_alarm_channel"
        private const val VOLUME_RAMP_DURATION_MS = 10_000L
        private const val SNOOZE_DURATION_MS = 10 * 60 * 1000L
    }

    @Inject lateinit var alarmDao: AlarmDao

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var volumeHandler: Handler? = null
    private var currentVolume = 0f
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val alarmId = intent.getLongExtra(AndroidAlarmScheduler.EXTRA_ALARM_ID, -1L)
                val label = intent.getStringExtra(AndroidAlarmScheduler.EXTRA_ALARM_LABEL) ?: "Alarm"
                startAlarm(alarmId, label)
            }
            ACTION_STOP -> stopAlarm()
            ACTION_PAUSE -> pauseSound()
            ACTION_RESUME -> resumeSound()
            ACTION_SNOOZE -> snoozeAlarm()
        }
        return START_NOT_STICKY
    }

    private fun startAlarm(alarmId: Long, label: String) {
        Log.d("AlarmService", "Starting alarm $alarmId")
        startForeground(NOTIFICATION_ID, buildNotification(alarmId, label))
        AlarmBroadcastReceiver.releaseWakeLock()
        startVibration()

        // Load custom sound URI from DB, then start sound
        serviceScope.launch {
            val customSoundUri = try {
                alarmDao.getAlarmById(alarmId)?.soundUri?.takeIf { it.isNotBlank() }
            } catch (e: Exception) {
                Log.w("AlarmService", "Failed to load alarm sound URI", e)
                null
            }
            withContext(Dispatchers.Main) {
                startSound(customSoundUri)
            }
        }

        val pm = getSystemService(PowerManager::class.java)
        val km = getSystemService(KeyguardManager::class.java)
        val isDeviceAwakeAndUnlocked = pm?.isInteractive == true && km?.isKeyguardLocked == false

        if (isDeviceAwakeAndUnlocked) {
            Log.d("AlarmService", "Device unlocked, launching AlarmActivity directly")
            val fullScreenIntent = Intent(this, AlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(AndroidAlarmScheduler.EXTRA_ALARM_ID, alarmId)
            }
            startActivity(fullScreenIntent)
        } else {
            Log.d("AlarmService", "Device locked/asleep, relying on full-screen notification")
        }
    }

    private val alarmAudioAttributes =
            AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()

    private fun startSound(customSoundUri: String? = null) {
        val uri = if (customSoundUri != null) {
            Uri.parse(customSoundUri)
        } else {
            resolveAlarmSoundUri()
        }
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, uri)
                setAudioAttributes(alarmAudioAttributes)
                isLooping = true
                setVolume(0f, 0f)
                prepare()
                start()
            }
            rampVolume()
        } catch (e: Exception) {
            Log.e("AlarmService", "Failed to start sound with $uri", e)
            tryFallbackSound()
        }
    }

    private fun resolveAlarmSoundUri(): Uri {
        return try {
            val resId = resources.getIdentifier("alarm_sound", "raw", packageName)
            if (resId != 0) {
                Uri.parse("android.resource://$packageName/$resId")
            } else {
                throw IllegalStateException("alarm_sound resource not found")
            }
        } catch (e: Exception) {
            Log.w("AlarmService", "Custom alarm sound not available, using system default", e)
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                    ?: Uri.parse("content://settings/system/alarm_alert")
        }
    }

    private fun tryFallbackSound() {
        try {
            val fallbackUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    ?: return
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, fallbackUri)
                setAudioAttributes(alarmAudioAttributes)
                isLooping = true
                setVolume(0f, 0f)
                prepare()
                start()
            }
            rampVolume()
        } catch (e: Exception) {
            Log.e("AlarmService", "Fallback sound also failed", e)
        }
    }

    private fun rampVolume() {
        currentVolume = 0.3f
        mediaPlayer?.setVolume(currentVolume, currentVolume)
        val steps = 20
        val intervalMs = VOLUME_RAMP_DURATION_MS / steps
        volumeHandler = Handler(Looper.getMainLooper())
        var step = 0
        val runnable = object : Runnable {
            override fun run() {
                if (step < steps) {
                    currentVolume = 0.3f + (0.7f * step.toFloat() / steps)
                    mediaPlayer?.setVolume(currentVolume, currentVolume)
                    step++
                    volumeHandler?.postDelayed(this, intervalMs)
                } else {
                    mediaPlayer?.setVolume(1f, 1f)
                }
            }
        }
        volumeHandler?.post(runnable)
    }

    private fun startVibration() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(VibratorManager::class.java)
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION") getSystemService(Vibrator::class.java)
        }
        val pattern = longArrayOf(0, 800, 200, 800, 200)
        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
    }

    private fun pauseSound() {
        volumeHandler?.removeCallbacksAndMessages(null)
        mediaPlayer?.pause()
        vibrator?.cancel()
    }

    private fun resumeSound() {
        mediaPlayer?.start()
        mediaPlayer?.setVolume(currentVolume, currentVolume)
        startVibration()
    }

    private fun stopAlarm() {
        Log.d("AlarmService", "Stopping alarm")
        releaseResources()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun snoozeAlarm() {
        Log.d("AlarmService", "Snoozing alarm for 10 minutes")
        releaseResources()
        val snoozeIntent = Intent(this, AlarmForegroundService::class.java).apply {
            action = ACTION_START
            putExtra(AndroidAlarmScheduler.EXTRA_ALARM_ID, -1L)
            putExtra(AndroidAlarmScheduler.EXTRA_ALARM_LABEL, "Snoozed Alarm")
        }
        val pendingSnooze = PendingIntent.getService(
            this, 9999, snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = getSystemService(AlarmManager::class.java)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + SNOOZE_DURATION_MS,
            pendingSnooze
        )
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun releaseResources() {
        volumeHandler?.removeCallbacksAndMessages(null)
        volumeHandler = null
        try { mediaPlayer?.stop() } catch (_: Exception) {}
        mediaPlayer?.release()
        mediaPlayer = null
        vibrator?.cancel()
        vibrator = null
    }

    override fun onDestroy() {
        Log.d("AlarmService", "Service destroyed")
        serviceScope.cancel()
        releaseResources()
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d("AlarmService", "Task removed - alarm continues ringing")
        super.onTaskRemoved(rootIntent)
    }

    private fun buildNotification(alarmId: Long, label: String): Notification {
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this, alarmId.toInt(),
            Intent(this, AlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(AndroidAlarmScheduler.EXTRA_ALARM_ID, alarmId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val snoozePendingIntent = PendingIntent.getService(
            this, (alarmId * 10 + 2).toInt(),
            Intent(this, AlarmForegroundService::class.java).apply {
                action = ACTION_SNOOZE
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(label)
                .setContentText("Tap to open Speak2Wake challenge")
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setContentIntent(fullScreenPendingIntent)
                .addAction(0, "Snooze 10m", snoozePendingIntent)
                .setOngoing(true)
                .setAutoCancel(false)
                .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "Speak2Wake Alarms", NotificationManager.IMPORTANCE_HIGH
        ).apply {
            setBypassDnd(true)
            setShowBadge(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?) = null
}
