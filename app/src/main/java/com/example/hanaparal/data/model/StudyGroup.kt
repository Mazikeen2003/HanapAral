package com.example.hanaparal.data.model

import com.google.firebase.Timestamp

data class StudyGroup(
    val groupId: String = "",
    val title: String = "",
    val subject: String = "",
    val adminId: String = "",
    val adminName: String = "",
    val maxMembers: Long = 10L,
    val createdAt: Timestamp? = null,
    val announcementHeader: String = ""
)
