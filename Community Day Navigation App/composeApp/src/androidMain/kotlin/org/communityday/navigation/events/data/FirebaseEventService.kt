package org.communityday.navigation.events.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseEventService : EventService {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val eventsCollection = firestore.collection("events")
    
    override suspend fun getAllEvents(): Result<List<Event>> {
        return try {
            val snapshot = eventsCollection.get().await()
            val events = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Event::class.java)?.copy(id = doc.id)
            }
            Result.success(events)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getEventById(id: String): Result<Event?> {
        return try {
            val document = eventsCollection.document(id).get().await()
            val event = document.toObject(Event::class.java)?.copy(id = document.id)
            Result.success(event)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getEventsByCategory(category: EventCategory): Result<List<Event>> {
        return try {
            val snapshot = eventsCollection
                .whereEqualTo("category", category.name)
                .get()
                .await()
            val events = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Event::class.java)?.copy(id = doc.id)
            }
            Result.success(events)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getEventsStream(): Flow<List<Event>> = callbackFlow {
        val listener = eventsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            
            val events = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Event::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            
            trySend(events)
        }
        
        awaitClose { listener.remove() }
    }
    
    override suspend fun registerForEvent(eventId: String, userId: String): Result<Boolean> {
        return try {
            val eventDoc = eventsCollection.document(eventId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(eventDoc)
                val currentRegistered = snapshot.getLong("registeredCount")?.toInt() ?: 0
                val capacity = snapshot.getLong("capacity")?.toInt()
                
                if (capacity != null && currentRegistered >= capacity) {
                    throw Exception("Event is fully booked")
                }
                
                transaction.update(eventDoc, "registeredCount", currentRegistered + 1)
                
                // Add user to registered users list
                val registration = mapOf(
                    "userId" to userId,
                    "registeredAt" to System.currentTimeMillis(),
                    "eventId" to eventId
                )
                firestore.collection("registrations").add(registration)
            }.await()
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
