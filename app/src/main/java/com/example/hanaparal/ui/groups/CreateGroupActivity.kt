package com.example.hanaparal.ui.groups

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hanaparal.data.remote.FirestoreSource

@Composable
fun CreateGroupActivity(
    maxMembers: Long = 10L, // Aligned to Long to match Firestore numeric type
    onGroupCreated: () -> Unit,
    viewModel: GroupViewModel = viewModel()
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }

    val loading by viewModel.loading.collectAsState()
    val success by viewModel.success.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(success) {
        if (success != null) {
            onGroupCreated()
            viewModel.clearMessages()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Reduced top spacer to keep it near "Hello, Name" as requested previously
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Create Study Group",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Group Title") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("Subject") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // Ipakita ang limit na galing sa Admin Panel
        Text(
            text = "Group Member Limit: $maxMembers",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            Button(
                onClick = {
                    if (title.isNotBlank() && subject.isNotBlank()) {
                        // Ipinapasa na dito ang tamang limit (hal. 5)
                        viewModel.createGroup(title, subject, maxMembers)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Create Group", fontWeight = FontWeight.Bold)
            }
        }
    }
}