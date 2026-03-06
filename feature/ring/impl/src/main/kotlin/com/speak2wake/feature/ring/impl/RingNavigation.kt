package com.speak2wake.feature.ring.impl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.speak2wake.feature.ring.api.RingRoute

fun NavGraphBuilder.ringScreen(
        onStartChallenge: (Long) -> Unit,
        onSnoozed: () -> Unit,
        onDismissed: () -> Unit = {},
) {
    composable<RingRoute> {
        RingRoute(
                onStartChallenge = onStartChallenge,
                onSnoozed = onSnoozed,
                onDismissed = onDismissed,
        )
    }
}
