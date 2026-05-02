package org.communityday.navigation.events.notifications

actual class NotificationScheduler actual constructor() {
    actual fun scheduleEventNotification(id: String, title: String, startTime: String, dateString: String,) {}
    actual fun cancelNotification(id: String) {}
    actual fun requestPermissions() {}
}