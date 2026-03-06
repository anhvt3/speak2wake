package com.speak2wake.core.model

import java.time.DayOfWeek
import java.time.LocalTime

data class Alarm(
    val id: Long = 0,
    val label: String = "",
    val time: LocalTime = LocalTime.of(7, 0),
    val repeatDays: Set<DayOfWeek> = emptySet(),
    val isEnabled: Boolean = true,
    val snoozeMinutes: Int = 10,
    val vibrate: Boolean = true,
    val challengeEnabled: Boolean = true,
    val vocabularyLevel: VocabularyLevel = VocabularyLevel.A1,
    val soundUri: String = "",
    val alwaysPronounce: Boolean = false,
)

enum class VocabularyLevel { A1, A2, B1, B2, MIXED }
