package org.communityday.navigation.events.notifications

import org.communityday.navigation.events.utils.convertTimeToMinutes
import platform.UserNotifications.*
import platform.Foundation.* //

actual class NotificationScheduler actual constructor() {
    actual fun scheduleEventNotification(id: String, title: String, startTime: String, dateString: String) {
        val center = UNUserNotificationCenter.currentNotificationCenter()

        center.getNotificationSettingsWithCompletionHandler { settings ->
            if (settings?.authorizationStatus == UNAuthorizationStatusAuthorized) {

                val dateParts = dateString.split("-")
                if (dateParts.size < 3) return@getNotificationSettingsWithCompletionHandler

                val eventMinutes = convertTimeToMinutes(startTime)

                // --- Define our two offsets (in minutes) ---
                val alerts = listOf(
                    60 to "1 hour",    // 60 minutes before
                    10 to "10 minutes" // 10 minutes before
                )

                alerts.forEach { (offset, label) ->
                    var targetTotalMinutes = eventMinutes - offset

                    // Basic safety check: if event is at 12:05 AM, don't go to -55 mins
                    if (targetTotalMinutes < 0) targetTotalMinutes = 0

                    val finalHour = targetTotalMinutes / 60
                    val finalMinute = targetTotalMinutes % 60

                    val components = NSDateComponents().apply {
                        setYear(dateParts[0].toLong())
                        setMonth(dateParts[1].toLong())
                        setDay(dateParts[2].toLong())
                        setHour(finalHour.toLong())
                        setMinute(finalMinute.toLong())
                        setTimeZone(NSTimeZone.localTimeZone)
                    }

                    // Setup Content
                    val content = UNMutableNotificationContent().apply {
                        setTitle("Event Starting Soon!")
                        setBody("$title is starting in $label (at $startTime)")
                        setSound(UNNotificationSound.defaultSound())
                    }

                    val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(components, false)

                    // CRITICAL: Unique ID for each alert so they don't overwrite each other
                    val uniqueId = "${id}_$offset"
                    val request = UNNotificationRequest.requestWithIdentifier(uniqueId, content, trigger)

                    center.addNotificationRequest(request) { error ->
                        if (error == null) {
                            val paddedMinute = finalMinute.toString().padStart(2, '0')
                            println("Scheduling for: $finalHour:$paddedMinute")
                        }
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