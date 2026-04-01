package com.example.hanaparal.data.model

data class StudyGroup(
    val groupId: String = "",
    val title: String = "",
    val subject: String = "",
    val adminId: String = "",
    val adminName: String = "",
    val maxMembers: Int = 10,
    val createdAt: Long = 0L,
    val announcementHeader: String = ""
)