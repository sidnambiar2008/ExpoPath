package org.communityday.navigation.events.notifications

import platform.UserNotifications.*

actual class NotificationScheduler actual constructor() {
    actual fun scheduleEventNotification(id: String, title: String, startTime: String) {
        val content = UNMutableNotificationContent().apply {
            setTitle("Event Starting Soon!")
            setBody("$title is about to start at $startTime")
            setSound(UNNotificationSound.defaultSound())
        }

        // Logic here to convert 'startTime' to NSDateComponents for the trigger
        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(5.0, false)

        val request = UNNotificationRequest.requestWithIdentifier(id, content, trigger)

        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { error ->
            if (error != null) println("Error scheduling notification: $error")
        }
    }

    actual fun cancelNotification(id: String) {
        UNUserNotificationCenter.currentNotificationCenter()
            .removePendingNotificationRequestsWithIdentifiers(listOf(id))
    }

    actual fun requestPermissions() {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        ) { granted, error ->
            if (granted) {
                println("Notifications allowed!")
            }
        }
    }
}