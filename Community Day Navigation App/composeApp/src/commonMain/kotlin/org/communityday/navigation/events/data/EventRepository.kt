package org.communityday.navigation.events.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch
import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.Transaction


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

    fun getBoothsStream(confCode: String): Flow<List<Booth>> {
        return firestore.collection("conferences")
            .document(confCode)
            .collection("booths")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    doc.data<Booth>().copy(id = doc.id)
                }
            }
            .catch { e ->
                println("Booth Fetch Error: ${e.message}")
                emit(emptyList()) // Returns an empty list instead of crashing the UI
            }
    }

    suspend fun registerForEvent(confId: String, eventId: String): Result<Unit> {
        return try {
            val eventRef = getCollection(confId).document(eventId)

            // Explicitly telling the IDE this is a Firestore Transaction
            firestore.runTransaction {
                // You are now inside the 'Transaction' scope.
                // Use 'this' or call methods directly.
                val snapshot = get(eventRef)

                val currentCount: Int = snapshot.get("registeredCount") ?: 0
                val capacity: Int = snapshot.get("capacity") ?: 0

                if (currentCount < capacity) {
                    update(eventRef, mapOf("registeredCount" to (currentCount + 1)))
                } else {
                    throw Exception("Event is full!")
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            println("Registration Error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun addEvent(confId: String, newEvent: Event): Result<Unit> {
        return try {
            val collection = getCollection(confId)

            // We use .add() to let Firestore generate a unique ID,
            // or .document(customId).set() if you want to prevent duplicates.
            collection.add(newEvent)

            Result.success(Unit)
        } catch (e: Exception) {
            println("Admin Error: Failed to add event: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun createConference(conference: Conference): Result<Unit> {
        return try {
            // This adds a new document to the top-level 'conferences' collection
            // You can use .add(conference) for a random ID
            // OR .document(conference.id).set(conference) if you have a specific code (like "NYC2026")
            firestore.collection("conferences")
                .document(conference.id)
                .set(conference)

            Result.success(Unit)
        } catch (e: Exception) {
            println("Error creating conference: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun addBooth(confId: String, newBooth: Booth): Result<Unit> {
        return try {
            // 1. Point to the 'booths' sub-collection inside the specific conference
            firestore.collection("conferences")
                .document(confId)
                .collection("booths")
                .add(newBooth) // 2. Use the booth object passed in

            Result.success(Unit)
        } catch (e: Exception) {
            println("Admin Error: Failed to add booth: ${e.message}")
            Result.failure(e)
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
