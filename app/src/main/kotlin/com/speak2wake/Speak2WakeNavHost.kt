package com.speak2wake

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.speak2wake.feature.challenge.api.ChallengeRoute
import com.speak2wake.feature.challenge.impl.challengeScreen
import com.speak2wake.feature.create.api.CreateAlarmRoute
import com.speak2wake.feature.create.api.EditAlarmRoute
import com.speak2wake.feature.create.impl.createAlarmScreen
import com.speak2wake.feature.home.api.HomeRoute
import com.speak2wake.feature.home.api.navigateToHome
import com.speak2wake.feature.home.impl.homeScreen
import com.speak2wake.feature.ring.api.RingRoute
import com.speak2wake.feature.ring.impl.ringScreen
import com.speak2wake.feature.settings.api.SettingsRoute
import com.speak2wake.feature.settings.impl.settingsScreen

@Composable
fun Speak2WakeNavHost(
        pendingAlarmId: Long? = null,
        onAlarmConsumed: () -> Unit = {},
        navController: NavHostController = rememberNavController(),
) {
    // When an alarm fires while the app is already open, navigate to RingRoute
    LaunchedEffect(pendingAlarmId) {
        if (pendingAlarmId != null) {
            navController.navigate(RingRoute(pendingAlarmId)) {
                launchSingleTop = true
            }
            onAlarmConsumed()
        }
    }

    NavHost(
            navController = navController,
            startDestination = HomeRoute,
    ) {
        homeScreen(
                onCreateAlarm = { navController.navigate(CreateAlarmRoute) },
                onEditAlarm = { id -> navController.navigate(EditAlarmRoute(id)) },
                onSettings = { navController.navigate(SettingsRoute) },
                onTestAlarm = { alarmId -> navController.navigate(RingRoute(alarmId)) },
        )
        createAlarmScreen(
                onBack = { navController.popBackStack() },
        )
        ringScreen(
                onStartChallenge = { alarmId -> navController.navigate(ChallengeRoute(alarmId)) },
                onSnoozed = { navController.navigateToHome() },
                onDismissed = { navController.navigateToHome() },
        )
        challengeScreen(
                onDismissed = { navController.navigateToHome() },
        )
        settingsScreen(
                onBack = { navController.popBackStack() },
        )
    }
}
