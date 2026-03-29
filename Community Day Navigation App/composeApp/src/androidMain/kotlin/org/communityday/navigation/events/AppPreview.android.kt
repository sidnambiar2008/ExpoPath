package org.communityday.navigation.events

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.communityday.navigation.events.ui.screens.EventListScreen
import org.communityday.navigation.events.WelcomeScreen
import org.communityday.navigation.events.ui.screens.EventDetailScreen
import org.communityday.navigation.events.data.Event
import org.communityday.navigation.events.data.EventCategory

@Preview
@Composable
fun AppPreview() {
    App()
}

@Preview
@Composable
fun WelcomeScreenPreview() {
    WelcomeScreen(
        onGetStarted = {},
        NavyBlue = androidx.compose.ui.graphics.Color(0xFF000033),
        Silver = androidx.compose.ui.graphics.Color(0xFFC0C0C0),
        ActionOrange = androidx.compose.ui.graphics.Color(0xFFFF8C00),
        Turquoise = androidx.compose.ui.graphics.Color(0xFF40E0D0)
    )
}

@Preview
@Composable
fun EventListScreenPreview() {
    EventListScreen(
        onEventClick = {}
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
    
    EventDetailScreen(
        event = sampleEvent,
        onBackClick = {}
    )
}
