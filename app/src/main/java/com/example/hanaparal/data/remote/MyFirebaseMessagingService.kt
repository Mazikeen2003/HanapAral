package com.example.hanaparal.data.remote

import android.util.Log
import com.example.hanaparal.utils.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private lateinit var notificationHelper: NotificationHelper

    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(applicationContext)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d("FCM", "From: ${remoteMessage.from}")

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d("FCM", "Message Notification Body: ${it.body}")
            val channelId = remoteMessage.data["channel_id"] ?: NotificationHelper.CHANNEL_GROUP_UPDATES
            notificationHelper.showNotification(
                channelId,
                it.title ?: "HanapAral Update",
                it.body ?: ""
            )
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Refreshed token: $token")
        // TODO: Store token in Firestore if user is logged in
    }
}
