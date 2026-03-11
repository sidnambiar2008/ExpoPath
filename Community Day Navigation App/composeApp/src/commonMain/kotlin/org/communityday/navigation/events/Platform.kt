package org.communityday.navigation.events

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform