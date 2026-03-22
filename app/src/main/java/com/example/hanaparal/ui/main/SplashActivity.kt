package com.example.hanaparal.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.hanaparal.ui.auth.LoginActivity
import com.example.hanaparal.ui.main.DashboardActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔥 Check if user is already logged in
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            // ✅ User already signed in → go to Dashboard
            startActivity(Intent(this, DashboardActivity::class.java))
        } else {
            // ❌ Not signed in → go to Login
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // Close SplashActivity
        finish()
    }
}
