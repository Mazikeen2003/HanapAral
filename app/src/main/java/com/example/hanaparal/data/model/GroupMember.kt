package com.example.hanaparal.data.model

import com.google.firebase.Timestamp

data class GroupMember(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "member", // "admin" or "member"
    val joinedAt: Timestamp? = null
)
