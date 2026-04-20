package org.communityday.navigation.events.data

import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val id: String = "", // Default to empty, Repository will overwrite this with doc.id
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val startTime: String = "",
    val endTime: String = "",
    val category: EventCategory = EventCategory.OTHER, // Safe fallback
    val room: String? = null,
    val speaker: String? = null,
    val capacity: Int? = null,
    val registeredCount: Int = 0,
    val imageUrl: String? = null,
    val tags: List<String> = emptyList(),
    val ownerId: String = "",
    val sortOrder: Int = 1
)

@Serializable
enum class EventCategory {
    KEYNOTE,
    WORKSHOP,
    TALK,
    NETWORKING,
    MEAL,
    REGISTRATION,
    SOCIAL,
    OTHER
}

// Mock data - replace with Firebase data later
object MockEvents {
    private val events = listOf(
        Event(
            id = "1",
            title = "Opening Keynote: Future of Community",
            description = "Join us for an inspiring keynote about the power of community and how technology is bringing people together in new ways.",
            location = "Main Auditorium",
            latitude = 40.7128,
            longitude = -74.0060,
            startTime = "09:00 AM",
            endTime = "10:00 AM",
            category = EventCategory.KEYNOTE,
            room = "Main Hall",
            speaker = "Sarah Johnson",
            capacity = 500,
            registeredCount = 342,
            tags = listOf("keynote", "inspiration", "community")
        ),
        Event(
            id = "2", 
            title = "Mobile Development Workshop",
            description = "Hands-on workshop building cross-platform mobile apps with Kotlin Multiplatform. Bring your laptop!",
            location = "Workshop Room A",
            latitude = 40.7130,
            longitude = -74.0058,
            startTime = "10:30 AM",
            endTime = "12:30 PM",
            category = EventCategory.WORKSHOP,
            room = "Room 101",
            speaker = "Mike Chen",
            capacity = 30,
            registeredCount = 28,
            tags = listOf("mobile", "kotlin", "workshop", "hands-on")
        ),
        Event(
            id = "3",
            title = "Networking Lunch",
            description = "Connect with fellow attendees over lunch. Vegetarian and vegan options available.",
            location = "Dining Hall",
            latitude = 40.7125,
            longitude = -74.0062,
            startTime = "12:30 PM",
            endTime = "01:30 PM",
            category = EventCategory.MEAL,
            capacity = 200,
            registeredCount = 156,
            tags = listOf("networking", "lunch", "food")
        ),
        Event(
            id = "4",
            title = "Cloud Architecture Best Practices",
            description = "Learn about modern cloud architecture patterns and how to build scalable, resilient systems.",
            location = "Conference Room B",
            latitude = 40.7132,
            longitude = -74.0056,
            startTime = "02:00 PM",
            endTime = "03:00 PM",
            category = EventCategory.TALK,
            room = "Room 205",
            speaker = "David Kumar",
            capacity = 100,
            registeredCount = 67,
            tags = listOf("cloud", "architecture", "scalability")
        ),
        Event(
            id = "5",
            title = "Coffee Break & Social",
            description = "Take a break and network with other attendees. Coffee, tea, and snacks provided.",
            location = "Lobby Area",
            latitude = 40.7126,
            longitude = -74.0061,
            startTime = "03:00 PM",
            endTime = "03:30 PM",
            category = EventCategory.SOCIAL,
            capacity = 150,
            registeredCount = 89,
            tags = listOf("coffee", "break", "social")
        ),
        Event(
            id = "6",
            title = "AI and Machine Learning in 2024",
            description = "Explore the latest trends in AI and ML, and how they're being applied in real-world scenarios.",
            location = "Tech Theater",
            latitude = 40.7134,
            longitude = -74.0054,
            startTime = "03:30 PM",
            endTime = "04:30 PM",
            category = EventCategory.TALK,
            room = "Theater 1",
            speaker = "Dr. Emily Rodriguez",
            capacity = 200,
            registeredCount = 178,
            tags = listOf("ai", "ml", "technology", "future")
        )
    )
    
    fun getAllEvents(): List<Event> = events
    
    fun getEventById(id: String): Event? = events.find { it.id == id }
    
    fun getEventsByCategory(category: EventCategory): List<Event> = 
        events.filter { it.category == category }
    
    fun getUpcomingEvents(): List<Event> = events.sortedBy { it.startTime }
}
