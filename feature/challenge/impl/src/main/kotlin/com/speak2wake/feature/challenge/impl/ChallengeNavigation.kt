package com.speak2wake.feature.challenge.impl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.speak2wake.feature.challenge.api.ChallengeRoute

fun NavGraphBuilder.challengeScreen(onDismissed: () -> Unit) {
    composable<ChallengeRoute> {
        ChallengeRoute(onDismissed = onDismissed)
    }
}
