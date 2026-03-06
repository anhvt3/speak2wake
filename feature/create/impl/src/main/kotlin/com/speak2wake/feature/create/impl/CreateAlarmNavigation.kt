package com.speak2wake.feature.create.impl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.speak2wake.feature.create.api.CreateAlarmRoute
import com.speak2wake.feature.create.api.EditAlarmRoute

fun NavGraphBuilder.createAlarmScreen(onBack: () -> Unit) {
    composable<CreateAlarmRoute> { CreateAlarmRoute(onBack = onBack) }
    composable<EditAlarmRoute> { CreateAlarmRoute(onBack = onBack) }
}
