package com.example.hanaparal.data.model

data class StudyGroup(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val creatorId: String = "",
    val members: List<String> = emptyList()
)
