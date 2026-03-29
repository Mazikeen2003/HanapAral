package com.example.hanaparal.ui.groups

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hanaparal.data.remote.FirestoreSource
import com.example.hanaparal.ui.theme.HanapAralTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateGroupActivity : ComponentActivity() {
    private val firestoreSource = FirestoreSource()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HanapAralTheme {
                var groupName by remember { mutableStateOf("") }
                var description by remember { mutableStateOf("") }
                var isLoading by remember { mutableStateOf(false) }
                
                // Observe global settings
                val settings by firestoreSource.observeGlobalSettings().collectAsState(initial = emptyMap())
                val isCreationEnabled = settings["group_creation_enabled"] as? Boolean ?: true

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Create Study Group") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    if (!isCreationEnabled) {
                        Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                            Text("Group creation is currently disabled by administrator.", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .padding(innerPadding)
                                .padding(24.dp)
                                .fillMaxSize()
                        ) {
                            OutlinedTextField(
                                value = groupName,
                                onValueChange = { groupName = it },
                                label = { Text("Group Name") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { Text("Description") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(32.dp))

                            if (isLoading) {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            } else {
                                Button(
                                    onClick = {
                                        if (groupName.isNotEmpty()) {
                                            isLoading = true
                                            val uid = FirebaseAuth.getInstance().currentUser?.uid
                                            val group = hashMapOf(
                                                "name" to groupName,
                                                "description" to description,
                                                "adminId" to uid,
                                                "createdAt" to com.google.firebase.Timestamp.now(),
                                                "members" to listOf(uid)
                                            )
                                            FirebaseFirestore.getInstance().collection("groups")
                                                .add(group)
                                                .addOnSuccessListener {
                                                    Toast.makeText(this@CreateGroupActivity, "Group Created", Toast.LENGTH_SHORT).show()
                                                    finish()
                                                }
                                                .addOnFailureListener {
                                                    isLoading = false
                                                    Toast.makeText(this@CreateGroupActivity, "Error creating group", Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Create Group")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
