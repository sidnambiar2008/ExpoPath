package org.communityday.navigation.events.data

actual class EventServiceFactory {
    actual fun createEventService(): EventService {
        return FirebaseEventService()
    }
}
