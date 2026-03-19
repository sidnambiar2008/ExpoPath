package org.communityday.navigation.events.data

import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.communityday.navigation.events.network.EventApiService

class KtorEventService(private val httpClient: HttpClient) : EventService {
    
    private val apiService = EventApiService(httpClient)
    
    override suspend fun getAllEvents(): Result<List<Event>> {
        return apiService.getAllEvents()
    }
    
    override suspend fun getEventById(id: String): Result<Event?> {
        return apiService.getEventById(id)
    }
    
    override suspend fun getEventsByCategory(category: EventCategory): Result<List<Event>> {
        return apiService.getEventsByCategory(category)
    }
    
    override fun getEventsStream(): Flow<List<Event>> = flow {
        // For non-Android platforms, we'll poll periodically
        // In a real app, you might implement WebSocket or other real-time mechanisms
        while (true) {
            getAllEvents().onSuccess { events ->
                emit(events)
            }.onFailure { 
                // Emit empty list on error to prevent UI from breaking
                emit(emptyList())
            }
            kotlinx.coroutines.delay(30000) // Poll every 30 seconds
        }
    }
    
    override suspend fun registerForEvent(eventId: String, userId: String): Result<Boolean> {
        return apiService.registerForEvent(eventId, userId)
    }
}
