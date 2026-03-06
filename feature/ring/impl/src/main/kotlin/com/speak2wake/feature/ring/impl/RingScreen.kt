package com.speak2wake.feature.ring.impl

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.speak2wake.core.designsystem.strings.LocalStrings
import com.speak2wake.core.designsystem.theme.*
import com.speak2wake.core.model.Alarm
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
internal fun RingRoute(
        onStartChallenge: (Long) -> Unit,
        onSnoozed: () -> Unit = {},
        onDismissed: () -> Unit = {},
        viewModel: RingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snoozed by viewModel.snoozed.collectAsStateWithLifecycle()
    val dismissed by viewModel.dismissed.collectAsStateWithLifecycle()

    LaunchedEffect(snoozed) { if (snoozed) onSnoozed() }

    LaunchedEffect(dismissed) { if (dismissed) onDismissed() }

    RingScreen(
            uiState = uiState,
            onSnooze = viewModel::snooze,
            onDismiss = viewModel::dismiss,
            onStartChallenge = {
                viewModel.pauseForChallenge()
                if (uiState is RingUiState.Ready) {
                    onStartChallenge((uiState as RingUiState.Ready).alarm.id)
                }
            },
    )
}

@Composable
internal fun RingScreen(
        uiState: RingUiState,
        onSnooze: () -> Unit,
        onDismiss: () -> Unit,
        onStartChallenge: () -> Unit,
) {
    val gradient =
            Brush.radialGradient(
                    colors = listOf(BackgroundMid, BackgroundDeep, BackgroundDeep),
                    radius = 1000f,
            )

    Box(
            modifier = Modifier.fillMaxSize().background(gradient),
            contentAlignment = Alignment.Center,
    ) {
        when (uiState) {
            RingUiState.Loading -> CircularProgressIndicator(color = OrangeAccent)
            is RingUiState.Ready ->
                    RingContent(
                            alarm = uiState.alarm,
                            onSnooze = onSnooze,
                            onDismiss = onDismiss,
                            onStartChallenge = onStartChallenge,
                    )
        }
    }
}

@Composable
private fun RingContent(
        alarm: Alarm,
        onSnooze: () -> Unit,
        onDismiss: () -> Unit,
        onStartChallenge: () -> Unit,
) {
    val s = LocalStrings.current
    val infiniteTransition = rememberInfiniteTransition(label = "ring_pulse")
    val scale by
            infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.15f,
                    animationSpec =
                            infiniteRepeatable(
                                    animation = tween(800, easing = EaseInOut),
                                    repeatMode = RepeatMode.Reverse,
                            ),
                    label = "pulse_scale",
            )

    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp),
    ) {
        // Pulsing ring icon
        Box(
                modifier = Modifier.size(120.dp).scale(scale).background(GlassSurface, CircleShape),
                contentAlignment = Alignment.Center,
        ) { Text("⏰", fontSize = 56.sp) }

        // Clock (updates every second)
        var currentTime by remember { mutableStateOf(LocalTime.now()) }
        LaunchedEffect(Unit) {
            while (true) {
                currentTime = LocalTime.now()
                kotlinx.coroutines.delay(1_000)
            }
        }
        Text(
                text = currentTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
        )

        // Label
        if (alarm.label.isNotBlank()) {
            Text(
                    text = alarm.label,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextSecondary,
            )
        }

        Spacer(Modifier.height(16.dp))

        if (alarm.challengeEnabled) {
            // Challenge button
            Button(
                    onClick = onStartChallenge,
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    colors =
                            ButtonDefaults.buttonColors(
                                    containerColor = OrangeAccent,
                                    contentColor = BackgroundDeep,
                            ),
                    shape = MaterialTheme.shapes.large,
            ) {
                Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text(s.startChallenge, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            // Direct dismiss button (no challenge required)
            Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    colors =
                            ButtonDefaults.buttonColors(
                                    containerColor = GreenSuccess,
                                    contentColor = BackgroundDeep,
                            ),
                    shape = MaterialTheme.shapes.large,
            ) {
                Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(s.dismissAlarm, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Snooze button
        OutlinedButton(
                onClick = onSnooze,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
        ) {
            Icon(Icons.Default.Snooze, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(s.snoozeFormat.format(alarm.snoozeMinutes), fontSize = 16.sp)
        }
    }
}
