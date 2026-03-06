package com.speak2wake.core.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.speak2wake.core.database.AlarmDao
import com.speak2wake.core.database.toModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var alarmDao: AlarmDao
    @Inject lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON"
        ) return

        Log.d("BootReceiver", "Device rebooted — re-registering alarms")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val alarms = alarmDao.getAlarms().first()
                alarms.filter { it.isEnabled }.forEach { entity ->
                    alarmScheduler.schedule(entity.toModel())
                }
                Log.d("BootReceiver", "Re-registered ${alarms.count { it.isEnabled }} alarms")
            } finally {
                pendingResult.finish()
            }
        }
    }
}
