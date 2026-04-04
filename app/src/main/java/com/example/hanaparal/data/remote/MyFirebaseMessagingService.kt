package com.example.hanaparal.data.remote

import android.util.Log
import com.example.hanaparal.utils.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d("FCM", "From: ${remoteMessage.from}")

        // 1. Handle Notification Payload (Para sa Firebase Console messages)
        remoteMessage.notification?.let {
            showNotification(it.title ?: "HanapAral", it.body ?: "")
        }

        // 2. Handle Data Payload (Para sa custom logic)
        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"] ?: "HanapAral Update"
            val body = remoteMessage.data["body"] ?: ""
            showNotification(title, body)
        }
    }

    private fun showNotification(title: String, body: String) {
        val notificationHelper = NotificationHelper(applicationContext)
        notificationHelper.showNotification(
            NotificationHelper.CHANNEL_ADMIN_NOTICES,
            title,
            body
        )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
    }
}
