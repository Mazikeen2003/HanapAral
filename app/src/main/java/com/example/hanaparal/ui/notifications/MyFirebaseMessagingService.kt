package com.example.hanaparal.ui.notifications

import android.util.Log
import com.example.hanaparal.utils.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d("FCM", "From: ${remoteMessage.from}")

        // 1. Handle Notification Payload (standard Firebase console messages)
        remoteMessage.notification?.let {
            showPopNotification(it.title ?: "HanapAral Notice", it.body ?: "")
        }

        // 2. Handle Data Payload (used for more complex/custom logic)
        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"] ?: "HanapAral Update"
            val body = remoteMessage.data["body"] ?: ""
            showPopNotification(title, body)
        }
    }

    private fun showPopNotification(title: String, body: String) {
        val notificationHelper = NotificationHelper(applicationContext)
        notificationHelper.showNotification(
            NotificationHelper.CHANNEL_ADMIN_NOTICES,
            title,
            body
        )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token generated: $token")
    }
}
