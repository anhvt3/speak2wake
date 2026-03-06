package com.speak2wake.feature.ring.impl

import com.speak2wake.core.model.Alarm

sealed interface RingUiState {
    data object Loading : RingUiState
    data class Ready(val alarm: Alarm) : RingUiState
}
