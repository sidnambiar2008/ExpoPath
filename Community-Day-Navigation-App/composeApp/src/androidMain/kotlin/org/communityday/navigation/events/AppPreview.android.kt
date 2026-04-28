package org.communityday.navigation.events

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import org.communityday.navigation.events.ui.screens.EventListScreen
import org.communityday.navigation.events.WelcomeScreen
import org.communityday.navigation.events.ui.screens.EventDetailScreen
import org.communityday.navigation.events.data.Event
import org.communityday.navigation.events.data.EventCategory
import org.communityday.navigation.events.mapDirectory.LocationProvider

@Preview
@Composable
fun AppPreview() {
    // We provide a "dummy" provider just so the preview can render
    val dummyProvider = LocationProvider()
    App(locationProvider = dummyProvider)
}

@Preview
@Composable
fun WelcomeScreenPreview() {
    WelcomeScreen(
        onGetStarted = {},
        NavyBlue = androidx.compose.ui.graphics.Color(0xFF000033),
        Silver = androidx.compose.ui.graphics.Color(0xFFC0C0C0),
        ActionOrange = androidx.compose.ui.graphics.Color(0xFFFF8C00),
        Turquoise = androidx.compose.ui.graphics.Color(0xFF40E0D0),
        onAdminLogin = {}
    )
}

@Preview
@Composable
fun EventListScreenPreview() {
    EventListScreen(
        confCode = "PREVIEW123", // A dummy conference code
        onEventClick = { event -> },
        onSwitchCode = {}
    )
}

@Preview
@Composable
fun EventDetailScreenPreview() {
    val sampleEvent = Event(
        id = "1",
        title = "Sample Event",
        description = "This is a sample event for preview",
        location = "Sample Location",
        latitude = 40.7128,
        longitude = -74.0060,
        startTime = "10:00 AM",
        endTime = "11:00 AM",
        category = EventCategory.TALK,
        room = "Room 101",
        speaker = "John Doe",
        capacity = 100,
        registeredCount = 45,
        imageUrl = null,
        tags = listOf("android", "kotlin", "compose")
    )
    
    //EventDetailScreen(
     //   event = sampleEvent,
     //   onBackClick = {}
    //)
}
