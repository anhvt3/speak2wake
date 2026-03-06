package com.speak2wake.feature.settings.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.speak2wake.core.designsystem.strings.LocalStrings
import com.speak2wake.core.designsystem.theme.*

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreen(
        uiState = uiState,
        onBack = onBack,
        onSnoozeChange = viewModel::setSnoozeMinutes,
        onVibrateToggle = viewModel::toggleVibrate,
        onChallengeToggle = viewModel::toggleChallenge,
        onLanguageChange = viewModel::setLanguage,
    )
}

@Composable
internal fun SettingsScreen(
    uiState: SettingsUiState,
    onBack: () -> Unit,
    onSnoozeChange: (Int) -> Unit,
    onVibrateToggle: () -> Unit,
    onChallengeToggle: () -> Unit,
    onLanguageChange: (String) -> Unit,
) {
    val s = LocalStrings.current

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDeep)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, s.back, tint = TextPrimary)
                }
                Text(
                    s.settingsTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Language
                SectionHeader(s.appLanguage)
                SettingsSection {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = uiState.languageCode == "vi",
                            onClick = { onLanguageChange("vi") },
                            label = { Text(s.vietnamese) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OrangeAccent,
                                selectedLabelColor = BackgroundDeep,
                                labelColor = TextSecondary,
                            ),
                        )
                        FilterChip(
                            selected = uiState.languageCode == "en",
                            onClick = { onLanguageChange("en") },
                            label = { Text(s.english) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OrangeAccent,
                                selectedLabelColor = BackgroundDeep,
                                labelColor = TextSecondary,
                            ),
                        )
                    }
                }

                // Alarm Defaults
                SectionHeader(s.alarmDefaults)
                SettingsSection {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(s.snoozeDuration, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                            Text(s.defaultForNew, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(
                                onClick = { if (uiState.defaultSnoozeMinutes > 1) onSnoozeChange(uiState.defaultSnoozeMinutes - 1) },
                                colors = ButtonDefaults.textButtonColors(contentColor = OrangeAccent),
                            ) { Text("-", fontSize = 20.sp) }
                            Text(
                                "${uiState.defaultSnoozeMinutes}m",
                                fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OrangeAccent,
                            )
                            TextButton(
                                onClick = { if (uiState.defaultSnoozeMinutes < 30) onSnoozeChange(uiState.defaultSnoozeMinutes + 1) },
                                colors = ButtonDefaults.textButtonColors(contentColor = OrangeAccent),
                            ) { Text("+", fontSize = 20.sp) }
                        }
                    }
                    HorizontalDivider(color = GlassBorder, modifier = Modifier.padding(vertical = 4.dp))
                    SettingsToggle(s.vibrate, s.vibrateWhenRings, uiState.defaultVibrate, onVibrateToggle)
                    HorizontalDivider(color = GlassBorder, modifier = Modifier.padding(vertical = 4.dp))
                    SettingsToggle(s.germanChallenge, s.requirePronunciation, uiState.defaultChallengeEnabled, onChallengeToggle)
                }

                // Vocabulary
                SectionHeader(s.vocabulary)
                SettingsSection {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        StatItem(value = "${uiState.vocabCount}", label = s.totalWords)
                        StatItem(value = "${uiState.masteredCount}", label = s.mastered)
                    }
                }

                // About
                SectionHeader(s.about)
                SettingsSection {
                    InfoRow(s.version, "2.4")
                    HorizontalDivider(color = GlassBorder, modifier = Modifier.padding(vertical = 4.dp))
                    InfoRow(s.app, "Speak2Wake")
                    HorizontalDivider(color = GlassBorder, modifier = Modifier.padding(vertical = 4.dp))
                    InfoRow(s.language, "German (de-DE)")
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, color = OrangeAccent,
        fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
}

@Composable
private fun SettingsSection(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(GlassSurface, RoundedCornerShape(16.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        content = content,
    )
}

@Composable
private fun SettingsToggle(title: String, subtitle: String, checked: Boolean, onToggle: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        Switch(checked = checked, onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(checkedTrackColor = OrangeAccent, checkedThumbColor = BackgroundDeep))
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
        Text(value, style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
    }
}
