package org.communityday.navigation.events.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch
import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.Transaction
import kotlinx.coroutines.flow.flowOf
import dev.gitlive.firebase.firestore.where


class EventRepository {
    private val firestore = Firebase.firestore

    /**
     * Helper to build the dynamic path based on the Conference ID (confId)
     */
    private fun getEventCollection(confId: String) = firestore
        .collection("conferences")
        .document(confId)
        .collection("events")

    private fun getBoothCollection(confId: String) = firestore
        .collection("conferences")
        .document(confId)
        .collection("booths")

    /**
     * One-time fetch for a specific conference
     */
    suspend fun getAllEvents(confId: String): Result<List<Event>> {
        return try {
            val snapshot = getEventCollection(confId).get()
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
        return getEventCollection(confId)
            .orderBy("startTime") // 👈 This keeps your schedule in order
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
        return getBoothCollection(confCode)
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

    fun getManagedConferencesStream(): Flow<List<Conference>> {
        val user = Firebase.auth.currentUser ?: return flowOf(emptyList())

        return firestore.collection("conferences")
            .where("ownerId", user.uid) // 👈 Direct field-to-value mapping
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    doc.data<Conference>().copy(id = doc.id)
                }
            }
            .catch { e ->
                println("Error fetching owned conferences: ${e.message}")
                emit(emptyList())
            }
    }


    suspend fun registerForEvent(confId: String, eventId: String): Result<Unit> {
        return try {
            val eventRef = getEventCollection(confId).document(eventId)

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
            val user = Firebase.auth.currentUser ?: throw Exception("Not logged in")

            // Attach the ownerId so the Security Rules allow the 'create'
            val eventWithSecurity = newEvent.copy(ownerId = user.uid)
            getEventCollection(confId).add(eventWithSecurity)
            Result.success(Unit)
        } catch (e: Exception) {
            println("Admin Error: Failed to add event: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun createConference(conference: Conference): Result<Unit> {
        return try {
            val user = Firebase.auth.currentUser ?: throw Exception("Not logged in")

            // We create a copy or a map to ensure the security fields are present
            val conferenceData = conference.copy(
                ownerId = user.uid,     // Must match request.auth.uid in rules
                isPublished = false     // Starts as a draft for "Nonsense" protection
            )

            firestore.collection("conferences")
                .document(conferenceData.id)
                .set(conferenceData)

            Result.success(Unit)
        } catch (e: Exception) {
            println("Error creating conference: ${e.message}")
            Result.failure(e)
        }
    }


    suspend fun addBooth(confId: String, newBooth: Booth): Result<Unit> {
        return try {
            // 1. Get the current user
            val user = Firebase.auth.currentUser ?: throw Exception("Not logged in")

            // 2. Attach the ownerId so the Security Rules are satisfied
            val boothWithSecurity = newBooth.copy(ownerId = user.uid)

            // 3. Save to the sub-collection
            getBoothCollection(confId)
                .add(boothWithSecurity)

            Result.success(Unit)
        } catch (e: Exception) {
            println("Admin Error: Failed to add booth: ${e.message}")
            Result.failure(e)
        }
    }
    suspend fun updateEvent(confId: String, event: Event): Result<Unit> {
        return try {
            getEventCollection(confId)
                .document(event.id) // 👈 Target the existing ID
                .set(event)         // 👈 Overwrite with new data
            Result.success(Unit)
        } catch (e: Exception) {
            println("Update Error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateBooth(confId: String, booth: Booth): Result<Unit> {
        return try {
            getBoothCollection(confId)
                .document(booth.id)
                .set(booth)
            Result.success(Unit)
        } catch (e: Exception) {
            println("Update Error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deleteEvent(confId: String, eventId: String): Result<Unit> {
        return try {
            getEventCollection(confId).document(eventId).delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun deleteBooth(confId: String, boothId: String): Result<Unit> {
        return try {
            getBoothCollection(confId).document(boothId).delete()
            Result.success(Unit)
        } catch (e: Exception) {
            println("Delete Booth Error: ${e.message}")
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
