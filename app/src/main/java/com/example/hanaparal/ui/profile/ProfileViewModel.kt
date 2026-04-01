package com.example.hanaparal.ui.profile

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class UserProfile(
    val name: String = "",
    val email: String = "",
    val course: String = "",
    val year: String = "",
    val photoUrl: String = "",
    val isAdmin: Boolean = false // Added isAdmin field
)

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var profileListener: ListenerRegistration? = null

    private val _profile = MutableStateFlow(UserProfile())
    val profile: StateFlow<UserProfile> = _profile

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        startProfileListener()
    }

    private fun startProfileListener() {
        val user = auth.currentUser ?: return
        val uid = user.uid
        _isLoading.value = true
        
        profileListener = db.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                _isLoading.value = false
                if (error != null) return@addSnapshotListener
                
                if (snapshot != null && snapshot.exists()) {
                    _profile.value = UserProfile(
                        name = snapshot.getString("name") ?: user.displayName ?: "",
                        email = snapshot.getString("email") ?: user.email ?: "",
                        course = snapshot.getString("course") ?: "",
                        year = snapshot.getString("year") ?: "",
                        photoUrl = snapshot.getString("photoUrl") ?: user.photoUrl?.toString() ?: "",
                        isAdmin = snapshot.getBoolean("isAdmin") ?: snapshot.getBoolean("isSuperuser") ?: false
                    )
                } else {
                    // Document doesn't exist yet, use Firebase Auth data as fallback
                    _profile.value = UserProfile(
                        name = user.displayName ?: "",
                        email = user.email ?: "",
                        photoUrl = user.photoUrl?.toString() ?: "",
                        isAdmin = false
                    )
                }
            }
    }

    fun updateProfile(name: String, course: String, year: String, onComplete: (Boolean) -> Unit) {
        val user = auth.currentUser ?: return
        val uid = user.uid
        _isLoading.value = true

        val updates = mutableMapOf(
            "name" to name,
            "course" to course,
            "year" to year,
            "email" to (user.email ?: "")
        )
        
        // Include photoUrl if it's available in Auth but not in Firestore yet
        if (user.photoUrl != null) {
            updates["photoUrl"] = user.photoUrl.toString()
        }

        // Use SetOptions.merge() so it creates the document if it doesn't exist
        db.collection("users").document(uid).set(updates, SetOptions.merge())
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                _isLoading.value = false
                onComplete(false)
            }
    }

    override fun onCleared() {
        super.onCleared()
        profileListener?.remove()
    }
}
