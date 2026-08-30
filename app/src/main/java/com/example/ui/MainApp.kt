package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.model.StreamStatus
import com.example.ui.components.LivePulseBadge
import com.example.ui.navigation.Screen
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.channel.YouTubeChannelScreen
import com.example.ui.screens.createlive.CreateLiveScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.livecontrol.LiveControlPanelScreen
import com.example.ui.screens.mystreams.MyStreamsScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.LiveGreen
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YTRed
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val currentChannel by viewModel.currentChannel.collectAsState()
    val userStreams by viewModel.userStreams.collectAsState()
    val liveControlState by viewModel.liveControlState.collectAsState()
    val adminStats by viewModel.adminStats.collectAsState()
    val allStreams by viewModel.allStreams.collectAsState()
    val systemLogs by viewModel.systemLogs.collectAsState()

    // Handle one-time toast messages
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isLiveStreaming = liveControlState.status == StreamStatus.LIVE || liveControlState.status == StreamStatus.RECONNECTING
    val isAdmin = currentUser?.role == "ADMIN"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (currentRoute != Screen.Auth.route) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = "Logo",
                                tint = YTRed,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "YT LIVE MANAGER",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    },
                    actions = {
                        if (isLiveStreaming) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable {
                                        navController.navigate(Screen.LiveStudio.route) {
                                            launchSingleTop = true
                                        }
                                    }
                                    .padding(end = 8.dp)
                            ) {
                                LivePulseBadge(customText = "LIVE")
                            }
                        } else if (currentChannel?.isConnected == true) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .background(LiveGreen.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "● Connected",
                                    color = LiveGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkSurface,
                        titleContentColor = TextPrimary
                    )
                )
            }
        },
        bottomBar = {
            if (currentRoute != Screen.Auth.route) {
                val navItems = mutableListOf(
                    Screen.Dashboard,
                    Screen.CreateLive,
                    Screen.LiveStudio,
                    Screen.MyStreams,
                    Screen.YouTubeChannel
                )
                if (isAdmin) {
                    navItems.add(Screen.AdminDashboard)
                }
                navItems.add(Screen.Settings)

                NavigationBar(
                    containerColor = DarkSurface,
                    contentColor = TextPrimary,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    navItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                if (screen == Screen.LiveStudio && isLiveStreaming) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                containerColor = YTRed,
                                                modifier = Modifier.size(8.dp)
                                            )
                                        }
                                    ) {
                                        Icon(
                                            imageVector = screen.icon,
                                            contentDescription = screen.title,
                                            tint = if (isSelected) YTRed else TextSecondary
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.title,
                                        tint = if (isSelected) {
                                            if (screen == Screen.AdminDashboard) PurpleAccent else YTRed
                                        } else TextSecondary
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) TextPrimary else TextMuted
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = if (screen == Screen.AdminDashboard) PurpleAccent.copy(alpha = 0.2f) else YTRed.copy(alpha = 0.2f),
                                selectedIconColor = if (screen == Screen.AdminDashboard) PurpleAccent else YTRed,
                                unselectedIconColor = TextSecondary
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Auth Screen
            composable(Screen.Auth.route) {
                AuthScreen(
                    viewModel = viewModel,
                    onAuthSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    }
                )
            }

            // Dashboard Screen
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    currentUser = currentUser,
                    allUsers = allUsers,
                    channel = currentChannel,
                    userStreams = userStreams,
                    liveControlState = liveControlState,
                    onSwitchUser = { userId -> viewModel.switchUser(userId) },
                    onNavigateToCreateLive = { navController.navigate(Screen.CreateLive.route) },
                    onNavigateToLiveControl = { streamId ->
                        navController.navigate(Screen.LiveStudio.route)
                    },
                    onNavigateToStreams = { navController.navigate(Screen.MyStreams.route) },
                    onNavigateToChannel = { navController.navigate(Screen.YouTubeChannel.route) },
                    onRebroadcast = { stream ->
                        viewModel.rebroadcastStream(stream)
                        navController.navigate(Screen.LiveStudio.route)
                    }
                )
            }

            // Create Live Screen
            composable(Screen.CreateLive.route) {
                CreateLiveScreen(
                    viewModel = viewModel,
                    channel = currentChannel,
                    onStreamStarted = { streamId ->
                        navController.navigate(Screen.LiveStudio.route) {
                            popUpTo(Screen.Dashboard.route)
                        }
                    },
                    onNavigateToChannel = {
                        navController.navigate(Screen.YouTubeChannel.route)
                    }
                )
            }

            // Live Control Studio Screen
            composable(Screen.LiveStudio.route) {
                LiveControlPanelScreen(
                    viewModel = viewModel,
                    state = liveControlState,
                    channel = currentChannel,
                    onNavigateToCreate = {
                        navController.navigate(Screen.CreateLive.route)
                    }
                )
            }

            // My Streams Screen
            composable(Screen.MyStreams.route) {
                MyStreamsScreen(
                    streams = userStreams,
                    onOpenLiveControl = { streamId ->
                        navController.navigate(Screen.LiveStudio.route)
                    },
                    onNavigateToCreate = {
                        navController.navigate(Screen.CreateLive.route)
                    },
                    onRebroadcast = { stream ->
                        viewModel.rebroadcastStream(stream)
                        navController.navigate(Screen.LiveStudio.route)
                    }
                )
            }

            // YouTube Channel Screen
            composable(Screen.YouTubeChannel.route) {
                YouTubeChannelScreen(
                    viewModel = viewModel,
                    channel = currentChannel
                )
            }

            // Admin Dashboard Screen
            composable(Screen.AdminDashboard.route) {
                AdminDashboardScreen(
                    viewModel = viewModel,
                    stats = adminStats,
                    allUsers = allUsers,
                    allStreams = allStreams,
                    logs = systemLogs,
                    currentUser = currentUser
                )
            }

            // Settings Screen
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel,
                    currentUser = currentUser,
                    allUsers = allUsers,
                    onSwitchUser = { userId -> viewModel.switchUser(userId) },
                    onLogout = {
                        viewModel.logout()
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
