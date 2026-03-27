package com.example.hanaparal.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hanaparal.data.model.GroupMember
import com.example.hanaparal.data.model.StudyGroup
import com.example.hanaparal.data.repository.GroupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GroupViewModel(
    private val repository: GroupRepository = GroupRepository()
) : ViewModel() {

    private val _groups = MutableStateFlow<List<StudyGroup>>(emptyList())
    val groups: StateFlow<List<StudyGroup>> = _groups

    private val _members = MutableStateFlow<List<GroupMember>>(emptyList())
    val members: StateFlow<List<GroupMember>> = _members

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _success = MutableStateFlow<String?>(null)
    val success: StateFlow<String?> = _success

    fun loadGroups() {
        viewModelScope.launch {
            _loading.value = true
            val result = repository.getAllGroups()
            _loading.value = false
            result.onSuccess { _groups.value = it }
            result.onFailure { _error.value = it.message }
        }
    }

    fun createGroup(title: String, subject: String, maxMembers: Int) {
        viewModelScope.launch {
            _loading.value = true
            val result = repository.createGroup(title, subject, maxMembers)
            _loading.value = false
            result.onSuccess { _success.value = "Group created successfully!" }
            result.onFailure { _error.value = it.message }
        }
    }

    fun joinGroup(groupId: String, maxMembers: Int) {
        viewModelScope.launch {
            _loading.value = true
            val result = repository.joinGroup(groupId, maxMembers)
            _loading.value = false
            result.onSuccess { _success.value = "Joined group successfully!" }
            result.onFailure { _error.value = it.message }
        }
    }

    fun loadMembers(groupId: String) {
        viewModelScope.launch {
            _loading.value = true
            val result = repository.getMembers(groupId)
            _loading.value = false
            result.onSuccess { _members.value = it }
            result.onFailure { _error.value = it.message }
        }
    }

    fun clearMessages() {
        _error.value = null
        _success.value = null
    }
}