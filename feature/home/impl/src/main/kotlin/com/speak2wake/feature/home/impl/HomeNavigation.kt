package com.speak2wake.feature.home.impl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.speak2wake.feature.home.api.HomeRoute

fun NavGraphBuilder.homeScreen(
    onCreateAlarm: () -> Unit,
    onEditAlarm: (Long) -> Unit,
    onSettings: () -> Unit,
    onTestAlarm: (Long) -> Unit = {},
) {
    composable<HomeRoute> {
        HomeRoute(
            onCreateAlarm = onCreateAlarm,
            onEditAlarm = onEditAlarm,
            onSettings = onSettings,
            onTestAlarm = onTestAlarm,
        )
    }
}
