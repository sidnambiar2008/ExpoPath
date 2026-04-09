package org.communityday.navigation.events.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch

class EventRepository {
    private val firestore = Firebase.firestore

    /**
     * Helper to build the dynamic path based on the Conference ID (confId)
     */
    private fun getCollection(confId: String) = firestore
        .collection("conferences")
        .document(confId)
        .collection("events")

    /**
     * One-time fetch for a specific conference
     */
    suspend fun getAllEvents(confId: String): Result<List<Event>> {
        return try {
            val snapshot = getCollection(confId).get()
            val events = snapshot.documents.map { doc ->
                doc.data<Event>().copy(id = doc.id)
            }
            Result.success(events)
        } catch (e: Exception) {
            println("Firestore Fetch Error for $confId: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Real-time Stream for a specific conference
     */
    fun getEventsStream(confId: String): Flow<List<Event>> {
        // We add .orderBy here to tell Firestore how to send the data
        return getCollection(confId)
            .orderBy("sortOrder") // 👈 This keeps your schedule in order
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    doc.data<Event>().copy(id = doc.id)
                }
            }.catch { e ->
                println("Firestore Stream Error for $confId: ${e.message}")
                emit(emptyList())
            }
    }

    fun getBoothsStream(confId: String): Flow<List<Booth>> {
        return firestore
            .collection("conferences/$confId/booths")
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    doc.data<Booth>().copy(id = doc.id)
                }
            }.catch { e ->
        println("Firestore Stream Error for $confId: ${e.message}")
        emit(emptyList())
    }

    }

    /**
     * Your safety net: If Firebase is empty or offline
     */
    fun getMockEvents(): List<Event> {
        return listOf(
            Event(
                id = "mock_1",
                title = "Offline: Check Connection",
                description = "We couldn't reach the cloud. Showing local data.",
                category = EventCategory.OTHER,
                location = "Unknown",
                startTime = "--:--",
                endTime = "--:--",
                latitude = 0.0,
                longitude = 0.0
            )
        )
    }
}