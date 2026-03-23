package com.example.hanaparal.data.remote

import com.example.hanaparal.data.model.GroupMember
import com.example.hanaparal.data.model.StudyGroup
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreSource {
    private val db = FirebaseFirestore.getInstance()

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
}