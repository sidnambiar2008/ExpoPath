package org.communityday.navigation.events.notifications

import org.communityday.navigation.events.utils.convertTimeToMinutes
import platform.UserNotifications.*
import platform.Foundation.* //

actual class NotificationScheduler actual constructor() {
    actual fun scheduleEventNotification(id: String, title: String, startTime: String, dateString: String,) {
        val center = UNUserNotificationCenter.currentNotificationCenter()

        center.getNotificationSettingsWithCompletionHandler { settings ->
            if (settings?.authorizationStatus == UNAuthorizationStatusAuthorized) {

                // 1. Parse the Date and Time (Assuming "YYYY-MM-DD" and "10:30 AM")
                val dateParts = dateString.split("-") // [2026, 05, 01]
                if (dateParts.size < 3) return@getNotificationSettingsWithCompletionHandler

                // Use your existing helper to get 24-hour time
                val eventMinutes = convertTimeToMinutes(startTime)
                val eventHour = eventMinutes / 60
                val eventMinute = eventMinutes % 60

                // 2. Create Date Components for the trigger
                val components = NSDateComponents().apply {
                    setYear(dateParts[0].toLong())
                    setMonth(dateParts[1].toLong())
                    setDay(dateParts[2].toLong())
                    setHour((eventHour).toLong())
                    setMinute((eventMinute).toLong())
                    // Optional: Subtract 10 minutes for the buffer
                    // setMinute((eventMinute - 10).toLong())
                }

                // 3. Setup Content
                val content = UNMutableNotificationContent().apply {
                    setTitle("Event Starting Soon!")
                    setBody("$title is starting at $startTime")
                    setSound(UNNotificationSound.defaultSound())
                }

                // 4. Calendar Trigger (repeats = false)
                val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(components, false)

                val request = UNNotificationRequest.requestWithIdentifier(id, content, trigger)

                center.addNotificationRequest(request) { error ->
                    if (error != null) {
                        println("iOS Notification Error: ${error.localizedDescription}")
                    } else {
                        println("Notification Scheduled for $dateString at $startTime")
                    }
                }
            }
        }
    }

    actual fun cancelNotification(id: String) {
        // removePending... handles notifications that haven't fired yet
        // removeDelivered... handles notifications already sitting in the tray
        UNUserNotificationCenter.currentNotificationCenter().let { center ->
            center.removePendingNotificationRequestsWithIdentifiers(listOf(id))
            center.removeDeliveredNotificationsWithIdentifiers(listOf(id))
        }
    }

    actual fun requestPermissions() {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        ) { granted, error ->
            if (granted) {
                println("Notifications allowed!")
            } else if (error != null) {
                println("Error requesting permissions: ${error.localizedDescription}")
            }
        }
    }
}