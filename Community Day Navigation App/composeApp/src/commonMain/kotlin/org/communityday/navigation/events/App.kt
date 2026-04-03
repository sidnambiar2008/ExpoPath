package org.communityday.navigation.events

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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

import communitydaynavigationapp.composeapp.generated.resources.Res

sealed class Screen {
    object Welcome : Screen()
    object EventList : Screen()
    data class EventDetail(val event: Event) : Screen()
}

@Composable
fun App() {
    val NavyBlue = androidx.compose.ui.graphics.Color(0xFF000033)
    val Silver = androidx.compose.ui.graphics.Color(0xFFC0C0C0)
    val ActionOrange = androidx.compose.ui.graphics.Color(0xFFFF8C00)
    val Turquoise = androidx.compose.ui.graphics.Color(0xFF40E0D0)
    
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Welcome) }
    var isCodeAccepted by remember { mutableStateOf(false) }
    
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            when (currentScreen) {
                is Screen.Welcome -> WelcomeScreen(
                    onGetStarted = { currentScreen = Screen.EventList },
                    NavyBlue = NavyBlue,
                    Silver = Silver,
                    ActionOrange = ActionOrange,
                    Turquoise = Turquoise
                )
                
                is Screen.EventList -> EventListScreen(
                    onEventClick = { event -> 
                        currentScreen = Screen.EventDetail(event) 
                    }
                )
                
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