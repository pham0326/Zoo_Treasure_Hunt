package com.pham0326.flinders.zootreasurehunt.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

@Serializable
object HomeDestination

@Serializable
object SettingsDestination

@Serializable
object AboutDestination

sealed class BottomNavItem(
    val label: String,
    val icon: ImageVector
) {
    data object Home : BottomNavItem(
        label = "Home",
        icon = Icons.Filled.Home
    )

    data object Settings : BottomNavItem(
        label = "Settings",
        icon = Icons.Filled.Settings
    )

    data object About : BottomNavItem(
        label = "About",
        icon = Icons.Filled.Info
    )
}