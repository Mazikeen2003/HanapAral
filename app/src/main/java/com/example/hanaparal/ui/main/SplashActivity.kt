package com.example.hanaparal.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.hanaparal.ui.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Check authentication
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            // ✅ SUCCESS: Go to MainActivity (Where Bon's Complete UI is located)
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // ❌ FAIL: Go to Login screen
            startActivity(Intent(this, LoginActivity::class.java))
        }

        finish()
    }
}
