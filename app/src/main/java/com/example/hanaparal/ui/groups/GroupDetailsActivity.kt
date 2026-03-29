package com.example.hanaparal.ui.groups

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hanaparal.data.remote.FirestoreSource
import com.example.hanaparal.ui.theme.HanapAralTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class GroupDetailsActivity : ComponentActivity() {
    private val firestoreSource = FirestoreSource()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val groupId = intent.getStringExtra("groupId") ?: return finish()

        setContent {
            HanapAralTheme {
                var groupData by remember { mutableStateOf<Map<String, Any>?>(null) }
                var isLoading by remember { mutableStateOf(true) }
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                
                // Observe global settings for member limit
                val settings by firestoreSource.observeGlobalSettings().collectAsState(initial = emptyMap())
                val maxMembers = (settings["max_members_per_group"] as? Long) ?: 10L

                LaunchedEffect(Unit) {
                    FirebaseFirestore.getInstance().collection("groups").document(groupId)
                        .addSnapshotListener { snapshot, _ ->
                            isLoading = false
                            groupData = snapshot?.data
                        }
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Group Details") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            },
                            actions = {
                                // Delete button for the owner
                                val adminId = groupData?.get("adminId") as? String
                                if (adminId == uid) {
                                    IconButton(onClick = {
                                        firestoreSource.deleteGroup(groupId) { success ->
                                            if (success) {
                                                Toast.makeText(this@GroupDetailsActivity, "Group Deleted", Toast.LENGTH_SHORT).show()
                                                finish()
                                            }
                                        }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Group", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        groupData?.let { g ->
                            val members = g["members"] as? List<*> ?: emptyList<Any>()
                            val isMember = members.contains(uid)
                            val name = g["name"] as? String ?: ""
                            val description = g["description"] as? String ?: ""

                            Column(modifier = Modifier.padding(innerPadding).padding(24.dp)) {
                                Text(text = name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = description, style = MaterialTheme.typography.bodyLarge)
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(text = "Members: ${members.size} / $maxMembers", style = MaterialTheme.typography.titleMedium)
                                
                                Spacer(modifier = Modifier.weight(1f))

                                val canJoin = !isMember && members.size < maxMembers

                                Button(
                                    onClick = {
                                        if (canJoin && uid != null) {
                                            FirebaseFirestore.getInstance().collection("groups").document(groupId)
                                                .update("members", FieldValue.arrayUnion(uid))
                                                .addOnSuccessListener {
                                                    Toast.makeText(this@GroupDetailsActivity, "Joined Group!", Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                    },
                                    enabled = canJoin,
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        when {
                                            isMember -> "Already a Member"
                                            members.size >= maxMembers -> "Group is Full"
                                            else -> "Join Study Group"
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
