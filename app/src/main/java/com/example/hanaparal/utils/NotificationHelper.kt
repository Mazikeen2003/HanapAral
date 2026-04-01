package com.example.hanaparal.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.hanaparal.ui.main.MainActivity

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_STUDY_REMINDERS = "study_reminders"
        const val CHANNEL_GROUP_UPDATES = "group_updates"
        const val CHANNEL_ADMIN_NOTICES = "admin_notices"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_STUDY_REMINDERS,
                    "Study Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Notifications for study sessions" },
                NotificationChannel(
                    CHANNEL_GROUP_UPDATES,
                    "Group Updates",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "Notifications for group activities" },
                NotificationChannel(
                    CHANNEL_ADMIN_NOTICES,
                    "Admin Notices",
                    NotificationManager.IMPORTANCE_HIGH // Increased to high for better visibility
                ).apply { description = "Important announcements from admins" }
            )

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            channels.forEach { manager.createNotificationChannel(it) }
        }
    }

    fun showNotification(channelId: String, title: String, body: String) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Set to high
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(System.currentTimeMillis().toInt(), builder.build())
            }
        } catch (e: SecurityException) {
            // Handle cases where permission was revoked just before calling
        }
    }
}
