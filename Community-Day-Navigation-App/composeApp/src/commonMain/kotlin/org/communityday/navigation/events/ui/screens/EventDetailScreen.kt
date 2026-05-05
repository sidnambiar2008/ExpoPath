package org.communityday.navigation.events.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.communityday.navigation.events.data.Event
import org.communityday.navigation.events.data.EventCategory
import org.jetbrains.compose.resources.vectorResource
import communitydaynavigationapp.composeapp.generated.resources.Res
import communitydaynavigationapp.composeapp.generated.resources.ic_back_arrow
import communitydaynavigationapp.composeapp.generated.resources.ic_howtoreg
import communitydaynavigationapp.composeapp.generated.resources.ic_location_on
import communitydaynavigationapp.composeapp.generated.resources.ic_map
import communitydaynavigationapp.composeapp.generated.resources.ic_meeting_room
import communitydaynavigationapp.composeapp.generated.resources.ic_person
import communitydaynavigationapp.composeapp.generated.resources.ic_schedule
import org.communityday.navigation.events.mapDirectory.openMap
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import kotlinx.coroutines.launch
import org.communityday.navigation.events.data.Conference
import org.communityday.navigation.events.data.EventRepository
import org.communityday.navigation.events.notifications.NotificationScheduler
import androidx.compose.foundation.text.selection.SelectionContainer
import communitydaynavigationapp.composeapp.generated.resources.ic_block
import communitydaynavigationapp.composeapp.generated.resources.ic_flag

@Composable
fun EventDetailScreen(
    confId: String,          // Pass this in
    event: Event,
    repository: EventRepository, // Pass this in
    onBackClick: () -> Unit,
    conferenceAddress: String,
    modifier: Modifier = Modifier
) {

    val context: Any? = null

    val NavyBlue = Color(0xFF000033)
    val Silver = Color(0xFFC0C0C0)
    val ActionOrange = Color(0xFFFF8C00)
    val Turquoise = Color(0xFF40E0D0)
    val scope = rememberCoroutineScope()
    val scheduler = remember { NotificationScheduler() }
    val conference by repository.getConferenceById(confId).collectAsState(null)

    // 1. Listen to which events this user is registered for
    val registeredIds by repository.getRegisteredEventIds().collectAsState(emptySet())
    val isUserRegistered = registeredIds.contains(event.id)
    val focusManager = LocalFocusManager.current // 1. Add this
    var showSafetyDialog by remember { mutableStateOf(false) }
    var showHideDialog by remember { mutableStateOf(false) }
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBlue)
            // 2. Add this modifier to the main container
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
            .padding(vertical = 16.dp, horizontal = 16.dp)
            .padding(bottom = 8.dp)

    ) {
        // Header with back button
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_back_arrow),
                    contentDescription = "Back",
                    tint = Turquoise
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Event Details",
                color = Silver,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        // Event Content
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                // Title and Category
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1A1A4D)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SelectionContainer(modifier = Modifier.weight(1f)) {
                            Text(
                                text = event.title,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 30.sp
                            )
                        }
                    }
                }
            }
            
            item {
                // Description
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1A1A4D)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "About this event",
                            color = Silver,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = event.description,
                            color = Silver.copy(alpha = 0.9f),
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
            
            item {
                // Event Details Grid
                EventDetailsGrid(event, Silver, ActionOrange, Turquoise)
            }
            
            // Tags if available
            if (event.tags.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1A1A4D)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Tags",
                                color = Silver,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            TagsRow(event.tags, Turquoise)
                        }
                    }
                }
            }
            
            // Registration info if capacity is set
            event.capacity?.let { capacity ->
                item {
                    RegistrationCard(
                        event = event,
                        capacity = capacity,
                        backgroundColor = Silver,
                        iconTint = ActionOrange,
                        isAlreadyRegistered = isUserRegistered, // Pass the status
                        onRegisterClick = {
                            scope.launch {
                                // 2. Trigger the logic we built
                                if (isUserRegistered) {
                                    repository.removeFromSchedule(confId, event.id)
                                } else {
                                    repository.registerForEvent(confId, event.id)
                                    repository.saveEventToUserSchedule(confId, event.id)
                                }
                            }
                        },
                        currentConference = conference,
                    )
                }
            }
            
            // Action Button
            item {
                // Only show the section if there is actually a location to show
                if ((event.latitude != null && event.longitude != null) || event.location.isNotBlank()) {

                    Button(
                        onClick = {
                            // 1. Grab the "Anchor" from the state we collected at the top of the screen
                            val anchor = conference?.address ?: conferenceAddress
                            println("Debug: Anchor address is -> $anchor")

                            // 2. Call the smart function.
                            // We pass 0.0 if the lat/lon is null; the actual fun handles the rest.
                            openMap(
                                lat = event.latitude ?: 0.0,
                                lon = event.longitude ?: 0.0,
                                label = event.location, // e.g. "Room 204"
                                conferenceAddress = anchor, // THE ANCHOR!
                                context = context
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ActionOrange)
                    ) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.ic_map),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "View on Map",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp)) // Give it some space from the map info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // BUTTON 1: REPORT
                        IconButton(
                            onClick = {
                                showSafetyDialog = true
                            }
                        ) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.ic_flag),
                                contentDescription = "Report Content",
                                tint = Color(0xFFCF6679).copy(alpha = 0.8f),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // A small vertical divider for a high-end look
                        Box(
                            modifier = Modifier
                                .height(20.dp)
                                .width(1.dp)
                                .background(Silver.copy(alpha = 0.3f))
                                .padding(horizontal = 8.dp)
                        )

                        // BUTTON 2: HIDE
                        IconButton(
                            onClick = {
                                // You can show a simpler "Are you sure you want to hide?" dialog here
                                showHideDialog = true
                            }
                        ) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.ic_block),
                                contentDescription = "Hide Conference",
                                tint = Color(0xFFCF6679).copy(alpha = 0.8f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
        if (showSafetyDialog) {
            AlertDialog(
                onDismissRequest = { showSafetyDialog = false },
                containerColor = Color(0xFF1A1A4D),
                title = {
                    Text("Report Content", color = Color.White, fontWeight = FontWeight.Bold)
                },
                text = {
                    Text(
                        "You will be redirected to our community report form to provide details about this event.",
                        color = Silver,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showSafetyDialog = false
                            uriHandler.openUri("https://docs.google.com/forms/...")
                        }
                    ) {
                        // The main action button
                        Text("Report", color = Turquoise, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSafetyDialog = false }) {
                        Text("Cancel", color = Silver)
                    }
                }
            )
        }
        if (showHideDialog) {
            AlertDialog(
                onDismissRequest = { showHideDialog = false },
                containerColor = Color(0xFF1A1A4D),
                title = {
                    Text("Hide Conference?", color = Color.White, fontWeight = FontWeight.Bold)
                },
                text = {
                    Column {
                        Text(
                            "You will no longer see '${conference?.name}' or any of its events in your search results.",
                            color = Silver,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "This action cannot be undone easily.",
                            color = Color(0xFFCF6679),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showHideDialog = false
                            scope.launch {
                                repository.hideConference(confId)
                                onBackClick() // Send them home immediately
                            }
                        }
                    ) {
                        // Keep the color red/pink to signal it's a permanent "removal"
                        Text("Hide", color = Color(0xFFCF6679), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showHideDialog = false }) {
                        Text("Cancel", color = Silver)
                    }
                }
            )
        }
    }
}

@Composable
private fun EventDetailsGrid(
    event: Event,
    Silver: Color,
    ActionOrange: Color,
    Turquoise: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A4D)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Event Information",
                color = Silver,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            //Time
            DetailRow(
                icon = vectorResource(Res.drawable.ic_schedule),
                label = "Time",
                value = "${event.startTime} - ${event.endTime}",
                iconColor = Turquoise,
                textColor = Silver
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
             //Location
            DetailRow(
                icon = vectorResource(Res.drawable.ic_location_on),
                label = "Location",
                value = event.location,
                iconColor = ActionOrange,
                textColor = Silver
            )

            
             //Room if available
            event.room?.let { room ->
                Spacer(modifier = Modifier.height(12.dp))
                DetailRow(
                    icon = vectorResource(Res.drawable.ic_meeting_room),
                    label = "Room",
                    value = room,
                    iconColor = Silver,
                    textColor = Silver
                )
            }
            
            // Speaker if available

            event.speaker?.let { speaker ->
                Spacer(modifier = Modifier.height(12.dp))
                DetailRow(
                    icon = vectorResource(Res.drawable.ic_person),
                    label = "Speaker",
                    value = speaker,
                    iconColor = Turquoise,
                    textColor = Silver
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    iconColor: Color,
    textColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                color = textColor.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
            Text(
                text = value,
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun TagsRow(
    tags: List<String>,
    Turquoise: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.forEach { tag ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Turquoise.copy(alpha = 0.2f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = tag.lowercase(),
                    color = Turquoise,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun RegistrationCard(
    event: Event,
    capacity: Int,
    backgroundColor: Color,
    iconTint: Color,
    currentConference: Conference?,
    isAlreadyRegistered: Boolean, // New parameter
    onRegisterClick: () -> Unit
) {
    val isFull = event.registeredCount >= capacity && !isAlreadyRegistered
    val scheduler = remember { NotificationScheduler() }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isAlreadyRegistered -> Color(0xFF1A3A4D) // Blue-ish for joined
                isFull -> Color(0xFF4A1A1A)             // Red for full
                else -> Color(0xFF1A4D1A)               // Green for available
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = vectorResource(Res.drawable.ic_howtoreg),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isAlreadyRegistered) "You're Scheduled!" else "Registration Status",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = if (isFull) "Fully Booked" else "${event.registeredCount} / $capacity spots taken",
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            // Only show the button if it's not full, OR if they are already in and want to leave
            if (!isFull || isAlreadyRegistered) {
                Button(
                    onClick = {
                        scope.launch {
                            if (!isAlreadyRegistered) {
                                // --- JOINING ---
                                // Request permission (shows popup on first Join)
                                scheduler.requestPermissions()

                                // Do your database work
                                onRegisterClick()

                                val dateToUse = currentConference?.dateString ?: "2026-05-01"
                                // Schedule the alert
                                scheduler.scheduleEventNotification(
                                    id = event.id,
                                    title = event.title,
                                    startTime = event.startTime,
                                    dateString = dateToUse, // Pass the date here!
                                )
                            } else {
                                // --- LEAVING ---
                                onRegisterClick()

                                // Cancel so they don't get alerted for an event they left
                                scheduler.cancelNotification(event.id)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAlreadyRegistered) Color.Transparent else Color.White
                    ),
                    border = if (isAlreadyRegistered) BorderStroke(1.dp, Color.White) else null,
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Text(
                        text = if (isAlreadyRegistered) "Leave" else "Join",
                        color = if (isAlreadyRegistered) Color.White else Color(0xFF1A4D1A)
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryBadge(
    category: EventCategory,
    NavyBlue: Color,
    Silver: Color,
    Turquoise: Color,
    ActionOrange: Color
) {
    val (backgroundColor, textColor) = when (category) {
        EventCategory.KEYNOTE -> ActionOrange to Color.White
        EventCategory.WORKSHOP -> Turquoise to NavyBlue
        EventCategory.TALK -> Color(0xFF9C27B0) to Color.White
        EventCategory.NETWORKING -> Color(0xFF4CAF50) to Color.White
        EventCategory.MEAL -> Color(0xFFFF9800) to Color.White
        EventCategory.REGISTRATION -> Color(0xFF607D8B) to Color.White
        EventCategory.SOCIAL -> Color(0xFFE91E63) to Color.White
        EventCategory.OTHER -> Silver to NavyBlue
    }
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = category.name.lowercase().replaceFirstChar { it.uppercase() },
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
