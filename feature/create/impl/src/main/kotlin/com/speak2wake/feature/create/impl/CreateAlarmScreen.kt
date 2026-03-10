package com.speak2wake.feature.create.impl

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.speak2wake.core.designsystem.strings.LocalStrings
import com.speak2wake.core.designsystem.theme.*
import com.speak2wake.core.model.ChallengeLanguage
import com.speak2wake.core.model.VocabularyLevel
import java.time.DayOfWeek

@Composable
internal fun CreateAlarmRoute(
    onBack: () -> Unit,
    viewModel: CreateAlarmViewModel = hiltViewModel(),
) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    LaunchedEffect(form.isSaved) { if (form.isSaved) onBack() }

    val context = LocalContext.current
    val s = LocalStrings.current

    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
        if (uri != null) {
            try { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: SecurityException) {}
            val ringtone = RingtoneManager.getRingtone(context, uri)
            val name = ringtone?.getTitle(context) ?: "Custom"
            viewModel.onSoundUriChange(uri.toString(), name)
        }
    }

    CreateAlarmScreen(
        form = form,
        onBack = onBack,
        onLabelChange = viewModel::onLabelChange,
        onTimeChange = viewModel::onTimeChange,
        onRepeatDayToggle = viewModel::onRepeatDayToggle,
        onSnoozeChange = viewModel::onSnoozeChange,
        onVibrateToggle = viewModel::onVibrateToggle,
        onChallengeToggle = viewModel::onChallengeToggle,
        onVocabLevelChange = viewModel::onVocabLevelChange,
        onAlwaysPronounceToggle = viewModel::onAlwaysPronounceToggle,
        onWordCountChange = viewModel::onWordCountChange,
        onChallengeLanguageChange = viewModel::onChallengeLanguageChange,
        onPickSound = {
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, s.selectAlarmSound)
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                if (form.soundUri.isNotBlank()) putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(form.soundUri))
            }
            ringtonePickerLauncher.launch(intent)
        },
        onClearSound = { viewModel.onSoundUriChange("", "Default") },
        onSave = viewModel::save,
        onDelete = viewModel::delete,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun CreateAlarmScreen(
    form: AlarmFormState,
    onBack: () -> Unit,
    onLabelChange: (String) -> Unit,
    onTimeChange: (Int, Int) -> Unit,
    onRepeatDayToggle: (DayOfWeek) -> Unit,
    onSnoozeChange: (Int) -> Unit,
    onVibrateToggle: () -> Unit,
    onChallengeToggle: () -> Unit,
    onVocabLevelChange: (VocabularyLevel) -> Unit,
    onAlwaysPronounceToggle: () -> Unit,
    onWordCountChange: (Int) -> Unit,
    onChallengeLanguageChange: (ChallengeLanguage) -> Unit,
    onPickSound: () -> Unit,
    onClearSound: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
) {
    val s = LocalStrings.current

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDeep)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back, tint = TextPrimary) }
                Text(
                    text = if (form.isEditMode) s.editAlarm else s.newAlarm,
                    style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold,
                )
                if (form.isEditMode) {
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = s.delete, tint = RedFail) }
                } else { Spacer(Modifier.size(48.dp)) }
            }

            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Time picker
                SectionCard {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("%02d:%02d".format(form.hour, form.minute), fontSize = 64.sp, fontWeight = FontWeight.Bold, color = OrangeAccent)
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(s.hour, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                NumberPicker(form.hour, 0..23) { onTimeChange(it, form.minute) }
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(s.minute, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                NumberPicker(form.minute, 0..59) { onTimeChange(form.hour, it) }
                            }
                        }
                    }
                }

                // Label
                SectionCard {
                    OutlinedTextField(
                        value = form.label, onValueChange = onLabelChange,
                        label = { Text(s.labelOptional, color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangeAccent, unfocusedBorderColor = GlassBorder, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                        singleLine = true,
                    )
                }

                // Repeat days
                SectionCard {
                    Text(s.repeat, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        DayOfWeek.entries.forEach { day ->
                            val selected = day in form.repeatDays
                            Box(
                                modifier = Modifier.size(40.dp).background(if (selected) OrangeAccent else GlassSurface, RoundedCornerShape(8.dp)).clickable { onRepeatDayToggle(day) },
                                contentAlignment = Alignment.Center,
                            ) { Text(day.name.take(1), fontWeight = FontWeight.Bold, color = if (selected) BackgroundDeep else TextSecondary) }
                        }
                    }
                }

                // Snooze
                SectionCard {
                    Text(s.snooze, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(s.duration, style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            NumberPicker(form.snoozeMinutes, 1..30, onSnoozeChange)
                            Text(s.min, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                    }
                }

                // Alarm Sound
                SectionCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(s.alarmSound, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Spacer(Modifier.height(4.dp))
                            Text(form.soundName, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                        Row {
                            if (form.soundUri.isNotBlank()) TextButton(onClick = onClearSound) { Text(s.reset, color = RedFail) }
                            TextButton(onClick = onPickSound) { Text(s.change, color = OrangeAccent, fontWeight = FontWeight.Bold) }
                        }
                    }
                }

                // Toggles
                SectionCard {
                    ToggleRow(s.vibrate, form.vibrate, onVibrateToggle)
                    HorizontalDivider(color = GlassBorder, modifier = Modifier.padding(vertical = 4.dp))
                    ToggleRow(s.voiceChallenge, form.challengeEnabled, onChallengeToggle)
                }

                // Challenge settings
                if (form.challengeEnabled) {
                    // Challenge language selector
                    SectionCard {
                        Text(s.challengeLanguageLabel, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Spacer(Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = form.challengeLanguage == ChallengeLanguage.DE,
                                onClick = { onChallengeLanguageChange(ChallengeLanguage.DE) },
                                label = { Text(s.germanChallenge) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = OrangeAccent, selectedLabelColor = BackgroundDeep, labelColor = TextSecondary),
                            )
                            FilterChip(
                                selected = form.challengeLanguage == ChallengeLanguage.VI,
                                onClick = { onChallengeLanguageChange(ChallengeLanguage.VI) },
                                label = { Text(s.vietnameseChallenge) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = OrangeAccent, selectedLabelColor = BackgroundDeep, labelColor = TextSecondary),
                            )
                        }
                    }

                    // Vocab level
                    SectionCard {
                        Text(s.vocabularyLevel, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Spacer(Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            VocabularyLevel.entries.forEach { level ->
                                FilterChip(
                                    selected = form.vocabularyLevel == level,
                                    onClick = { onVocabLevelChange(level) },
                                    label = { Text(level.name) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = OrangeAccent, selectedLabelColor = BackgroundDeep, labelColor = TextSecondary),
                                )
                            }
                        }
                    }

                    SectionCard {
                        ToggleRow(s.alwaysPronounce, form.alwaysPronounce, onAlwaysPronounceToggle)
                    }

                    SectionCard {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(s.wordCount, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                NumberPicker(form.challengeWordCount, 1..10, onWordCountChange)
                                Text(s.wordCountWords, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent, contentColor = BackgroundDeep),
                shape = RoundedCornerShape(16.dp),
            ) { Text(if (form.isEditMode) s.updateAlarm else s.saveAlarm, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
        }
    }
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().background(GlassSurface, RoundedCornerShape(16.dp)).border(1.dp, GlassBorder, RoundedCornerShape(16.dp)).padding(16.dp), content = content)
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onToggle: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
        Switch(checked = checked, onCheckedChange = { onToggle() }, colors = SwitchDefaults.colors(checkedTrackColor = OrangeAccent, checkedThumbColor = BackgroundDeep))
    }
}

@Composable
private fun NumberPicker(value: Int, range: IntRange, onValueChange: (Int) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = { if (value > range.first) onValueChange(value - 1) else onValueChange(range.last) }, colors = ButtonDefaults.textButtonColors(contentColor = OrangeAccent)) { Text("−", fontSize = 24.sp) }
        Text(
            "%02d".format(value), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary,
            modifier = Modifier.widthIn(min = 48.dp).clickable { showDialog = true },
        )
        TextButton(onClick = { if (value < range.last) onValueChange(value + 1) else onValueChange(range.first) }, colors = ButtonDefaults.textButtonColors(contentColor = OrangeAccent)) { Text("+", fontSize = 24.sp) }
    }
    if (showDialog) {
        NumberInputDialog(
            currentValue = value,
            range = range,
            onConfirm = { onValueChange(it); showDialog = false },
            onDismiss = { showDialog = false },
        )
    }
}

@Composable
private fun NumberInputDialog(currentValue: Int, range: IntRange, onConfirm: (Int) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf(currentValue.toString()) }
    val focusRequester = remember { FocusRequester() }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BackgroundMid,
        title = { Text("Enter value (${range.first}–${range.last})", color = TextPrimary) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.filter { c -> c.isDigit() } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangeAccent, unfocusedBorderColor = GlassBorder, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
            )
            LaunchedEffect(Unit) { focusRequester.requestFocus() }
        },
        confirmButton = {
            TextButton(onClick = {
                val num = text.toIntOrNull()?.coerceIn(range) ?: currentValue
                onConfirm(num)
            }) { Text("OK", color = OrangeAccent, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        },
    )
}
