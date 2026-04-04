package com.example.hanaparal.ui.groups

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hanaparal.data.model.GroupMember
import com.example.hanaparal.data.model.StudyGroup
import com.google.firebase.auth.FirebaseAuth

@Composable
fun GroupDetailsActivity(
    group: StudyGroup,
    onBack: () -> Unit,
    viewModel: GroupViewModel = viewModel()
) {
    val members by viewModel.members.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val success by viewModel.success.collectAsState()
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser

    LaunchedEffect(group.groupId) {
        viewModel.loadMembers(group.groupId)
    }

    LaunchedEffect(success) {
        if (success != null) {
            Toast.makeText(context, success, Toast.LENGTH_SHORT).show()
            viewModel.loadMembers(group.groupId)
            viewModel.clearMessages()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Group Header Info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Subject: ${group.subject}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "Created by: ${group.adminName}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${members.size} / ${group.maxMembers} Members Joined",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Members List",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (loading) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
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

        // Join Button Logic
        val isAlreadyMember = members.any { it.uid == currentUser?.uid }
        val isFull = members.size >= group.maxMembers

        Button(
            // ALIGNED: Only groupId is passed to the ViewModel
            onClick = { viewModel.joinGroup(group.groupId) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !isAlreadyMember && !isFull && !loading
        ) {
            Text(
                text = when {
                    isAlreadyMember -> "Already a Member"
                    isFull -> "Group Full"
                    else -> "Join Study Group"
                },
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MemberCard(member: GroupMember) {
    ListItem(
        headlineContent = { Text(member.name, fontWeight = FontWeight.Bold) },
        supportingContent = { Text(member.email) },
        leadingContent = {
            Surface(
                color = if (member.role == "admin") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (member.role == "admin") Color.White else MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        trailingContent = {
            if (member.role == "admin") {
                Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                    Text("Admin", modifier = Modifier.padding(4.dp))
                }
            }
        },
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    )
}
