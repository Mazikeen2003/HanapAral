package com.example.hanaparal.ui.profile

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class UserProfile(
    val name: String = "",
    val email: String = "",
    val course: String = "",
    val year: String = "",
    val photoUrl: String = ""
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
        val uid = auth.currentUser?.uid ?: return
        _isLoading.value = true
        
        // Use a snapshot listener so changes reflect immediately across the app
        profileListener = db.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                _isLoading.value = false
                if (error != null) return@addSnapshotListener
                
                if (snapshot != null && snapshot.exists()) {
                    _profile.value = UserProfile(
                        name = snapshot.getString("name") ?: "",
                        email = snapshot.getString("email") ?: "",
                        course = snapshot.getString("course") ?: "",
                        year = snapshot.getString("year") ?: "",
                        photoUrl = snapshot.getString("photoUrl") ?: ""
                    )
                }
            }
    }

    fun updateProfile(name: String, course: String, year: String, onComplete: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        _isLoading.value = true
        val updates = mapOf(
            "name" to name,
            "course" to course,
            "year" to year
        )
        // Updating Firestore will trigger the SnapshotListener automatically
        db.collection("users").document(uid).update(updates)
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
