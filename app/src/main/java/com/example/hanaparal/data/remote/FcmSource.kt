package com.example.hanaparal.data.remote

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging

class FcmSource(private val firestoreSource: FirestoreSource) {
    private val fcm = FirebaseMessaging.getInstance()

    fun fetchAndStoreToken() {
        fcm.token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FcmSource", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            Log.d("FcmSource", "FCM Token: $token")
            
            // Store in Firestore
            firestoreSource.saveFcmToken(token)
        }
    }

    fun subscribeToTopic(topic: String) {
        fcm.subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FcmSource", "Subscribed to $topic")
                }
            }
    }

    fun unsubscribeFromTopic(topic: String) {
        fcm.unsubscribeFromTopic(topic)
    }
}
