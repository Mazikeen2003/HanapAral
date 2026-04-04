package com.example.hanaparal

import android.app.Application
import com.example.hanaparal.utils.NotificationHelper
import com.google.firebase.FirebaseApp

class HanapAralApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Initialize Notification Channels early
        NotificationHelper(this)
    }
}
