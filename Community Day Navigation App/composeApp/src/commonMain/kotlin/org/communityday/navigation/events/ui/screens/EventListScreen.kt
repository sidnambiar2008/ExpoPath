package org.communityday.navigation.events.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.communityday.navigation.events.data.Event
import org.communityday.navigation.events.data.EventCategory
import org.communityday.navigation.events.di.ServiceLocator

@Composable
fun EventListScreen(
    onEventClick: (Event) -> Unit,
    modifier: Modifier = Modifier
) {
    val NavyBlue = Color(0xFF000033)
    val Silver = Color(0xFFC0C0C0)
    val ActionOrange = Color(0xFFFF8C00)
    val Turquoise = Color(0xFF40E0D0)
    
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val coroutineScope = rememberCoroutineScope()
    val repository = ServiceLocator.eventRepository
    
    // Load events on composition
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            
            repository.getAllEvents()
                .onSuccess { loadedEvents ->
                    events = loadedEvents
                    isLoading = false
                }
                .onFailure { error ->
                    errorMessage = "Failed to load events: ${error.message}"
                    // Fallback to mock data for development
                    events = repository.getMockEvents()
                    isLoading = false
                }
        }
    }
    
    // Listen for real-time updates
    LaunchedEffect(Unit) {
        repository.getEventsStream().collect { updatedEvents ->
            events = updatedEvents
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBlue)
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Community Day Events",
            color = Silver,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Turquoise,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        
        // Error message
        errorMessage?.let { message ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF4A1A1A)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "⚠️ $message",
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Showing sample events for development",
                        color = Silver.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
        }
        
        // Event List
        if (!isLoading) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(events) { event ->
                    EventCard(
                        event = event,
                        onClick = { onEventClick(event) },
                        NavyBlue = NavyBlue,
                        Silver = Silver,
                        ActionOrange = ActionOrange,
                        Turquoise = Turquoise
                    )
                }
            }
        }
    }
}

@Composable
private fun EventCard(
    event: Event,
    onClick: () -> Unit,
    NavyBlue: Color,
    Silver: Color,
    ActionOrange: Color,
    Turquoise: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A4D)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title and Category
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = event.title,
                    color = Silver,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                CategoryBadge(
                    category = event.category,
                    NavyBlue = NavyBlue,
                    Silver = Silver,
                    Turquoise = Turquoise,
                    ActionOrange = ActionOrange
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Description
            Text(
                text = event.description,
                color = Silver.copy(alpha = 0.8f),
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Time and Location
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Time",
                        tint = Turquoise,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${event.startTime} - ${event.endTime}",
                        color = Silver.copy(alpha = 0.9f),
                        fontSize = 12.sp
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = ActionOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.location,
                        color = Silver.copy(alpha = 0.9f),
                        fontSize = 12.sp
                    )
                }
            }
            
            // Registration info for events with capacity
            event.capacity?.let { capacity ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${event.registeredCount}/$capacity registered",
                        color = if (event.registeredCount >= capacity) Color.Red else Silver.copy(alpha = 0.8f),
                        fontSize = 11.sp
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
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = category.name.lowercase().replaceFirstChar { it.uppercase() },
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
