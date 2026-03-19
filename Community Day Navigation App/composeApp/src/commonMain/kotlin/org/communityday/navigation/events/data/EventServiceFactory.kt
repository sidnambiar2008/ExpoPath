package org.communityday.navigation.events.data

import io.ktor.client.HttpClient
import org.communityday.navigation.events.network.HttpClientFactory

expect class EventServiceFactory() {
    fun createEventService(): EventService
}

actual class EventServiceFactory {
    private val httpClient: HttpClient by lazy { HttpClientFactory.create() }
    
    actual fun createEventService(): EventService {
        return KtorEventService(httpClient)
    }
}
