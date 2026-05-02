package org.communityday.navigation.events.notifications

expect class NotificationScheduler() { // Keep constructor empty here
    fun requestPermissions()
    fun scheduleEventNotification(id: String, title: String, startTime: String, dateString: String,)
    fun cancelNotification(id: String)
}