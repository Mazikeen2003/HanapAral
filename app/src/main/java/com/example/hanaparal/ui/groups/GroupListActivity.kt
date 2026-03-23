package com.example.hanaparal.ui.groups

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hanaparal.data.model.StudyGroup

@Composable
fun GroupListActivity(
    onGroupClick: (StudyGroup) -> Unit,
    viewModel: GroupViewModel = viewModel()
) {
    val groups by viewModel.groups.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadGroups()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Study Groups",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        if (loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(groups) { group ->
                    GroupCard(group = group, onClick = { onGroupClick(group) })
                }
            }
        }
    }
}

@Composable
fun GroupCard(group: StudyGroup, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = group.title, style = MaterialTheme.typography.titleMedium)
            Text(text = group.subject, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Admin: ${group.adminName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}