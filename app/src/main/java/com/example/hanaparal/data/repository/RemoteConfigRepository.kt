package com.example.hanaparal.data.repository

import com.example.hanaparal.data.remote.RemoteConfigSource

class RemoteConfigRepository(private val remoteConfigSource: RemoteConfigSource) {
    
    fun fetchAndActivate(onComplete: (Boolean) -> Unit) {
        remoteConfigSource.fetchAndActivate(onComplete)
    }

    fun isGroupCreationEnabled(): Boolean = remoteConfigSource.isGroupCreationEnabled()
    fun getAnnouncementHeader(): String = remoteConfigSource.getAnnouncementHeader()
    fun getMaxMembersPerGroup(): Long = remoteConfigSource.getMaxMembersPerGroup()
    fun isStudyReminderEnabled(): Boolean = remoteConfigSource.isStudyReminderEnabled()
}
