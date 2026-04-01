package com.example.hanaparal.ui.groups

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hanaparal.data.model.GroupMember

@Composable
fun GroupDetailsActivity(
    groupId: String,
    maxMembers: Int = 10,
    viewModel: GroupViewModel = viewModel()
) {
    val members by viewModel.members.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val success by viewModel.success.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadMembers(groupId)
    }

    LaunchedEffect(success) {
        if (success != null) {
            viewModel.loadMembers(groupId)
            viewModel.clearMessages()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Group Details",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Members",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        success?.let {
            Text(text = it, color = MaterialTheme.colorScheme.primary)
        }

        if (loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(members) { member ->
                    MemberCard(member = member)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.joinGroup(groupId, maxMembers) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Join Group")
        }
    }
}

@Composable
fun MemberCard(member: GroupMember) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = member.name, style = MaterialTheme.typography.titleSmall)
                Text(text = member.email, style = MaterialTheme.typography.bodySmall)
            }
            Text(
                text = if (member.role == "admin") "Admin" else "Member",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
