package org.communityday.navigation.events.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.communityday.navigation.events.utils.convertTimeToMinutes

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
        return getEventCollection(confId)
            .snapshots
            .map { snapshot ->
                snapshot.documents
                    .map { doc ->
                        doc.data<Event>().copy(id = doc.id)
                    }
                    .sortedBy { event ->
                        convertTimeToMinutes(event.startTime)
                    }
            }
            .catch { e ->
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
            .where { "ownerId" equalTo user.uid } // 👈 The new FilterBuilder syntax
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    val conference = doc.data<Conference>()
                    conference.copy(objectID = doc.id)
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
                val capacity: Int = snapshot.get("capacity") ?: -1

                if (capacity == -1||currentCount < capacity) {
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
                ownerId = user.uid, // Must match request.auth.uid in rules
                isPublished = false,     // Starts as a draft for "Nonsense" protection
                joinCode = conference.joinCode,
                objectID = conference.joinCode
            )

            firestore.collection("conferences")
                .document(conferenceData.joinCode)
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
     * Saves an event ID to the user's private registration list
     */
    suspend fun saveEventToUserSchedule(confId: String, eventId: String): Result<Unit> {
        val user = Firebase.auth.currentUser ?: return Result.failure(Exception("Not logged in"))
        return try {
            // Path: users -> {UID} -> registeredEvents -> {eventId}
            firestore.collection("users")
                .document(user.uid)
                .collection("registeredEvents")
                .document(eventId)
                .set(mapOf(
                    "confId" to confId,
                    "timestamp" to FieldValue.serverTimestamp
                ))
            Result.success(Unit)
        } catch (e: Exception) {
            println("Error saving to schedule: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun removeFromSchedule(confId: String, eventId: String): Result<Unit> {
        val user = Firebase.auth.currentUser ?: return Result.failure(Exception("Not logged in"))

        return try {
            // 1. Call your unregister logic to decrement the count in the conference
            val unregisterResult = unregisterFromEvent(confId, eventId)
            if (unregisterResult.isSuccess) {
                // 2. Remove from the user's private list
                firestore.collection("users")
                    .document(user.uid)
                    .collection("registeredEvents")
                    .document(eventId)
                    .delete()

                Result.success(Unit)
            }
            else
            {
                Result.failure(unregisterResult.exceptionOrNull() ?: Exception("Unknown Error"))
            }
        } catch (e: Exception) {
            println("Error removing from schedule: ${e.message}")
            Result.failure(e)
        }
    }
    /**
     * Returns a stream of event IDs the user has registered for
     */
    fun getRegisteredEventIds(): Flow<Set<String>> {
        val user = Firebase.auth.currentUser ?: return flowOf(emptySet())
        return firestore.collection("users")
            .document(user.uid)
            .collection("registeredEvents")
            .snapshots
            .map { snapshot -> snapshot.documents.map { it.id }.toSet() }
    }

    suspend fun ensureAnonymousAuth(): String? {
        val auth = Firebase.auth
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                currentUser.uid
            } else {
                // This creates a "Guest" account in the background
                val result = auth.signInAnonymously()
                result.user?.uid
            }
        } catch (e: Exception) {
            println("Auth Error: ${e.message}")
            null
        }
    }
    suspend fun unregisterFromEvent(confId: String, eventId: String): Result<Unit> {
        val eventRef = getEventCollection(confId).document(eventId)
            return try {
                firestore.runTransaction {
                    // 'it' represents the Transaction object automatically
                    val snapshot = get(eventRef)

                    // Use 'snapshot.get("field")' or 'snapshot.getLong("field")'
                    val currentCount: Int = snapshot.get("registeredCount") ?: 0

                    if (currentCount > 0) {
                        update(eventRef, mapOf("registeredCount" to (currentCount - 1)))
                    }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
    }

    suspend fun forceResetEventCount(confId: String, eventId: String, newCount: Int): Result<Unit> {
        return try {
            // We don't need a transaction here because the Admin's word is law
            getEventCollection(confId)
                .document(eventId)
                .update(mapOf("registeredCount" to newCount))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateConferenceName(joinCode: String, newName: String): Result<Unit> {
        return try {
            firestore.collection("conferences")
                .document(joinCode)
                .update(mapOf("name" to newName))
            Result.success(Unit)
        } catch (e: Exception) {
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

