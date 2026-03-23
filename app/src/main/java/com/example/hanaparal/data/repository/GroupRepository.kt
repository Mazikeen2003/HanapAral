package com.example.hanaparal.data.repository

import com.example.hanaparal.data.model.GroupMember
import com.example.hanaparal.data.model.StudyGroup
import com.example.hanaparal.data.remote.FirestoreSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GroupRepository(
    private val firestoreSource: FirestoreSource = FirestoreSource()
) {
    private val auth = FirebaseAuth.getInstance()

    suspend fun createGroup(title: String, subject: String, maxMembers: Int): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("Not logged in"))
            val groupId = FirebaseFirestore.getInstance().collection("groups").document().id

            val group = StudyGroup(
                groupId = groupId,
                title = title,
                subject = subject,
                adminId = user.uid,
                adminName = user.displayName ?: "Unknown",
                maxMembers = maxMembers,
                createdAt = System.currentTimeMillis()
            )

            firestoreSource.createGroup(group)

            // Auto-assign creator as admin member
            val adminMember = GroupMember(
                uid = user.uid,
                name = user.displayName ?: "Unknown",
                email = user.email ?: "",
                role = "admin",
                joinedAt = System.currentTimeMillis()
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

    suspend fun joinGroup(groupId: String, maxMembers: Int): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("Not logged in"))

            // Duplicate join prevention
            val alreadyJoined = firestoreSource.isMemberAlreadyJoined(groupId, user.uid)
            if (alreadyJoined) return Result.failure(Exception("You have already joined this group."))

            // Max member restriction
            val currentCount = firestoreSource.getMemberCount(groupId)
            if (currentCount >= maxMembers) return Result.failure(Exception("Group is already full."))

            val member = GroupMember(
                uid = user.uid,
                name = user.displayName ?: "Unknown",
                email = user.email ?: "",
                role = "member",
                joinedAt = System.currentTimeMillis()
            )
            firestoreSource.addMember(groupId, member)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGroupDetails(groupId: String): Result<StudyGroup?> {
        return try {
            val group = firestoreSource.getGroupById(groupId)
            Result.success(group)
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