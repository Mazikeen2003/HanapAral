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
import coil.compose.AsyncImage
import com.example.hanaparal.data.remote.FcmSource
import com.example.hanaparal.data.remote.FirestoreSource
import com.example.hanaparal.data.remote.RemoteConfigSource
import com.example.hanaparal.ui.admin.AdminPanelActivity
import com.example.hanaparal.ui.auth.LoginActivity
import com.example.hanaparal.ui.groups.CreateGroupActivity
import com.example.hanaparal.ui.groups.GroupListActivity
import com.example.hanaparal.ui.profile.ProfileActivity
import com.example.hanaparal.ui.theme.HanapAralTheme
import com.example.hanaparal.ui.theme.ThemeManager
import com.example.hanaparal.utils.NotificationHelper
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
                var announcement by remember { mutableStateOf("Welcome to HanapAral Hub") }
                var groupCreationEnabled by remember { mutableStateOf(true) }
                val user = FirebaseAuth.getInstance().currentUser

                // Listen for global broadcasts to show as notifications (simulated push)
                LaunchedEffect(Unit) {
                    firestoreSource.observeLatestBroadcast().collect { broadcast ->
                        val title = broadcast["title"] as? String ?: "Announcement"
                        val body = broadcast["body"] as? String ?: ""
                        if (body.isNotEmpty()) {
                            notificationHelper.showNotification(NotificationHelper.CHANNEL_ADMIN_NOTICES, title, body)
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    remoteConfigSource.fetchAndActivate {
                        announcement = remoteConfigSource.getAnnouncementHeader()
                        groupCreationEnabled = remoteConfigSource.isGroupCreationEnabled()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        LargeTopAppBar(
                            title = {
                                Column {
                                    Text("HanapAral", fontWeight = FontWeight.ExtraBold)
                                    Text(
                                        "Hello, ${user?.displayName?.split(" ")?.firstOrNull() ?: "Student"}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            actions = {
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
                                        model = user?.photoUrl,
                                        contentDescription = "Profile",
                                        modifier = Modifier.size(32.dp).clip(CircleShape),
                                        contentScale = ContentScale.Crop,
                                        error = painterResource(id = android.R.drawable.ic_menu_report_image)
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.largeTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                titleContentColor = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }
                ) { innerPadding ->
                    DashboardScreen(
                        announcement = announcement,
                        isGroupEnabled = groupCreationEnabled,
                        onOpenAdmin = {
                            startActivity(Intent(this@MainActivity, AdminPanelActivity::class.java))
                        },
                        onBrowseGroups = {
                            startActivity(Intent(this@MainActivity, GroupListActivity::class.java))
                        },
                        onCreateGroup = {
                            startActivity(Intent(this@MainActivity, CreateGroupActivity::class.java))
                        },
                        onLogout = {
                            FirebaseAuth.getInstance().signOut()
                            val intent = Intent(this@MainActivity, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
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
            Text(
                "Quick Actions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
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

        item {
            Text(
                "Management",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
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
            containerColor = if (enabled) containerColor else Color.LightGray.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}
