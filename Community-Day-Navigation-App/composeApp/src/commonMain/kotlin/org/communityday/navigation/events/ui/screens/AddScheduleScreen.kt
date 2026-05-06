package org.communityday.navigation.events.ui.screens

import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.communityday.navigation.events.data.EventRepository
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import org.communityday.navigation.events.data.Event
import androidx.compose.runtime.getValue
import org.communityday.navigation.events.utils.convertTimeToMinutes
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.sp
import communitydaynavigationapp.composeapp.generated.resources.Res
import communitydaynavigationapp.composeapp.generated.resources.ic_delete
import kotlinx.coroutines.launch
import org.communityday.navigation.events.notifications.NotificationScheduler
import org.jetbrains.compose.resources.vectorResource

@Composable
fun AddScheduleScreen(
    confId: String,
    repository: EventRepository,
    onEventClick: (Event) -> Unit // Added to allow navigation back to details
) {
    val scope = rememberCoroutineScope()
    val allEvents by repository.getEventsStream(confId).collectAsState(emptyList())
    val registeredIds by repository.getRegisteredEventIds().collectAsState(emptySet())
    val notificationScheduler = remember { NotificationScheduler() }

    // 1. Filter AND Sort: Use your time utility to keep the schedule in order
    val mySchedule = remember(allEvents, registeredIds) {
        allEvents
            .filter { event -> registeredIds.contains(event.id) }
            .sortedBy { convertTimeToMinutes(it.startTime) }
    }


    val NavyBlue = Color(0xFF000033)
    val Turquoise = Color(0xFF40E0D0)
    val Silver = Color(0xFFC0C0C0)
    val ActionOrange = Color(0xFFFF8C00)

    Column(modifier = Modifier.fillMaxSize().background(NavyBlue).padding(16.dp)) {
        Text("My Events", color = Silver, fontSize = 24.sp, fontWeight = FontWeight.Bold)


        Spacer(modifier = Modifier.height(16.dp))

        if (mySchedule.isEmpty()) {
            // Better Empty State
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Your schedule is empty", color = Silver, fontWeight = FontWeight.Bold)
                    Text("Register for events to see them here.", color = Silver.copy(alpha = 0.6f))
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(mySchedule) { event ->
                    ScheduleCard(
                        event = event,
                        accentColor = Turquoise,
                        textColor = Silver,
                        onCardClick = { onEventClick(event) },
                        onRemove = {
                            scope.launch {
                                // 2. Update both the user schedule and the event's global count
                                repository.removeFromSchedule(confId, event.id)
                                notificationScheduler.cancelNotification(event.id)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ScheduleCard(
    event: Event,
    accentColor: Color,
    textColor: Color,
    onCardClick: () -> Unit,
    onRemove: () -> Unit
) {
    val Silver = Color(0xFFC0C0C0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        onClick = onCardClick, // Make the card clickable to go back to details
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A4D))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time Column
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(event.startTime, color = accentColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("—", color = textColor)
                Text(event.endTime, color = accentColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info Column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
                Text(
                    text = event.location,
                    color = textColor,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Remove Button
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_delete),
                    contentDescription = "Remove from Schedule",
                    tint = Silver
                )
            }
        }
    }
}