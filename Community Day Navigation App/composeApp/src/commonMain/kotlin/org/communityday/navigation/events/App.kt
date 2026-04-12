package org.communityday.navigation.events

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBar

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.communityday.navigation.events.data.Event
import org.communityday.navigation.events.ui.screens.EventListScreen
import org.communityday.navigation.events.ui.screens.EventDetailScreen
import kotlinx.serialization.Serializable


import communitydaynavigationapp.composeapp.generated.resources.Res
import org.communityday.navigation.events.ui.screens.BoothListScreen

sealed interface Screen {
    @kotlinx.serialization.Serializable
    data object Welcome : Screen
    @kotlinx.serialization.Serializable
    data object EventList : Screen

    @Serializable data object BoothList: Screen
    @Serializable data object Map: Screen
    @Serializable data object Profile: Screen
    @Serializable data class EventDetail(val event: Event) : Screen
}

@Composable
fun BottomNavigationBar(
    currentScreen: Screen,
    onTabSelected: (Screen) -> Unit
) {
    androidx.compose.material3.NavigationBar {
        // Tab 1: Events
        androidx.compose.material3.NavigationBarItem(
            selected = currentScreen is Screen.EventList,
            onClick = { onTabSelected(Screen.EventList) },
            label = { Text("Events") },
            icon = { androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.DateRange, contentDescription = null) }
        )

        // Tab 2: Booths
        androidx.compose.material3.NavigationBarItem(
            selected = currentScreen is Screen.BoothList,
            onClick = { onTabSelected(Screen.BoothList) },
            label = { Text("Booths") },
            icon = { androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.List, contentDescription = null) }
        )

        // Tab 3: Map
        androidx.compose.material3.NavigationBarItem(
            selected = currentScreen is Screen.Map,
            onClick = { onTabSelected(Screen.Map) },
            label = { Text("Map") },
            icon = { androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.LocationOn, contentDescription = null) }
        )

        // Tab 4: Profile
        androidx.compose.material3.NavigationBarItem(
            selected = currentScreen is Screen.Profile,
            onClick = { onTabSelected(Screen.Profile) },
            label = { Text("Profile") },
            icon = { androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.AccountCircle, contentDescription = null) }
        )
    }
}

@Composable
fun App() {
    val NavyBlue = androidx.compose.ui.graphics.Color(0xFF000033)
    val Silver = androidx.compose.ui.graphics.Color(0xFFC0C0C0)
    val ActionOrange = androidx.compose.ui.graphics.Color(0xFFFF8C00)
    val Turquoise = androidx.compose.ui.graphics.Color(0xFF40E0D0)
    
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Welcome) }

    MaterialTheme {
        // 3. Logic to hide the bar on specific screens
        val showBottomBar = currentScreen !is Screen.Welcome && currentScreen !is Screen.EventDetail

        androidx.compose.material3.Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    BottomNavigationBar(
                        currentScreen = currentScreen,
                        onTabSelected = { selectedScreen: Screen -> currentScreen = selectedScreen }
                    )
                }
            }
        ) { paddingValues ->
            // 4. Content Area
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Respects the space taken by the bottom bar
            ) {
                when (currentScreen) {
                    is Screen.Welcome -> WelcomeScreen(
                        onGetStarted = { currentScreen = Screen.EventList },
                        NavyBlue = NavyBlue,
                        Silver = Silver,
                        ActionOrange = ActionOrange,
                        Turquoise = Turquoise
                    )

                    is Screen.EventList -> EventListScreen(
                        onEventClick = { event -> currentScreen = Screen.EventDetail(event) }
                    )

                    is Screen.BoothList -> {
                        // We'll create the BoothListScreen composable next
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Booth List Coming Soon")
                        }
                    }

                    is Screen.Map -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Map Screen Coming Soon")
                        }
                    }
                    is Screen.Profile -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Profile Screen Coming Soon")
                        }
                    }

                    is Screen.EventDetail -> {
                        val event = (currentScreen as Screen.EventDetail).event
                        EventDetailScreen(
                            event = event,
                            onBackClick = { currentScreen = Screen.EventList }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    NavyBlue: androidx.compose.ui.graphics.Color,
    Silver: androidx.compose.ui.graphics.Color,
    ActionOrange: androidx.compose.ui.graphics.Color,
    Turquoise: androidx.compose.ui.graphics.Color
) {
    var showContent by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .background(NavyBlue)
            .safeContentPadding()
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo/Image - using a simple color placeholder for now
        AnimatedVisibility(showContent) {
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Logo", color = androidx.compose.ui.graphics.Color.White)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Title
        AnimatedVisibility(showContent) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
               // Text(
                 //   text = "Community Day",
                 //   color = Silver,
                 //   fontSize = 32.sp,
                 //   fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
               // )
                Text(
                    text = "Navigation App",
                    color = Turquoise,
                    fontSize = 24.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Description
        AnimatedVisibility(showContent) {
            Text(
                text = "Navigate through conference events,\nconnect with the community,\nand make the most of your day.",
                color = Silver.copy(alpha = 0.8f),
                fontSize = 16.sp,
                lineHeight = 24.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Get Started Button
        AnimatedVisibility(showContent) {
            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = ActionOrange
                )
            ) {
                Text(
                    text = "Get Started",
                    color = androidx.compose.ui.graphics.Color.White,
                    fontSize = 18.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }
        }
        
        // Trigger animation
        LaunchedEffect(Unit) {
            showContent = true
        }
    }
}