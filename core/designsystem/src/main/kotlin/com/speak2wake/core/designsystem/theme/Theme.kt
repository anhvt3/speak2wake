package com.speak2wake.core.designsystem.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary         = OrangeAccent,
    onPrimary       = BackgroundDeep,
    secondary       = GoldAccent,
    onSecondary     = BackgroundDeep,
    background      = BackgroundDeep,
    surface         = BackgroundMid,
    surfaceVariant  = BackgroundLight,
    onBackground    = TextPrimary,
    onSurface       = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error           = RedFail,
    tertiary        = GreenSuccess,
)

@Composable
fun Speak2WakeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Speak2WakeTypography,
        content = content,
    )
}
