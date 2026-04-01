package com.example.hanaparal.data.remote

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.example.hanaparal.R

class RemoteConfigSource {
    private val remoteConfig = FirebaseRemoteConfig.getInstance()

    companion object {
        const val GROUP_CREATION_ENABLED = "group_creation_enabled"
        const val GLOBAL_ANNOUNCEMENT_HEADER = "global_announcement_header"
        const val MAX_MEMBERS_PER_GROUP = "max_members_per_group"
        const val STUDY_REMINDER_ENABLED = "study_reminder_enabled"
    }

    init {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        
        // Set default values
        val defaults = mapOf(
            GROUP_CREATION_ENABLED to true,
            GLOBAL_ANNOUNCEMENT_HEADER to "Welcome to HanapAral!",
            MAX_MEMBERS_PER_GROUP to 10L,
            STUDY_REMINDER_ENABLED to true
        )
        remoteConfig.setDefaultsAsync(defaults)
    }

    fun fetchAndActivate(onComplete: (Boolean) -> Unit) {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val updated = task.result
                    Log.d("RemoteConfig", "Config params updated: $updated")
                    onComplete(true)
                } else {
                    Log.e("RemoteConfig", "Fetch failed")
                    onComplete(false)
                }
            }
    }

    fun isGroupCreationEnabled(): Boolean = remoteConfig.getBoolean(GROUP_CREATION_ENABLED)
    fun getAnnouncementHeader(): String = remoteConfig.getString(GLOBAL_ANNOUNCEMENT_HEADER)
    fun getMaxMembersPerGroup(): Long = remoteConfig.getLong(MAX_MEMBERS_PER_GROUP)
    fun isStudyReminderEnabled(): Boolean = remoteConfig.getBoolean(STUDY_REMINDER_ENABLED)
}
