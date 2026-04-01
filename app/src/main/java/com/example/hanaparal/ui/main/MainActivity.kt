package com.example.hanaparal.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.hanaparal.R
import com.example.hanaparal.data.model.StudyGroup
import com.example.hanaparal.data.remote.FcmSource
import com.example.hanaparal.data.remote.FirestoreSource
import com.example.hanaparal.data.remote.RemoteConfigSource
import com.example.hanaparal.ui.admin.AdminPanelActivity
import com.example.hanaparal.ui.auth.LoginActivity
import com.example.hanaparal.ui.groups.CreateGroupActivity
import com.example.hanaparal.ui.groups.GroupDetailsActivity
import com.example.hanaparal.ui.groups.GroupListActivity
import com.example.hanaparal.ui.profile.ProfileActivity
import com.example.hanaparal.ui.profile.ProfileViewModel
import com.example.hanaparal.ui.theme.HanapAralTheme
import com.example.hanaparal.ui.theme.ThemeManager
import com.example.hanaparal.utils.NotificationHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    private val remoteConfigSource = RemoteConfigSource()
    private val firestoreSource = FirestoreSource()
    private val fcmSource = FcmSource(firestoreSource)
    private lateinit var notificationHelper: NotificationHelper

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        notificationHelper = NotificationHelper(this)
        askNotificationPermission()
        fcmSource.fetchAndStoreToken()

        setContent {
            val userDarkMode by ThemeManager.isDarkMode.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val currentDark = userDarkMode ?: systemDark

            HanapAralTheme {
                val profileViewModel: ProfileViewModel = viewModel()
                val profile by profileViewModel.profile.collectAsState()
                
                var announcement by remember { mutableStateOf("Welcome to HanapAral Hub") }
                var groupCreationEnabled by remember { mutableStateOf(true) }
                var maxMembers by remember { mutableStateOf(10) }
                val user = FirebaseAuth.getInstance().currentUser

                // Navigation State
                var currentScreen by remember { mutableStateOf("dashboard") }
                var selectedGroup by remember { mutableStateOf<StudyGroup?>(null) }

                LaunchedEffect(Unit) {
                    remoteConfigSource.fetchAndActivate {
                        announcement = remoteConfigSource.getAnnouncementHeader()
                        groupCreationEnabled = remoteConfigSource.isGroupCreationEnabled()
                    }
                    
                    firestoreSource.observeGlobalSettings().collect { settings ->
                        (settings["group_creation_enabled"] as? Boolean)?.let { groupCreationEnabled = it }
                        (settings["max_members_per_group"] as? Long)?.let { maxMembers = it.toInt() }
                        (settings["announcement_header"] as? String)?.let { announcement = it }
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    when (currentScreen) {
                                        "dashboard" -> "HanapAral"
                                        "group_list" -> "Find Groups"
                                        "create_group" -> "Create Group"
                                        "group_details" -> "Group Details"
                                        else -> "HanapAral"
                                    },
                                    fontWeight = FontWeight.ExtraBold
                                )
                            },
                            navigationIcon = {
                                if (currentScreen != "dashboard") {
                                    IconButton(onClick = { 
                                        currentScreen = if (currentScreen == "group_details") "group_list" else "dashboard" 
                                    }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                                            contentDescription = "Back"
                                        )
                                    }
                                }
                            },
                            actions = {
                                if (currentScreen == "dashboard") {
                                    IconButton(onClick = { ThemeManager.setDarkMode(!currentDark) }) {
                                        Icon(
                                            if (currentDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                            contentDescription = "Toggle Dark Mode"
                                        )
                                    }
                                    IconButton(onClick = {
                                        startActivity(Intent(this@MainActivity, ProfileActivity::class.java))
                                    }) {
                                        AsyncImage(
                                            model = if (profile.photoUrl.isNotEmpty()) profile.photoUrl else user?.photoUrl,
                                            contentDescription = "Profile",
                                            modifier = Modifier.size(32.dp).clip(CircleShape),
                                            contentScale = ContentScale.Crop,
                                            error = painterResource(id = android.R.drawable.ic_menu_report_image)
                                        )
                                    }
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            "dashboard" -> DashboardScreen(
                                announcement = announcement,
                                isGroupEnabled = groupCreationEnabled,
                                isAdmin = profile.isAdmin,
                                onOpenAdmin = {
                                    startActivity(Intent(this@MainActivity, AdminPanelActivity::class.java))
                                },
                                onBrowseGroups = { currentScreen = "group_list" },
                                onCreateGroup = { currentScreen = "create_group" },
                                onLogout = { logout() }
                            )
                            "group_list" -> GroupListActivity(
                                onGroupClick = { group -> 
                                    selectedGroup = group
                                    currentScreen = "group_details"
                                }
                            )
                            "create_group" -> CreateGroupActivity(
                                onGroupCreated = { currentScreen = "group_list" }
                            )
                            "group_details" -> selectedGroup?.let { group ->
                                GroupDetailsActivity(
                                    group = group,
                                    onBack = { currentScreen = "group_list" }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        
        GoogleSignIn.getClient(this, gso).signOut().addOnCompleteListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
fun DashboardScreen(
    announcement: String,
    isGroupEnabled: Boolean,
    isAdmin: Boolean,
    onOpenAdmin: () -> Unit,
    onBrowseGroups: () -> Unit,
    onCreateGroup: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Campaign,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = announcement,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        item {
            Text("Quick Actions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ActionCard(
                    title = "Find Groups",
                    icon = Icons.Default.Search,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.weight(1f),
                    onClick = onBrowseGroups
                )
                ActionCard(
                    title = "Create",
                    icon = Icons.Default.Add,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.weight(1f),
                    enabled = isGroupEnabled,
                    onClick = onCreateGroup
                )
            }
        }

        if (isAdmin) {
            item {
                Text("Management", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            item {
                ListItem(
                    headlineContent = { Text("Admin Controls", fontWeight = FontWeight.Bold) },
                    supportingContent = { Text("Biometric protected system settings") },
                    leadingContent = {
                        Surface(
                            color = MaterialTheme.colorScheme.secondary,
                            shape = CircleShape,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White)
                            }
                        }
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .clickable { onOpenAdmin() }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text("Logout from Account", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    icon: ImageVector,
    containerColor: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) containerColor else Color.Gray.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = if (enabled) MaterialTheme.colorScheme.onSecondaryContainer else Color.DarkGray)
            Spacer(Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}
