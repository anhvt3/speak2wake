package com.speak2wake.feature.home.impl

import com.speak2wake.core.model.Alarm

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val alarms: List<Alarm>) : HomeUiState
}
