package org.communityday.navigation.events.data

import kotlinx.coroutines.flow.Flow

interface EventService {
    suspend fun getAllEvents(): Result<List<Event>>
    suspend fun getEventById(id: String): Result<Event?>
    suspend fun getEventsByCategory(category: EventCategory): Result<List<Event>>
    fun getEventsStream(): Flow<List<Event>>
    suspend fun registerForEvent(eventId: String, userId: String): Result<Boolean>
}
