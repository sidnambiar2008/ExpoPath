package org.communityday.navigation.events.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.communityday.navigation.events.data.Event
import org.communityday.navigation.events.data.EventCategory

class EventApiService(private val httpClient: HttpClient) {
    
    companion object {
        private const val BASE_URL = "https://your-api-base-url.com/api"
        private const val EVENTS_ENDPOINT = "$BASE_URL/events"
    }
    
    suspend fun getAllEvents(): Result<List<Event>> {
        return try {
            val events: List<Event> = httpClient.get(EVENTS_ENDPOINT).body()
            Result.success(events)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getEventById(id: String): Result<Event?> {
        return try {
            val event: Event? = httpClient.get("$EVENTS_ENDPOINT/$id").body()
            Result.success(event)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getEventsByCategory(category: EventCategory): Result<List<Event>> {
        return try {
            val events: List<Event> = httpClient.get("$EVENTS_ENDPOINT?category=$category").body()
            Result.success(events)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun registerForEvent(eventId: String, userId: String): Result<Boolean> {
        return try {
            val response = httpClient.post("$EVENTS_ENDPOINT/$eventId/register") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("userId" to userId))
            }
            Result.success(response.status.value in 200..299)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
