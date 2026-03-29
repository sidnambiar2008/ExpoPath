package org.communityday.navigation.events.data

import org.communityday.navigation.events.data.EventService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

actual object EventServiceFactory {
    actual fun createEventService(): EventService {
        // JS implementation - can use fetch API or mock for now
        return MockEventService()
    }
}

// Mock implementation for JS platform
class MockEventService : EventService {
    override suspend fun getAllEvents(): Result<List<Event>> {
        return Result.success(emptyList())
    }
    
    override suspend fun getEventById(id: String): Result<Event?> {
        return Result.success(null)
    }
    
    override suspend fun getEventsByCategory(category: EventCategory): Result<List<Event>> {
        return Result.success(emptyList())
    }
    
    override fun getEventsStream(): Flow<List<Event>> = flowOf(emptyList())
    
    override suspend fun registerForEvent(eventId: String, userId: String): Result<Boolean> {
        return Result.success(true)
    }
}
