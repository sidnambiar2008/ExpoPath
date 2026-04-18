package org.communityday.navigation.events

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
//import androidx.activity.compose.BackHandler
import communitydaynavigationapp.composeapp.generated.resources.Res
import communitydaynavigationapp.composeapp.generated.resources.ic_schedule
import communitydaynavigationapp.composeapp.generated.resources.ic_map
import communitydaynavigationapp.composeapp.generated.resources.ic_person
import communitydaynavigationapp.composeapp.generated.resources.ic_store
import communitydaynavigationapp.composeapp.generated.resources.ic_settings
import org.communityday.navigation.events.data.Booth
import org.communityday.navigation.events.ui.screens.BoothDetailScreen
import org.communityday.navigation.events.ui.screens.BoothListScreen
import org.communityday.navigation.events.ui.screens.SettingsScreen

sealed interface Screen {
    @kotlinx.serialization.Serializable
    data object Welcome : Screen
    @kotlinx.serialization.Serializable
    data object EventList : Screen

    @Serializable data object BoothList: Screen
    @Serializable data class BoothDetail(val booth: Booth):Screen{

    }
    @Serializable data object Map: Screen
    @Serializable data object Profile: Screen
    @Serializable data object JoinConference : Screen
    @Serializable data class EventDetail(val event: Event) : Screen
    @Serializable data object Settings: Screen
}


@Composable
fun BottomNavigationBar(
    currentScreen: Screen,
    onTabSelected: (Screen) -> Unit
) {
    NavigationBar {
        // Tab 1: Events
        NavigationBarItem(
            selected = currentScreen is Screen.EventList,
            onClick = { onTabSelected(Screen.EventList) },
            label = { Text("Events") },
            icon = { Icon(
                painter = painterResource(Res.drawable.ic_schedule),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            ) }
        )

        // Tab 2: Booths
        NavigationBarItem(
            selected = currentScreen is Screen.BoothList,
            onClick = { onTabSelected(Screen.BoothList) },
            label = { Text("Booths") },
            icon = { Icon(
                painter = painterResource(Res.drawable.ic_store),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            ) }
        )

        // Tab 3: Map
        NavigationBarItem(
            selected = currentScreen is Screen.Map,
            onClick = { onTabSelected(Screen.Map) },
            label = { Text("Map") },
            icon = { Icon(
                painter = painterResource(Res.drawable.ic_map),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            ) }
        )

        // Tab 4: Profile
        NavigationBarItem(
            selected = currentScreen is Screen.Profile,
            onClick = { onTabSelected(Screen.Profile) },
            label = { Text("Profile") },
            icon = { Icon(
                painter = painterResource(Res.drawable.ic_person),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            ) }
        )
        // Tab 5: Settings
        NavigationBarItem(
            selected = currentScreen is Screen.Settings,
            onClick = { onTabSelected(Screen.Settings) },
            label = { Text("Settings") },
            icon = { Icon(
                painter = painterResource(Res.drawable.ic_settings),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            ) }
        )
    }
}

@Composable
fun App() {
    val NavyBlue = Color(0xFF000033)
    val Silver = Color(0xFFC0C0C0)
    val ActionOrange = Color(0xFFFF8C00)
    val Turquoise = Color(0xFF40E0D0)

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Welcome) }
    var isJoined by remember {mutableStateOf(false)}
    var activeCode by remember { mutableStateOf("") }

    MaterialTheme {
        // 3. Logic to hide the bar on specific screens
        val showBottomBar = isJoined &&
                currentScreen !is Screen.Welcome &&
                currentScreen !is Screen.EventDetail &&
                currentScreen !is Screen.JoinConference

       // BackHandler(enabled = currentScreen !is Screen.Welcome && currentScreen !is Screen.EventList) {
            // If they are in a Detail screen or another tab, send them back to the Event List
         //   currentScreen = Screen.EventList
       // }
        Scaffold(
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
                        onGetStarted = {
                            currentScreen = Screen.JoinConference },
                        NavyBlue = NavyBlue,
                        Silver = Silver,
                        ActionOrange = ActionOrange,
                        Turquoise = Turquoise,
                    )

                    is Screen.JoinConference -> JoinConferenceScreen(
                        NavyBlue = NavyBlue,
                        Silver = Silver,
                        Turquoise = Turquoise,
                        onConferenceJoined = { code ->
                            activeCode = code.trim()
                            isJoined = true
                            currentScreen = Screen.EventList
                        }
                    )

                    is Screen.EventList -> EventListScreen(
                        confCode = activeCode,
                        onEventClick = { event -> currentScreen = Screen.EventDetail(event) },
                        onSwitchCode = {
                            isJoined = false
                            currentScreen = Screen.JoinConference

                        }
                    )

                    is Screen.BoothList -> BoothListScreen(
                        confCode = activeCode,
                        onBoothClick = {booth -> currentScreen = Screen.BoothDetail(booth)}
                    )

                    is Screen.BoothDetail -> {
                        val booth = (currentScreen as Screen.BoothDetail).booth
                        BoothDetailScreen(booth = booth,
                            onBackClick = {currentScreen = Screen.BoothList})
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

                    is Screen.Settings -> SettingsScreen(
                        onBackClick = { currentScreen = Screen.EventList }, // Or return to the previous list
                        onDeleteAccount = {
                            // Handle Firebase account deletion here later
                            println("Delete requested")
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
}

@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    NavyBlue: Color,
    Silver: Color,
    ActionOrange: Color,
    Turquoise: Color
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

@Composable
fun JoinConferenceScreen(
    onConferenceJoined: (String) -> Unit,
    NavyBlue: Color,
    Turquoise: Color,
    Silver: Color
) {
    var confCode by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBlue)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Enter Access Code",
            style = MaterialTheme.typography.headlineSmall,
            color = Turquoise
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Enter the Event Code Provided by the Organizer",
            color = Silver,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confCode,
            onValueChange = { confCode = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Code") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Turquoise,
                unfocusedLabelColor = Silver,
                focusedBorderColor = Turquoise,
                unfocusedBorderColor = Silver,
                cursorColor = Turquoise,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { if (confCode.isNotBlank()) onConferenceJoined(confCode.trim()) },
            colors = ButtonDefaults.buttonColors(containerColor = Turquoise),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Join Conference", color = NavyBlue, fontWeight = FontWeight.Bold)
        }
    }
}