package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Auth : Screen("auth", "Sign In", Icons.Default.Dashboard)
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object CreateLive : Screen("create_live", "Create Live", Icons.Default.AddCircle)
    object LiveStudio : Screen("live_studio", "Live Studio", Icons.Default.Podcasts)
    object MyStreams : Screen("my_streams", "My Streams", Icons.Default.VideoLibrary)
    object YouTubeChannel : Screen("youtube_channel", "Channel", Icons.Default.Tv)
    object AdminDashboard : Screen("admin_dashboard", "Admin", Icons.Default.AdminPanelSettings)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}
