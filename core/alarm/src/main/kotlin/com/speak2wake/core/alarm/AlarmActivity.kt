package com.speak2wake.core.alarm

import android.app.KeyguardManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Full-screen activity that:
 * 1. Wakes the screen (turn on when locked)
 * 2. Dismisses the keyguard (lock screen)
 * 3. Launches MainActivity for full challenge flow
 */
@AndroidEntryPoint
class AlarmActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // CRITICAL: Turn on screen and show over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            // Request keyguard dismissal so ring screen is fully interactive
            val km = getSystemService(KeyguardManager::class.java)
            km?.requestDismissKeyguard(this, object : KeyguardManager.KeyguardDismissCallback() {
                override fun onDismissSucceeded() {
                    Log.d("AlarmActivity", "Keyguard dismissed")
                }
                override fun onDismissError() {
                    Log.w("AlarmActivity", "Keyguard dismiss error")
                }
                override fun onDismissCancelled() {
                    Log.w("AlarmActivity", "Keyguard dismiss cancelled")
                }
            })
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
            )
        }

        val alarmId = intent.getLongExtra(AndroidAlarmScheduler.EXTRA_ALARM_ID, -1L)
        Log.d("AlarmActivity", "Alarm activity created for alarm $alarmId")

        // Delegate to MainActivity with the alarm ID — it handles Ring/Challenge navigation
        val mainIntent = Intent().apply {
            component = ComponentName(packageName, "com.speak2wake.MainActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AndroidAlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra("navigate_to_ring", true)
        }
        startActivity(mainIntent)
        finish()
    }
}
