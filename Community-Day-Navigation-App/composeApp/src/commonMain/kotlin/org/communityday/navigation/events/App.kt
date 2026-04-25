package org.communityday.navigation.events

import androidx.compose.animation.AnimatedVisibility
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
import communitydaynavigationapp.composeapp.generated.resources.Res
import communitydaynavigationapp.composeapp.generated.resources.ic_schedule
import communitydaynavigationapp.composeapp.generated.resources.ic_map
import communitydaynavigationapp.composeapp.generated.resources.ic_person
import communitydaynavigationapp.composeapp.generated.resources.ic_store
import communitydaynavigationapp.composeapp.generated.resources.ic_settings
import org.communityday.navigation.events.data.AuthRepository
import org.communityday.navigation.events.data.Booth
import org.communityday.navigation.events.data.ConferenceSearcher
import org.communityday.navigation.events.data.EventRepository
import org.communityday.navigation.events.ui.screens.AddConferenceScreen
import org.communityday.navigation.events.ui.screens.AdminDashboardScreen
import org.communityday.navigation.events.ui.screens.BoothDetailScreen
import org.communityday.navigation.events.ui.screens.BoothListScreen
import org.communityday.navigation.events.ui.screens.JoinConferenceScreen
import org.communityday.navigation.events.ui.screens.LoginScreen
import org.communityday.navigation.events.ui.screens.ManageMyConferencesScreen
import org.communityday.navigation.events.ui.screens.ProfileScreen
import org.communityday.navigation.events.ui.screens.SettingsScreen
import org.communityday.navigation.events.mapDirectory.LocationProvider
import org.communityday.navigation.events.ui.screens.EventSearchScreen
import org.communityday.navigation.events.data.SearchViewModel

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
    @Serializable data object AddConference: Screen
    @Serializable data class AdminDashboard(val confId: String): Screen
    @Serializable data object Login: Screen
    @Serializable data object SearchConference: Screen
    {

    }
    @Serializable data object ManageMyConference: Screen
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
fun App(locationProvider: LocationProvider) {
    val NavyBlue = Color(0xFF000033)
    val Silver = Color(0xFFC0C0C0)
    val ActionOrange = Color(0xFFFF8C00)
    val Turquoise = Color(0xFF40E0D0)

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Welcome) }
    var isJoined by remember {mutableStateOf(false)}
    var activeCode by remember { mutableStateOf("") }
    val repository = remember { EventRepository() }
    val authRepo = remember { AuthRepository() }
    val conferenceSearcher = remember { ConferenceSearcher() }
    val searchViewModel = remember { SearchViewModel(conferenceSearcher) }

    MaterialTheme {
        // 3. Logic to hide the bar on specific screens
        val showBottomBar = when (currentScreen) {
            is Screen.Welcome,
            is Screen.Login,
            is Screen.JoinConference,
            is Screen.AddConference -> false // Hide on entry/setup screens
            else -> true // Show on everything else (EventList, AdminDashboard, etc.)
        }

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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Respects the space taken by the bottom bar
            ) {
                when (val screen = currentScreen) {
                    is Screen.Welcome -> WelcomeScreen(
                        onGetStarted = {
                            currentScreen =
                                if (isJoined) Screen.EventList else Screen.SearchConference //Make SearchConferenceLater
                        },
                        NavyBlue = NavyBlue,
                        Silver = Silver,
                        ActionOrange = ActionOrange,
                        Turquoise = Turquoise,
                        onAdminLogin = {
                            currentScreen = Screen.Login
                        }
                    )

                    is Screen.JoinConference -> JoinConferenceScreen(
                        NavyBlue = NavyBlue,
                        Silver = Silver,
                        Turquoise = Turquoise,
                        onConferenceJoined = { code ->
                            activeCode = code.trim()
                            isJoined = true
                            currentScreen = Screen.EventList
                        },
                        onBackClick = { currentScreen = Screen.Welcome },
                        ActionOrange = ActionOrange
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
                        onBoothClick = { booth -> currentScreen = Screen.BoothDetail(booth) }
                    )

                    is Screen.BoothDetail -> {
                        val booth = (currentScreen as Screen.BoothDetail).booth
                        BoothDetailScreen(
                            booth = booth,
                            onBackClick = { currentScreen = Screen.BoothList })
                    }

                    is Screen.Map -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Map Screen Coming Soon")
                        }
                    }

                    is Screen.Profile -> {
                        ProfileScreen(
                            repository = repository,
                            Turquoise = Turquoise,
                            onNavigateToManageList = { currentScreen = Screen.ManageMyConference })
                    }

                    is Screen.ManageMyConference -> {
                        ManageMyConferencesScreen(
                            repository = repository,
                            onConferenceSelected = { id ->
                                currentScreen = Screen.AdminDashboard(id)
                            },
                            onBack = { currentScreen = Screen.Profile },
                            Turquoise = Turquoise
                        )
                    }

                    is Screen.Settings -> SettingsScreen(
                        onBackClick = {
                            currentScreen = Screen.EventList
                        }, // Or return to the previous list
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

                    is Screen.Login -> {
                        LoginScreen(
                            authRepo = authRepo,
                            onLoginSuccess = { currentScreen = Screen.AddConference },
                            onBackClick = { currentScreen = Screen.Welcome },
                            ActionOrange = ActionOrange
                        )
                    }

                    is Screen.AddConference -> {
                        AddConferenceScreen(
                            repository = repository,
                            onConferenceCreated = { newId ->
                                currentScreen = Screen.AdminDashboard(newId)
                            },
                            onBack = { currentScreen = Screen.Welcome }
                        )
                    }

                    is Screen.AdminDashboard -> {
                        AdminDashboardScreen(
                            confId = screen.confId, // Use 'screen' directly
                            repository = repository,
                            onBack = { currentScreen = Screen.Profile },
                            Turquoise = Turquoise
                        )
                    }

                    is Screen.SearchConference -> {
                        EventSearchScreen(
                            viewModel = searchViewModel,
                            onNavigateToJoinCode = { currentScreen = Screen.JoinConference },
                            onDirectJoin = { currentScreen = Screen.EventList }
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
    onAdminLogin: () -> Unit,
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
                Text("Logo", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Title
        AnimatedVisibility(showContent) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "ExpoPath",
                    color = Turquoise,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium
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
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp) // This handles the gap between them
            ){
            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ActionOrange
                )
            ) {
                Text(
                    text = "Join Conference",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onAdminLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ActionOrange
                )
            ) {
                Text(
                    text = "Login To Create a Conference or Community Event",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                }
            }
        }

        // Trigger animation
        LaunchedEffect(Unit) {
            showContent = true
        }
    }
}

