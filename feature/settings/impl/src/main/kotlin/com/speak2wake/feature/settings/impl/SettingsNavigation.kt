package com.speak2wake.feature.settings.impl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.speak2wake.feature.settings.api.SettingsRoute

fun NavGraphBuilder.settingsScreen(onBack: () -> Unit) {
    composable<SettingsRoute> { SettingsRoute(onBack = onBack) }
}
