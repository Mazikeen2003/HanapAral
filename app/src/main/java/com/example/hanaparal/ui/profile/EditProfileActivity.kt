package com.example.hanaparal.ui.profile

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hanaparal.ui.theme.HanapAralTheme

class EditProfileActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            HanapAralTheme {
                val viewModel: ProfileViewModel = viewModel()
                val profile by viewModel.profile.collectAsState()
                val isLoading by viewModel.isLoading.collectAsState()

                // Local state for editing
                var name by remember(profile.name) { mutableStateOf(profile.name) }
                var course by remember(profile.course) { mutableStateOf(profile.course) }
                var year by remember(profile.year) { mutableStateOf(profile.year) }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Edit Profile Details", fontWeight = FontWeight.Bold) },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text(
                            "Update your information so other students can find you easily.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        EditTextField(label = "Full Name", value = name, onValueChange = { name = it })
                        EditTextField(label = "Course / Program", value = course, onValueChange = { course = it })
                        EditTextField(label = "Year Level", value = year, onValueChange = { year = it })

                        Spacer(modifier = Modifier.weight(1f))

                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        } else {
                            Button(
                                onClick = {
                                    if (name.isNotBlank()) {
                                        viewModel.updateProfile(name, course, year) { success ->
                                            if (success) {
                                                Toast.makeText(this@EditProfileActivity, "Updated successfully!", Toast.LENGTH_SHORT).show()
                                                finish()
                                            } else {
                                                Toast.makeText(this@EditProfileActivity, "Failed to update", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(this@EditProfileActivity, "Name is required", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(60.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Save and Update", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditTextField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}
