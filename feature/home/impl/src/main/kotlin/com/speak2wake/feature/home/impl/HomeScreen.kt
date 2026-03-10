package com.speak2wake.feature.home.impl

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.speak2wake.core.designsystem.component.GlassCard
import com.speak2wake.core.designsystem.strings.LocalStrings
import com.speak2wake.core.designsystem.theme.*
import com.speak2wake.core.model.Alarm
import java.time.format.DateTimeFormatter

@Composable
internal fun HomeRoute(
    onCreateAlarm: () -> Unit,
    onEditAlarm: (Long) -> Unit,
    onSettings: () -> Unit,
    onTestAlarm: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        uiState = uiState,
        onCreateAlarm = onCreateAlarm,
        onEditAlarm = onEditAlarm,
        onSettings = onSettings,
        onTestAlarm = onTestAlarm,
        onToggleAlarm = viewModel::toggleAlarm,
        onDeleteAlarm = viewModel::deleteAlarm,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    uiState: HomeUiState,
    onCreateAlarm: () -> Unit,
    onEditAlarm: (Long) -> Unit,
    onSettings: () -> Unit,
    onTestAlarm: (Long) -> Unit,
    onToggleAlarm: (Long, Boolean) -> Unit,
    onDeleteAlarm: (Long) -> Unit,
) {
    val s = LocalStrings.current

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDeep)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(s.appName, style = MaterialTheme.typography.headlineLarge, color = OrangeAccent, fontWeight = FontWeight.Bold)
                IconButton(onClick = onSettings) {
                    Icon(Icons.Default.Settings, contentDescription = s.settings, tint = TextSecondary)
                }
            }

            when (uiState) {
                HomeUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = OrangeAccent) }
                is HomeUiState.Success -> {
                    if (uiState.alarms.isEmpty()) {
                        EmptyState(onCreateAlarm = onCreateAlarm)
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(uiState.alarms, key = { it.id }) { alarm ->
                                SwipeToDeleteAlarmCard(alarm, { onToggleAlarm(alarm.id, it) }, { onEditAlarm(alarm.id) }, { onDeleteAlarm(alarm.id) }, { onTestAlarm(alarm.id) })
                            }
                            item { Spacer(Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onCreateAlarm,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = OrangeAccent, contentColor = BackgroundDeep, shape = CircleShape,
        ) { Icon(Icons.Default.Add, contentDescription = s.addAlarm, modifier = Modifier.size(28.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteAlarmCard(alarm: Alarm, onToggle: (Boolean) -> Unit, onClick: () -> Unit, onDelete: () -> Unit, onTest: () -> Unit) {
    val s = LocalStrings.current
    val dismissState = rememberSwipeToDismissBoxState(confirmValueChange = { if (it == SwipeToDismissBoxValue.EndToStart) { onDelete(); true } else false })
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(Modifier.fillMaxSize().background(RedFail, RoundedCornerShape(16.dp)).padding(horizontal = 24.dp), contentAlignment = Alignment.CenterEnd) {
                Icon(Icons.Default.Delete, contentDescription = s.delete, tint = TextPrimary)
            }
        },
        enableDismissFromStartToEnd = false,
        content = { AlarmCard(alarm, onToggle, onClick, onTest) },
    )
}

@Composable
private fun AlarmCard(alarm: Alarm, onToggle: (Boolean) -> Unit, onClick: () -> Unit, onTest: () -> Unit) {
    val s = LocalStrings.current
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    GlassCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(alarm.time.format(timeFormatter), fontSize = 36.sp, fontWeight = FontWeight.Bold, color = if (alarm.isEnabled) TextPrimary else TextSecondary)
                if (alarm.label.isNotBlank()) Text(alarm.label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                    if (alarm.challengeEnabled) Chip("🎤 ${alarm.challengeLanguage} ${alarm.vocabularyLevel}")
                    if (alarm.repeatDays.isNotEmpty()) Chip(alarm.repeatDays.joinToString("") { it.name.take(1) })
                }
            }
            IconButton(onClick = onTest, modifier = Modifier.size(36.dp).background(OrangeAccent.copy(alpha = 0.15f), CircleShape)) {
                Icon(Icons.Default.PlayArrow, contentDescription = s.testAlarm, tint = OrangeAccent, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(8.dp))
            Switch(checked = alarm.isEnabled, onCheckedChange = onToggle, colors = SwitchDefaults.colors(checkedThumbColor = BackgroundDeep, checkedTrackColor = OrangeAccent))
        }
    }
}

@Composable
private fun Chip(text: String) {
    Box(Modifier.clip(RoundedCornerShape(8.dp)).background(GlassSurface).padding(horizontal = 8.dp, vertical = 2.dp)) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = OrangeAccent)
    }
}

@Composable
private fun EmptyState(onCreateAlarm: () -> Unit) {
    val s = LocalStrings.current
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(80.dp), tint = GlassBorder)
        Spacer(Modifier.height(16.dp))
        Text(s.noAlarmsYet, style = MaterialTheme.typography.titleLarge, color = TextSecondary)
        Spacer(Modifier.height(8.dp))
        Text(s.tapToCreate, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onCreateAlarm, colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent, contentColor = BackgroundDeep)) {
            Text(s.createAlarm, fontWeight = FontWeight.Bold)
        }
    }
}
