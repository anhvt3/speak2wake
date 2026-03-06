package com.speak2wake.feature.ring.impl

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.speak2wake.core.alarm.AlarmForegroundService
import com.speak2wake.core.alarm.AlarmScheduler
import com.speak2wake.core.data.repository.AlarmRepository
import com.speak2wake.feature.ring.api.RingRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class RingViewModel
@Inject
constructor(
        savedStateHandle: SavedStateHandle,
        private val alarmRepository: AlarmRepository,
        private val alarmScheduler: AlarmScheduler,
        @ApplicationContext private val context: Context,
) : ViewModel() {

    private val alarmId = savedStateHandle.toRoute<RingRoute>().alarmId

    private val _snoozed = MutableStateFlow(false)
    val snoozed: StateFlow<Boolean> = _snoozed.asStateFlow()

    private val _dismissed = MutableStateFlow(false)
    val dismissed: StateFlow<Boolean> = _dismissed.asStateFlow()

    val uiState: StateFlow<RingUiState> =
            flow {
                        val alarm = alarmRepository.getAlarmById(alarmId)
                        if (alarm != null) emit(RingUiState.Ready(alarm))
                    }
                    .stateIn(
                            viewModelScope,
                            SharingStarted.WhileSubscribed(5_000),
                            RingUiState.Loading
                    )

    fun snooze() {
        viewModelScope.launch {
            val alarm = (uiState.value as? RingUiState.Ready)?.alarm ?: return@launch
            stopAlarmService()
            alarmScheduler.scheduleSnooze(alarm.id, alarm.label, alarm.snoozeMinutes)
            _snoozed.value = true
        }
    }

    /** Dismiss alarm directly (when challenge is disabled). */
    fun dismiss() {
        viewModelScope.launch {
            stopAlarmService()
            alarmRepository.rescheduleAfterFire(alarmId)
            _dismissed.value = true
        }
    }

    fun pauseForChallenge() {
        sendServiceAction(AlarmForegroundService.ACTION_PAUSE)
    }

    private fun stopAlarmService() {
        sendServiceAction(AlarmForegroundService.ACTION_STOP)
    }

    private fun sendServiceAction(action: String) {
        try {
            val intent =
                    Intent(context, AlarmForegroundService::class.java).apply {
                        this.action = action
                    }
            context.startService(intent)
        } catch (e: Exception) {
            Log.e("RingVM", "Failed to send service action: $action", e)
        }
    }
}
