package com.speak2wake.feature.home.api

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable object HomeRoute

fun NavController.navigateToHome() = navigate(HomeRoute) {
    popUpTo(graph.startDestinationId) { inclusive = true }
}
