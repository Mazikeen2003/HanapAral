package com.example.hanaparal.data.repository

import com.example.hanaparal.data.model.GroupMember
import com.example.hanaparal.data.model.StudyGroup
import com.example.hanaparal.data.remote.FirestoreSource
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GroupRepository(
    private val firestoreSource: FirestoreSource = FirestoreSource()
) {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun createGroup(title: String, subject: String, maxMembers: Long): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("Not logged in"))
            
            // SECURITY CHECK: Direct fetch from Firestore to ensure we have the LATEST toggle state
            val settings = firestoreSource.getGlobalSettingsOnce()
            val isEnabled = settings["group_creation_enabled"] as? Boolean ?: true
            
            if (!isEnabled) {
                return Result.failure(Exception("Creation Disabled: The administrator has temporarily disabled group creation."))
            }

            val groupRef = db.collection("groups").document()
            val groupId = groupRef.id

            val group = StudyGroup(
                groupId = groupId,
                title = title,
                subject = subject,
                adminId = user.uid,
                adminName = user.displayName ?: "Unknown",
                maxMembers = maxMembers,
                createdAt = Timestamp.now()
            )

            firestoreSource.createGroup(group)

            val adminMember = GroupMember(
                uid = user.uid,
                name = user.displayName ?: "Unknown",
                email = user.email ?: "",
                role = "admin",
                joinedAt = Timestamp.now()
            )
            
            firestoreSource.addMember(groupId, adminMember)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllGroups(): Result<List<StudyGroup>> {
        return try {
            val groups = firestoreSource.getAllGroups()
            Result.success(groups)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinGroup(groupId: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("Not logged in"))

            val group = firestoreSource.getGroupById(groupId) 
                ?: return Result.failure(Exception("Group not found"))

            val alreadyJoined = firestoreSource.isMemberAlreadyJoined(groupId, user.uid)
            if (alreadyJoined) return Result.failure(Exception("You have already joined this group."))

            val currentCount = firestoreSource.getMemberCount(groupId)
            if (currentCount >= group.maxMembers) {
                return Result.failure(Exception("Group is already full."))
            }

            val member = GroupMember(
                uid = user.uid,
                name = user.displayName ?: "Unknown",
                email = user.email ?: "",
                role = "member",
                joinedAt = Timestamp.now()
            )
            firestoreSource.addMember(groupId, member)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMembers(groupId: String): Result<List<GroupMember>> {
        return try {
            val members = firestoreSource.getMembers(groupId)
            Result.success(members)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
