package com.example.hanaparal.data.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirestoreSource {
    private val db = FirebaseFirestore.getInstance()

    fun getFirestore(): FirebaseFirestore {
        return db
    }

    // --- User Management ---
    fun saveFcmToken(token: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("users").document(userId)
            .set(mapOf("fcmToken" to token), SetOptions.merge())
            .addOnSuccessListener { Log.d("FirestoreSource", "FCM token saved") }
    }

    fun getAllUsers(onResult: (List<Map<String, Any>>) -> Unit) {
        db.collection("users").get()
            .addOnSuccessListener { snapshot ->
                val users = snapshot.documents.mapNotNull { it.data?.plus("uid" to it.id) }
                onResult(users)
            }
            .addOnFailureListener {
                Log.e("FirestoreSource", "Error getting users", it)
            }
    }

    fun updateUserRole(uid: String, isSuperuser: Boolean, onComplete: (Boolean) -> Unit) {
        db.collection("users").document(uid)
            .update("isSuperuser", isSuperuser)
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    fun checkIfSuperuser(uid: String, onResult: (Boolean) -> Unit) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val isSuper = document.getBoolean("isSuperuser") ?: false
                onResult(isSuper)
            }
            .addOnFailureListener { onResult(false) }
    }

    // --- Global Settings ---
    fun observeGlobalSettings(): Flow<Map<String, Any>> = callbackFlow {
        val listener = db.collection("settings").document("global")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                snapshot?.data?.let { trySend(it) }
            }
        awaitClose { listener.remove() }
    }

    fun updateGlobalSetting(key: String, value: Any, onComplete: (Boolean) -> Unit) {
        db.collection("settings").document("global")
            .set(mapOf(key to value), SetOptions.merge())
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    // --- Announcements & Broadcasts ---
    fun sendBroadcast(title: String, body: String, onComplete: (Boolean) -> Unit) {
        val announcement = mapOf(
            "title" to title,
            "body" to body,
            "timestamp" to Timestamp.now()
        )
        db.collection("broadcasts").add(announcement)
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    fun observeLatestBroadcast(): Flow<Map<String, Any>> = callbackFlow {
        val query = db.collection("broadcasts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
        
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirestoreSource", "Error observing broadcasts", error)
                return@addSnapshotListener
            }
            val doc = snapshot?.documents?.firstOrNull()
            doc?.data?.let { trySend(it) }
        }
        awaitClose { listener.remove() }
    }

    // --- Group Management ---
    fun deleteGroup(groupId: String, onComplete: (Boolean) -> Unit) {
        db.collection("groups").document(groupId)
            .delete()
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }
}
