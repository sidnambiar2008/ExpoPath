package org.communityday.navigation.events.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.util.Calendar

actual class NotificationScheduler actual constructor() {
    companion object {
        lateinit var context: Context
        const val CHANNEL_ID = "event_reminders"
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Event Reminders"
            val descriptionText = "Notifications for upcoming events"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    actual fun requestPermissions() {
        // On Android, the UI (Activity) usually handles the actual popup.
        // But we can check status here.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val status = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            if (status != PackageManager.PERMISSION_GRANTED) {
                // Note: In a real app, you'd trigger an Activity Result Launcher here.
                println("Android: Notification permission not granted yet.")
            }
        }
    }

    actual fun scheduleEventNotification(id: String, title: String, startTime: String, dateString: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create the notification visual
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Event Starting Soon!")
            .setContentText("$title starts at $startTime")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        // For your USC project: This fires IMMEDIATELY.
        // To fire later, you'd wrap this in an AlarmManager.
        notificationManager.notify(id.hashCode(), builder.build())
    }

    actual fun cancelNotification(id: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(id.hashCode())
    }
}