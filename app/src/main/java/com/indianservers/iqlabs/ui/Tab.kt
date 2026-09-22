package com.indianservers.iqlabs.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.ui.graphics.vector.ImageVector

enum class Tab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Rounded.Home),
    Games("Games", Icons.Rounded.SportsEsports),
    Progress("Progress", Icons.Rounded.BarChart),
    Settings("Settings", Icons.Rounded.Settings),
}
