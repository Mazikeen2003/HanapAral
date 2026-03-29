package com.example.hanaparal.ui.groups

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hanaparal.ui.theme.HanapAralTheme
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class StudyGroup(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val members: List<String> = emptyList()
)

class GroupListActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HanapAralTheme {
                var groups by remember { mutableStateOf<List<StudyGroup>>(emptyList()) }
                var isLoading by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    FirebaseFirestore.getInstance().collection("groups")
                        .orderBy("createdAt", Query.Direction.DESCENDING)
                        .addSnapshotListener { snapshot, e ->
                            isLoading = false
                            if (e != null) return@addSnapshotListener
                            groups = snapshot?.documents?.map { doc ->
                                val membersRaw = doc.get("members")
                                val membersList = mutableListOf<String>()
                                if (membersRaw is List<*>) {
                                    membersRaw.forEach { item ->
                                        if (item is String) membersList.add(item)
                                    }
                                }
                                StudyGroup(
                                    id = doc.id,
                                    name = doc.getString("name") ?: "",
                                    description = doc.getString("description") ?: "",
                                    members = membersList
                                )
                            } ?: emptyList()
                        }
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Available Groups") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (groups.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No groups found. Be the first to create one!")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.padding(innerPadding).fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(groups) { group ->
                                GroupItem(group) {
                                    val intent = Intent(this@GroupListActivity, GroupDetailsActivity::class.java)
                                    intent.putExtra("groupId", group.id)
                                    startActivity(intent)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GroupItem(group: StudyGroup, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = group.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = group.description, maxLines = 2, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${group.members.size} Members",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
