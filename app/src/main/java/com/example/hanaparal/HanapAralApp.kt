package com.example.hanaparal

import android.app.Application
import com.google.firebase.FirebaseApp

class HanapAralApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
    }
}
