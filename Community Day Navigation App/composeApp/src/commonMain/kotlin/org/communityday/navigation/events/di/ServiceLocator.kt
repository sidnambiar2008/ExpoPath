package org.communityday.navigation.events.di

import org.communityday.navigation.events.data.EventRepository

expect object ServiceLocator {
    val eventRepository: EventRepository
}
