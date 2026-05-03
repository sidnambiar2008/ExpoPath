package org.communityday.navigation.events

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import communitydaynavigationapp.composeapp.generated.resources.Res
import communitydaynavigationapp.composeapp.generated.resources.ic_home
import communitydaynavigationapp.composeapp.generated.resources.ic_schedule
import communitydaynavigationapp.composeapp.generated.resources.ic_map
import communitydaynavigationapp.composeapp.generated.resources.ic_person
import communitydaynavigationapp.composeapp.generated.resources.ic_store
import communitydaynavigationapp.composeapp.generated.resources.ic_settings
import communitydaynavigationapp.composeapp.generated.resources.ic_swapconference
import communitydaynavigationapp.composeapp.generated.resources.logo_main
import kotlinx.coroutines.launch
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
import org.communityday.navigation.events.ui.screens.AddScheduleScreen
import org.jetbrains.compose.resources.vectorResource

sealed interface Screen {
    @Serializable
    data object Welcome : Screen
    @Serializable
    data object EventList : Screen

    @Serializable data object BoothList: Screen
    @Serializable data class BoothDetail(val booth: Booth):Screen{

    }
    @Serializable data object Map: Screen
    @Serializable data object Profile: Screen
    @Serializable data object JoinConference : Screen
    @Serializable data class EventDetail(val event: Event, val confId: String) : Screen
    @Serializable data object Settings: Screen
    @Serializable data object AddConference: Screen
    @Serializable data class AdminDashboard(val confId: String): Screen
    @Serializable data object Login: Screen
    @Serializable data object SearchConference: Screen
    {

    }
    @Serializable data object ManageMyConference: Screen
    @Serializable data object ScheduleScreen: Screen
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
            selected = currentScreen is Screen.ScheduleScreen,
            onClick = { onTabSelected(Screen.ScheduleScreen) },
            label = { Text("My Events", textAlign = TextAlign.Center) },
            icon = { Icon(
                painter = painterResource(Res.drawable.ic_schedule),
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
    var pendingCode by remember { mutableStateOf("") }
    val isAttendeeModeActive = activeCode.trim().isNotEmpty()
    val scope = rememberCoroutineScope()
    var isDeletingAccount by remember { mutableStateOf(false) }
    var showSecurityError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        repository.ensureAnonymousAuth()
    }

    MaterialTheme {
        // 3. Logic to hide the bar on specific screens
        val showBottomBar = when (currentScreen) {
            is Screen.Welcome,
            is Screen.Login,
            is Screen.JoinConference,
            is Screen.SearchConference,
            is Screen.AddConference,
            is Screen.ManageMyConference, // Hides bar when looking at your list of owned confs
            is Screen.AdminDashboard -> false // Hides bar when adding/editing events
            else -> true
        }

       // BackHandler(enabled = currentScreen !is Screen.Welcome && currentScreen !is Screen.EventList) {
            // If they are in a Detail screen or another tab, send them back to the Event List
         //   currentScreen = Screen.EventList
       // }
        val focusManager = LocalFocusManager.current
        Scaffold(
            containerColor = NavyBlue,
            bottomBar = {
                if (showBottomBar) {
                    BottomNavigationBar(
                        currentScreen = currentScreen,
                        onTabSelected = { selectedScreen: Screen -> currentScreen = selectedScreen }
                    )
                }
            },
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }

        ) { paddingValues ->
            // Content Area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (val screen = currentScreen) {
                    is Screen.Welcome -> WelcomeScreen(
                        onGetStarted = {
                            currentScreen =
                                if (isJoined) Screen.EventList else Screen.SearchConference
                        },
                        NavyBlue = NavyBlue,
                        Silver = Silver,
                        ActionOrange = ActionOrange,
                        Turquoise = Turquoise,
                        onAdminLogin = {
                            currentScreen = Screen.Login
                        }
                    )

                    is Screen.ScheduleScreen -> AddScheduleScreen(
                        confId = activeCode,
                        repository = repository,
                        onEventClick = {event ->
                            currentScreen = Screen.EventDetail(event, activeCode)}
                    )

                    is Screen.JoinConference -> JoinConferenceScreen(
                        NavyBlue = NavyBlue,
                        Silver = Silver,
                        Turquoise = Turquoise,
                        initialCode = pendingCode,
                        onConferenceJoined = { code ->
                            activeCode = code.trim()
                            isJoined = true
                            currentScreen = Screen.EventList
                            pendingCode = ""
                        },
                        onBackClick = {
                            pendingCode = ""
                            currentScreen = Screen.Welcome},
                        ActionOrange = ActionOrange
                    )

                    is Screen.EventList -> EventListScreen(
                        confCode = activeCode,
                        onEventClick = { event -> currentScreen = Screen.EventDetail(event, activeCode) },
                        onSwitchCode = {
                            isJoined = false
                            currentScreen = Screen.Welcome
                        }
                    )

                    is Screen.BoothList -> BoothListScreen(
                        confCode = activeCode,
                        onBoothClick = { booth -> currentScreen = Screen.BoothDetail(booth) }
                    )

                    is Screen.BoothDetail -> {
                        val booth = (currentScreen as Screen.BoothDetail).booth
                        val conference by repository.getConferenceById(activeCode).collectAsState(null)
                        BoothDetailScreen(
                            booth = booth,
                            conferenceAddress = conference?.address ?: "",
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
                            onNavigateToManageList = { currentScreen = Screen.ManageMyConference },
                            onBackClick = {currentScreen = Screen.Welcome},
                            authRepository = authRepo
                            )
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
                        isDeleting = isDeletingAccount,
                        showSecurityWarning = showSecurityError,
                        onDismissSecurityWarning = { showSecurityError = false },
                        onDeleteAccount = {
                            isDeletingAccount = true
                            scope.launch {
                                val result = repository.deleteUserCompletely()
                                isDeletingAccount = false // Stop the spinner

                                if (result.isSuccess) {
                                    currentScreen = Screen.Welcome
                                } else {
                                    val error = result.exceptionOrNull()?.message
                                    if (error == "SENSITIVE_OPERATION_REQUIRED_RECENT_LOGIN") {
                                        showSecurityError = true
                                    } else {
                                        // Handle other errors (like no internet)
                                        println("Delete Error: $error")
                                    }
                                }
                            }
                        }
                    )

                    is Screen.EventDetail -> {
                        val event = (currentScreen as Screen.EventDetail).event
                        val confId = (currentScreen as Screen.EventDetail).confId

                        // Collect the conference data to get the building address
                        val conference by repository.getConferenceById(confId).collectAsState(null)

                        EventDetailScreen(
                            confId = confId,
                            event = event,
                            repository = repository,
                            conferenceAddress = conference?.address ?: "", // Pass the anchor!
                            onBackClick = { currentScreen = Screen.EventList }
                        )
                    }

                    is Screen.Login -> {
                        LoginScreen(
                            authRepo = authRepo,
                            onLoginSuccess = {
                                activeCode = ""
                                isJoined = false
                                currentScreen = Screen.AddConference},
                            onBackClick = { currentScreen = Screen.Welcome },
                        )
                    }

                    is Screen.AddConference -> {
                        AddConferenceScreen(
                            repository = repository,
                            onConferenceCreated = { newId ->
                                currentScreen = Screen.AdminDashboard(newId)
                            },
                            onBack = { currentScreen = Screen.Welcome },
                            onSwitchAccount = { currentScreen = Screen.Login},
                            authRepository = authRepo
                        )
                    }

                    is Screen.AdminDashboard -> {
                        AdminDashboardScreen(
                            confId = screen.confId, // Use 'screen' directly
                            repository = repository,
                            onBack = {
                                if (activeCode.isBlank()) {
                                    // If they aren't "attending" a conference, send them to Welcome
                                    currentScreen = Screen.Welcome
                                } else {
                                    // If they have an active conference, send them back to the Profile
                                    // where the bottom bar will reappear!
                                    currentScreen = Screen.Profile
                                }
                            },
                            Turquoise = Turquoise,
                            isAttendeeModeActive = isAttendeeModeActive
                        )
                    }

                    is Screen.SearchConference -> {
                        EventSearchScreen(
                            viewModel = searchViewModel,
                            onNavigateToJoinCode = {
                                println("Navigate to Join Code Screen")
                                currentScreen = Screen.JoinConference
                            },
                            onDirectJoin = { code ->
                                // 2. Set the code as active so the Event List knows what to load
                                activeCode = code.trim()
                                isJoined = true
                                currentScreen = Screen.EventList
                            },
                            onBackClick = {currentScreen = Screen.Welcome}
                        )
                    }
                }
                if (showBottomBar) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 16.dp, top = 8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp), // Spaces the two buttons apart
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // --- Home Button Group ---
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { currentScreen = Screen.Welcome }
                            ) {
                                IconButton(
                                    onClick = { currentScreen = Screen.Welcome },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(Res.drawable.ic_home),
                                        contentDescription = "Home",
                                        tint = Turquoise,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Text(
                                    text = "Home",
                                    color = Turquoise,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // --- Change Event Button Group ---
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    // Change this to whatever screen lets them pick a new code
                                    currentScreen = Screen.JoinConference
                                }
                            ) {
                                IconButton(
                                    onClick = { currentScreen = Screen.SearchConference },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        // Make sure you have a swap or settings icon in your resources
                                        imageVector = vectorResource(Res.drawable.ic_swapconference),
                                        contentDescription = "Change Event",
                                        tint = Turquoise,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Text(
                                    text = "Change Event",
                                    color = Turquoise,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
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
            Image(
                painter = painterResource(Res.drawable.logo_main),
                contentDescription = "App Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(160.dp) // Adjust size as needed
                    .padding(bottom = 16.dp)
            )
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
                text = "Navigate through events\nConnect with the community\nMake the most of your day",
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
                    text = "Join Event",
                    color = Color.White,
                    fontSize = 16.sp,
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
                    text = "Create Event",
                    color = Color.White,
                    fontSize = 16.sp,
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

