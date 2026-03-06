package com.speak2wake.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.speak2wake.core.model.Alarm
import com.speak2wake.core.model.VocabularyLevel
import java.time.DayOfWeek
import java.time.LocalTime

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val hourOfDay: Int,
    val minute: Int,
    val repeatDays: String, // comma-separated ints e.g. "1,2,5"
    val isEnabled: Boolean,
    val snoozeMinutes: Int,
    val vibrate: Boolean,
    val challengeEnabled: Boolean,
    val vocabularyLevel: String,
    val soundUri: String,
    val alwaysPronounce: Boolean = false,
)

fun AlarmEntity.toModel() = Alarm(
    id = id,
    label = label,
    time = LocalTime.of(hourOfDay, minute),
    repeatDays = repeatDays.split(",").filter { it.isNotBlank() }
        .map { DayOfWeek.of(it.toInt()) }.toSet(),
    isEnabled = isEnabled,
    snoozeMinutes = snoozeMinutes,
    vibrate = vibrate,
    challengeEnabled = challengeEnabled,
    vocabularyLevel = VocabularyLevel.valueOf(vocabularyLevel),
    soundUri = soundUri,
    alwaysPronounce = alwaysPronounce,
)

fun Alarm.toEntity() = AlarmEntity(
    id = id,
    label = label,
    hourOfDay = time.hour,
    minute = time.minute,
    repeatDays = repeatDays.joinToString(",") { it.value.toString() },
    isEnabled = isEnabled,
    snoozeMinutes = snoozeMinutes,
    vibrate = vibrate,
    challengeEnabled = challengeEnabled,
    vocabularyLevel = vocabularyLevel.name,
    soundUri = soundUri,
    alwaysPronounce = alwaysPronounce,
)
