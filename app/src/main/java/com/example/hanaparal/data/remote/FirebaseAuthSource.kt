package com.example.hanaparal.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class FirebaseAuthSource {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
    
    fun signOut() {
        auth.signOut()
    }
}
