package com.speak2wake.feature.home.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speak2wake.core.data.repository.AlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
) : ViewModel() {

    val uiState = alarmRepository.getAlarms()
        .map { HomeUiState.Success(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState.Loading)

    fun toggleAlarm(alarmId: Long, enabled: Boolean) {
        viewModelScope.launch { alarmRepository.setAlarmEnabled(alarmId, enabled) }
    }

    fun deleteAlarm(alarmId: Long) {
        viewModelScope.launch { alarmRepository.deleteAlarm(alarmId) }
    }
}
