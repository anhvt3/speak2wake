package com.speak2wake.feature.settings.impl

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speak2wake.core.data.KEY_LANGUAGE
import com.speak2wake.core.data.settingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(
        val defaultSnoozeMinutes: Int = 10,
        val defaultVibrate: Boolean = true,
        val defaultChallengeEnabled: Boolean = true,
        val vocabCount: Int = 0,
        val masteredCount: Int = 0,
        val languageCode: String = "en",
)

@HiltViewModel
class SettingsViewModel
@Inject
constructor(
        @ApplicationContext private val context: Context,
        private val vocabularyRepository: com.speak2wake.core.data.repository.VocabularyRepository,
) : ViewModel() {

    companion object {
        val KEY_SNOOZE = intPreferencesKey("default_snooze_minutes")
        val KEY_VIBRATE = booleanPreferencesKey("default_vibrate")
        val KEY_CHALLENGE = booleanPreferencesKey("default_challenge_enabled")
    }

    private val dataStore = context.settingsDataStore

    private val defaultLanguage: String
        get() = if (java.util.Locale.getDefault().language == "vi") "vi" else "en"

    val uiState: StateFlow<SettingsUiState> =
            combine(
                            dataStore.data,
                            vocabularyRepository.getAllWords(),
                            vocabularyRepository.getMasteredCount(),
                    ) { prefs, words, mastered ->
                        SettingsUiState(
                                defaultSnoozeMinutes = prefs[KEY_SNOOZE] ?: 10,
                                defaultVibrate = prefs[KEY_VIBRATE] ?: true,
                                defaultChallengeEnabled = prefs[KEY_CHALLENGE] ?: true,
                                vocabCount = words.size,
                                masteredCount = mastered,
                                languageCode = prefs[KEY_LANGUAGE] ?: defaultLanguage,
                        )
                    }
                    .stateIn(
                            viewModelScope,
                            SharingStarted.WhileSubscribed(5_000),
                            SettingsUiState(languageCode = defaultLanguage)
                    )

    fun setSnoozeMinutes(minutes: Int) {
        viewModelScope.launch { dataStore.edit { it[KEY_SNOOZE] = minutes } }
    }

    fun toggleVibrate() {
        viewModelScope.launch { dataStore.edit { it[KEY_VIBRATE] = !(it[KEY_VIBRATE] ?: true) } }
    }

    fun toggleChallenge() {
        viewModelScope.launch {
            dataStore.edit { it[KEY_CHALLENGE] = !(it[KEY_CHALLENGE] ?: true) }
        }
    }

    fun setLanguage(code: String) {
        viewModelScope.launch { dataStore.edit { it[KEY_LANGUAGE] = code } }
    }
}
