package org.communityday.navigation.events.di

import org.communityday.navigation.events.data.EventRepository
import org.communityday.navigation.events.data.EventService
import org.communityday.navigation.events.data.EventServiceFactory

object ServiceLocator {
    private val eventService: EventService by lazy {
        EventServiceFactory().createEventService()
    }
    
    val eventRepository: EventRepository by lazy {
        EventRepository(eventService)
    }
}
