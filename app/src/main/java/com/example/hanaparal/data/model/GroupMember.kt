package com.example.hanaparal.data.model

data class GroupMember(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "member", // "admin" or "member"
    val joinedAt: Long = 0L
)