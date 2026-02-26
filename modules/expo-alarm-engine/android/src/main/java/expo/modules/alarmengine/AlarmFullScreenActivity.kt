package expo.modules.alarmengine

import android.app.Activity
import android.app.KeyguardManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.util.Log

/**
 * Activity that displays over the lock screen when an alarm fires.
 * Uses appropriate flags for different Android versions to ensure
 * the alarm is visible even when the device is locked.
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

    // Finish this activity immediately - the React Native app will handle
    // the UI via the onAlarmFired event. This activity is only needed
    // to turn on the screen and show over lock screen.
    finish()
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
