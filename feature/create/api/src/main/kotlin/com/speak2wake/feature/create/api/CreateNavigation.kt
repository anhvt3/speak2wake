package com.speak2wake.feature.create.api
import kotlinx.serialization.Serializable
@Serializable object CreateAlarmRoute
@Serializable data class EditAlarmRoute(val alarmId: Long)
