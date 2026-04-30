package org.communityday.navigation.events.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import kotlinx.coroutines.launch
import org.communityday.navigation.events.data.EventRepository
import org.communityday.navigation.events.notifications.NotificationScheduler

@Composable
fun EventDetailScreen(
    confId: String,          // Pass this in
    event: Event,
    repository: EventRepository, // Pass this in
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val context: Any? = null

    val NavyBlue = Color(0xFF000033)
    val Silver = Color(0xFFC0C0C0)
    val ActionOrange = Color(0xFFFF8C00)
    val Turquoise = Color(0xFF40E0D0)
    val scope = rememberCoroutineScope()
    val scheduler = remember { NotificationScheduler() }

    // 1. Listen to which events this user is registered for
    val registeredIds by repository.getRegisteredEventIds().collectAsState(emptySet())
    val isUserRegistered = registeredIds.contains(event.id)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBlue)
            .padding(16.dp)
    ) {
        // Header with back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_back_arrow),
                    contentDescription = "Back",
                    tint = Silver
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
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Event Content
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = event.title,
                            color = Silver,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CategoryBadge(event.category, NavyBlue, Silver, Turquoise, ActionOrange)
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
                        }
                    )
                }
            }
            
            // Action Button
            item {
                if (event.latitude != null && event.longitude != null) {
                    Button(
                        onClick = {
                            openMap(
                                lat = event.latitude,
                                lon = event.longitude,
                                label = event.title,
                                context = context
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ActionOrange
                        )
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
                }
                else {
                    Text("Physical Location: ${event.location}")
                }
            }
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

                                // Schedule the alert
                                scheduler.scheduleEventNotification(
                                    id = event.id,
                                    title = event.title,
                                    startTime = event.startTime
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
