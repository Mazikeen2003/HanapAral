package com.example.hanaparal.ui.admin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.hanaparal.data.remote.FirestoreSource
import com.example.hanaparal.ui.theme.HanapAralTheme
import com.google.firebase.auth.FirebaseAuth

class AdminPanelActivity : FragmentActivity() {

    private val firestoreSource = FirestoreSource()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAdminAndAuthenticate()
    }

    private fun checkAdminAndAuthenticate() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            finish()
            return
        }

        firestoreSource.checkIfSuperuser(user.uid) { isSuperuser ->
            if (isSuperuser) {
                showBiometricPrompt()
            } else {
                Toast.makeText(this, "Access Denied: You are not a Superuser", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext, "Auth error: $errString", Toast.LENGTH_SHORT).show()
                    finish()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    showAdminUI()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Biometric match failed", Toast.LENGTH_SHORT).show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Admin Verification")
            .setSubtitle("Authenticate to access system controls")
            .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .setConfirmationRequired(false)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    private fun showAdminUI() {
        setContent {
            HanapAralTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("System Administration") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    AdminScreen(
                        firestoreSource = firestoreSource,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun AdminScreen(firestoreSource: FirestoreSource, modifier: Modifier = Modifier) {
    var showBroadcastDialog by remember { mutableStateOf(false) }
    var showUserDialog by remember { mutableStateOf(false) }
    var showConfigDialog by remember { mutableStateOf(false) }
    var showGroupsDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(16.dp))
                    Text(
                        "Superuser Mode Active",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        item { Text("System Controls", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }

        item {
            AdminActionItem(
                title = "Push Notifications",
                subtitle = "Broadcast global announcements",
                icon = Icons.Default.Notifications,
                color = Color(0xFF673AB7),
                onClick = { showBroadcastDialog = true }
            )
        }
        item {
            AdminActionItem(
                title = "User Access Control",
                subtitle = "Manage superuser permissions",
                icon = Icons.Default.People,
                color = Color(0xFF2196F3),
                onClick = { showUserDialog = true }
            )
        }
        item {
            AdminActionItem(
                title = "Study Group Controls",
                subtitle = "Toggle creation and constraints",
                icon = Icons.Default.Settings,
                color = Color(0xFFFF9800),
                onClick = { showConfigDialog = true }
            )
        }
        item {
            AdminActionItem(
                title = "Delete Existing Groups",
                subtitle = "Cleanup unauthorized or inactive groups",
                icon = Icons.Default.DeleteForever,
                color = MaterialTheme.colorScheme.error,
                onClick = { showGroupsDialog = true }
            )
        }
    }

    if (showBroadcastDialog) {
        BroadcastDialog(firestoreSource) { showBroadcastDialog = false }
    }

    if (showUserDialog) {
        UserAccessDialog(firestoreSource) { showUserDialog = false }
    }

    if (showConfigDialog) {
        ConfigControlDialog(firestoreSource) { showConfigDialog = false }
    }

    if (showGroupsDialog) {
        ManageGroupsDialog(firestoreSource) { showGroupsDialog = false }
    }
}

@Composable
fun AdminActionItem(title: String, subtitle: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.Bold) },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Surface(
                color = color.copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color)
                }
            }
        },
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
    )
}

@Composable
fun BroadcastDialog(firestoreSource: FirestoreSource, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Global Broadcast") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = message, onValueChange = { message = it }, label = { Text("Message") }, modifier = Modifier.fillMaxWidth())
                if (isSending) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSending,
                onClick = {
                if (title.isNotEmpty() && message.isNotEmpty()) {
                    isSending = true
                    firestoreSource.sendBroadcast(title, message) { success ->
                        isSending = false
                        if (success) {
                            onDismiss()
                        }
                    }
                }
            }) { Text("Send Now") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun UserAccessDialog(firestoreSource: FirestoreSource, onDismiss: () -> Unit) {
    var users by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        firestoreSource.getAllUsers { 
            users = it
            isLoading = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("User Permissions") },
        text = {
            Box(modifier = Modifier.heightIn(max = 400.dp).fillMaxWidth()) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn {
                        items(users) { user ->
                            val uid = user["uid"] as? String ?: ""
                            val isSuper = user["isSuperuser"] as? Boolean ?: false
                            val name = user["name"] as? String ?: user["email"] as? String ?: "Unknown"
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(name, modifier = Modifier.weight(1f))
                                Switch(checked = isSuper, onCheckedChange = { checked ->
                                    firestoreSource.updateUserRole(uid, checked) { success ->
                                        if (success) {
                                            firestoreSource.getAllUsers { users = it }
                                        }
                                    }
                                })
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } }
    )
}

@Composable
fun ConfigControlDialog(firestoreSource: FirestoreSource, onDismiss: () -> Unit) {
    val settings by firestoreSource.observeGlobalSettings().collectAsState(initial = emptyMap())
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Group Settings") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                val enabled = settings["group_creation_enabled"] as? Boolean ?: true
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Enable Group Creation", modifier = Modifier.weight(1f))
                    Switch(checked = enabled, onCheckedChange = { checked ->
                        firestoreSource.updateGlobalSetting("group_creation_enabled", checked) {}
                    })
                }
                
                val maxMembers = (settings["max_members_per_group"] as? Long) ?: 10L
                Column {
                    Text("Max Members: $maxMembers")
                    Slider(
                        value = maxMembers.toFloat(),
                        onValueChange = { value -> firestoreSource.updateGlobalSetting("max_members_per_group", value.toLong()) {} },
                        valueRange = 2f..50f,
                        steps = 48
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
fun ManageGroupsDialog(firestoreSource: FirestoreSource, onDismiss: () -> Unit) {
    var groups by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        firestoreSource.getFirestore().collection("groups").get()
            .addOnSuccessListener { snapshot ->
                groups = snapshot.documents.mapNotNull { it.data?.plus("id" to it.id) }
                isLoading = false
            }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Groups") },
        text = {
            Box(modifier = Modifier.heightIn(max = 400.dp).fillMaxWidth()) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn {
                        items(groups) { group ->
                            val id = group["id"] as String
                            val name = group["name"] as? String ?: "Unnamed Group"
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(name, modifier = Modifier.weight(1f))
                                IconButton(onClick = {
                                    firestoreSource.deleteGroup(id) { success ->
                                        if (success) {
                                            groups = groups.filterNot { it["id"] == id }
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
