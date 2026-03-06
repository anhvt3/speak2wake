package com.speak2wake.feature.create.impl

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.speak2wake.core.data.repository.AlarmRepository
import com.speak2wake.core.model.Alarm
import com.speak2wake.core.model.VocabularyLevel
import com.speak2wake.feature.create.api.EditAlarmRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime
import javax.inject.Inject

data class AlarmFormState(
    val id: Long = 0,
    val label: String = "",
    val hour: Int = 7,
    val minute: Int = 0,
    val repeatDays: Set<DayOfWeek> = emptySet(),
    val isEnabled: Boolean = true,
    val snoozeMinutes: Int = 10,
    val vibrate: Boolean = true,
    val challengeEnabled: Boolean = true,
    val vocabularyLevel: VocabularyLevel = VocabularyLevel.A1,
    val soundUri: String = "",
    val soundName: String = "Default",
    val alwaysPronounce: Boolean = false,
    val isEditMode: Boolean = false,
    val isSaved: Boolean = false,
)

@HiltViewModel
class CreateAlarmViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val alarmRepository: AlarmRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val editAlarmId: Long? = try {
        savedStateHandle.toRoute<EditAlarmRoute>().alarmId
    } catch (e: Exception) { null }

    private val _form = MutableStateFlow(AlarmFormState(isEditMode = editAlarmId != null))
    val form: StateFlow<AlarmFormState> = _form.asStateFlow()

    init {
        editAlarmId?.let { loadAlarm(it) }
    }

    private fun loadAlarm(id: Long) {
        viewModelScope.launch {
            val alarm = alarmRepository.getAlarmById(id) ?: return@launch
            val soundName = if (alarm.soundUri.isBlank()) {
                "Default"
            } else {
                try {
                    val ringtone = RingtoneManager.getRingtone(context, Uri.parse(alarm.soundUri))
                    ringtone?.getTitle(context) ?: "Custom"
                } catch (e: Exception) {
                    "Custom"
                }
            }
            _form.value = AlarmFormState(
                id = alarm.id,
                label = alarm.label,
                hour = alarm.time.hour,
                minute = alarm.time.minute,
                repeatDays = alarm.repeatDays,
                isEnabled = alarm.isEnabled,
                snoozeMinutes = alarm.snoozeMinutes,
                vibrate = alarm.vibrate,
                challengeEnabled = alarm.challengeEnabled,
                vocabularyLevel = alarm.vocabularyLevel,
                soundUri = alarm.soundUri,
                soundName = soundName,
                alwaysPronounce = alarm.alwaysPronounce,
                isEditMode = true,
            )
        }
    }

    fun onLabelChange(label: String) { _form.update { it.copy(label = label) } }
    fun onTimeChange(hour: Int, minute: Int) { _form.update { it.copy(hour = hour, minute = minute) } }
    fun onRepeatDayToggle(day: DayOfWeek) {
        _form.update {
            val days = it.repeatDays.toMutableSet()
            if (day in days) days.remove(day) else days.add(day)
            it.copy(repeatDays = days)
        }
    }
    fun onSnoozeChange(minutes: Int) { _form.update { it.copy(snoozeMinutes = minutes) } }
    fun onVibrateToggle() { _form.update { it.copy(vibrate = !it.vibrate) } }
    fun onChallengeToggle() { _form.update { it.copy(challengeEnabled = !it.challengeEnabled) } }
    fun onVocabLevelChange(level: VocabularyLevel) { _form.update { it.copy(vocabularyLevel = level) } }
    fun onSoundUriChange(uri: String, name: String) { _form.update { it.copy(soundUri = uri, soundName = name) } }
    fun onAlwaysPronounceToggle() { _form.update { it.copy(alwaysPronounce = !it.alwaysPronounce) } }

    fun save() {
        viewModelScope.launch {
            val f = _form.value
            val alarm = Alarm(
                id = f.id,
                label = f.label,
                time = LocalTime.of(f.hour, f.minute),
                repeatDays = f.repeatDays,
                isEnabled = f.isEnabled,
                snoozeMinutes = f.snoozeMinutes,
                vibrate = f.vibrate,
                challengeEnabled = f.challengeEnabled,
                vocabularyLevel = f.vocabularyLevel,
                soundUri = f.soundUri,
                alwaysPronounce = f.alwaysPronounce,
            )
            if (f.isEditMode) alarmRepository.updateAlarm(alarm)
            else alarmRepository.createAlarm(alarm)
            _form.update { it.copy(isSaved = true) }
        }
    }

    fun delete() {
        viewModelScope.launch {
            if (_form.value.isEditMode) {
                alarmRepository.deleteAlarm(_form.value.id)
                _form.update { it.copy(isSaved = true) }
            }
        }
    }
}
