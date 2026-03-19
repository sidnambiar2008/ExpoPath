package org.communityday.navigation.events.data

import kotlinx.coroutines.flow.Flow

class EventRepository(private val eventService: EventService) {
    
    suspend fun getAllEvents(): Result<List<Event>> {
        return eventService.getAllEvents()
    }
    
    suspend fun getEventById(id: String): Result<Event?> {
        return eventService.getEventById(id)
    }
    
    suspend fun getEventsByCategory(category: EventCategory): Result<List<Event>> {
        return eventService.getEventsByCategory(category)
    }
    
    fun getEventsStream(): Flow<List<Event>> {
        return eventService.getEventsStream()
    }
    
    suspend fun registerForEvent(eventId: String, userId: String): Result<Boolean> {
        return eventService.registerForEvent(eventId, userId)
    }
    
    // Fallback to mock data for development/testing
    suspend fun getMockEvents(): List<Event> {
        return MockEvents.getAllEvents()
    }
}
