package com.speak2wake

import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.speak2wake.core.alarm.AlarmForegroundService
import com.speak2wake.core.alarm.AndroidAlarmScheduler
import com.speak2wake.core.data.KEY_LANGUAGE
import com.speak2wake.core.data.settingsDataStore
import com.speak2wake.core.designsystem.strings.*
import com.speak2wake.core.designsystem.theme.Speak2WakeTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    private val pendingAlarmId = mutableStateOf<Long?>(null)

    private val defaultLanguage: String
        get() = if (java.util.Locale.getDefault().language == "vi") "vi" else "en"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val km = getSystemService(KeyguardManager::class.java)
            km?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        val permissions = mutableListOf(android.Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissions.toTypedArray())
        ensureFullScreenIntentPermission()
        handleAlarmIntent(intent)

        setContent {
            val languageCode by settingsDataStore.data
                .map { it[KEY_LANGUAGE] ?: defaultLanguage }
                .collectAsState(initial = defaultLanguage)

            val strings = if (languageCode == "vi") VietnameseStrings else EnglishStrings

            CompositionLocalProvider(LocalStrings provides strings) {
                Speak2WakeTheme {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        Speak2WakeNavHost(
                            pendingAlarmId = pendingAlarmId.value,
                            onAlarmConsumed = { pendingAlarmId.value = null },
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleAlarmIntent(intent)
    }

    private fun handleAlarmIntent(intent: Intent?) {
        val alarmId = extractAlarmId(intent) ?: return
        Log.d("MainActivity", "Handling alarm intent for alarm $alarmId")
        if (intent?.getBooleanExtra("trigger_alarm_service", false) == true) {
            val serviceIntent = Intent(this, AlarmForegroundService::class.java).apply {
                action = AlarmForegroundService.ACTION_START
                putExtra(AndroidAlarmScheduler.EXTRA_ALARM_ID, alarmId)
                putExtra(AndroidAlarmScheduler.EXTRA_ALARM_LABEL, "Test Alarm")
            }
            startForegroundService(serviceIntent)
        }
        pendingAlarmId.value = alarmId
    }

    private fun extractAlarmId(intent: Intent?): Long? {
        if (intent == null) return null
        val navigateToRing = intent.getBooleanExtra("navigate_to_ring", false)
        val alarmId = intent.getLongExtra(AndroidAlarmScheduler.EXTRA_ALARM_ID, -1L)
        return if (navigateToRing && alarmId != -1L) alarmId else null
    }

    private fun ensureFullScreenIntentPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val nm = getSystemService(NotificationManager::class.java)
            if (!nm.canUseFullScreenIntent()) {
                Log.w("MainActivity", "Full-screen intent NOT granted")
                startActivity(Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                })
            }
        }
    }
}
