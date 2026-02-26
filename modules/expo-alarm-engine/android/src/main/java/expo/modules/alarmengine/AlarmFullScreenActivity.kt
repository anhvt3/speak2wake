package expo.modules.alarmengine

import android.app.Activity
import android.app.KeyguardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.util.Log

/**
 * Activity that displays over the lock screen when an alarm fires.
 * Turns on screen, dismisses keyguard, and launches the main React Native
 * activity so the user sees the Ring screen.
 */
class AlarmFullScreenActivity : Activity() {

  companion object {
    private const val TAG = "AlarmFullScreenActivity"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val alarmId = intent?.getStringExtra("alarm_id") ?: "unknown"
    Log.d(TAG, "AlarmFullScreenActivity launched for alarm: $alarmId")

    setupLockScreenFlags()

    // Launch the main React Native activity to bring app to foreground
    // This ensures the JS layer receives the onAlarmFired event
    try {
      val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
      if (launchIntent != null) {
        launchIntent.addFlags(
          Intent.FLAG_ACTIVITY_NEW_TASK or
          Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
          Intent.FLAG_ACTIVITY_SINGLE_TOP
        )
        launchIntent.putExtra("alarm_id", alarmId)
        startActivity(launchIntent)
        Log.d(TAG, "Launched main activity for alarm: $alarmId")
      } else {
        Log.e(TAG, "Could not get launch intent for package: $packageName")
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed to launch main activity", e)
    }

    // Delay finish to let main activity take over screen-on/keyguard flags
    Handler(Looper.getMainLooper()).postDelayed({ finish() }, 1500)
  }

  @Suppress("DEPRECATION")
  private fun setupLockScreenFlags() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
      setShowWhenLocked(true)
      setTurnScreenOn(true)

      val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
      keyguardManager.requestDismissKeyguard(this, null)
    } else {
      window.addFlags(
        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
      )
    }

    window.addFlags(
      WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
      WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
    )
  }
}
