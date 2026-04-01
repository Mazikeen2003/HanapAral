package com.example.hanaparal.data.remote

import com.example.hanaparal.data.model.GroupMember
import com.example.hanaparal.data.model.StudyGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreSource {
    private val db = FirebaseFirestore.getInstance()

    fun getFirestore(): FirebaseFirestore = db

    suspend fun createGroup(group: StudyGroup) {
        db.collection("groups")
            .document(group.groupId)
            .set(group)
            .await()
    }

    suspend fun getAllGroups(): List<StudyGroup> {
        val snapshot = db.collection("groups").get().await()
        return snapshot.documents.mapNotNull { it.toObject(StudyGroup::class.java) }
    }

    suspend fun getGroupById(groupId: String): StudyGroup? {
        val doc = db.collection("groups").document(groupId).get().await()
        return doc.toObject(StudyGroup::class.java)
    }

    suspend fun addMember(groupId: String, member: GroupMember) {
        db.collection("groups")
            .document(groupId)
            .collection("members")
            .document(member.uid)
            .set(member)
            .await()
    }

    suspend fun getMembers(groupId: String): List<GroupMember> {
        val snapshot = db.collection("groups")
            .document(groupId)
            .collection("members")
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toObject(GroupMember::class.java) }
    }

    suspend fun isMemberAlreadyJoined(groupId: String, uid: String): Boolean {
        val doc = db.collection("groups")
            .document(groupId)
            .collection("members")
            .document(uid)
            .get()
            .await()
        return doc.exists()
    }

    suspend fun getMemberCount(groupId: String): Int {
        val snapshot = db.collection("groups")
            .document(groupId)
            .collection("members")
            .get()
            .await()
        return snapshot.size()
    }

    suspend fun getUserProfile(uid: String): Map<String, Any>? {
        val doc = db.collection("users").document(uid).get().await()
        return if (doc.exists()) doc.data else null
    }

    fun saveFcmToken(token: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("users").document(userId)
            .update("fcmToken", token)
    }

    // Admin / Superuser Logic
    fun checkIfSuperuser(uid: String, callback: (Boolean) -> Unit) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                // Check both field names to be safe
                val isSuper = doc.getBoolean("isSuperuser") ?: doc.getBoolean("isAdmin") ?: false
                callback(isSuper)
            }
            .addOnFailureListener { callback(false) }
    }

    fun getAllUsers(callback: (List<Map<String, Any>>) -> Unit) {
        db.collection("users").get()
            .addOnSuccessListener { snapshot ->
                callback(snapshot.documents.mapNotNull { it.data?.plus("uid" to it.id) })
            }
            .addOnFailureListener { callback(emptyList()) }
    }

    fun updateUserRole(uid: String, isSuperuser: Boolean, callback: (Boolean) -> Unit) {
        // Update both fields for consistency
        val updates = mapOf("isSuperuser" to isSuperuser, "isAdmin" to isSuperuser)
        db.collection("users").document(uid).update(updates)
            .addOnCompleteListener { callback(it.isSuccessful) }
    }

    fun sendBroadcast(title: String, message: String, callback: (Boolean) -> Unit) {
        val broadcast = hashMapOf("title" to title, "body" to message, "timestamp" to System.currentTimeMillis())
        db.collection("broadcasts").add(broadcast)
            .addOnCompleteListener { callback(it.isSuccessful) }
    }

    fun observeLatestBroadcast(): Flow<Map<String, Any>> = callbackFlow {
        val listener = db.collection("broadcasts")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val doc = snapshot?.documents?.firstOrNull()
                if (doc != null) {
                    trySend(doc.data ?: emptyMap())
                }
            }
        awaitClose { listener.remove() }
    }

    fun observeGlobalSettings(): Flow<Map<String, Any>> {
        val settingsFlow = MutableStateFlow<Map<String, Any>>(emptyMap())
        db.collection("settings").document("global_config")
            .addSnapshotListener { snapshot, _ ->
                snapshot?.data?.let { settingsFlow.value = it }
            }
        return settingsFlow
    }

    fun updateGlobalSetting(key: String, value: Any, callback: (Boolean) -> Unit) {
        // Use set with merge to ensure document is created if it doesn't exist
        db.collection("settings").document("global_config")
            .set(mapOf(key to value), SetOptions.merge())
            .addOnCompleteListener { callback(it.isSuccessful) }
    }

    fun deleteGroup(groupId: String, callback: (Boolean) -> Unit) {
        db.collection("groups").document(groupId).delete()
            .addOnCompleteListener { callback(it.isSuccessful) }
    }
}
