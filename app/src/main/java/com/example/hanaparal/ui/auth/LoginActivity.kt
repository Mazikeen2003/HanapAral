package com.example.hanaparal.ui.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hanaparal.R
import com.example.hanaparal.ui.main.MainActivity
import com.example.hanaparal.ui.theme.HanapAralTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HanapAralTheme {
                LoginScreen(
                    onLoginSuccess = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                isLoading = true
                firebaseAuthWithGoogle(account, auth, db, onLoginSuccess) { error ->
                    isLoading = false
                    errorMessage = error
                    Log.e("Login", "Firebase Auth Error: $error")
                }
            } catch (e: ApiException) {
                isLoading = false
                errorMessage = "Google Sign-In failed: ${e.statusCode}"
                Log.e("Login", "Google Sign-In API Error", e)
            }
        } else {
            isLoading = false
            Log.e("Login", "Result not OK: ${result.resultCode}")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "App Logo",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "HanapAral",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Study Together, Succeed Together",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Authenticating...", style = MaterialTheme.typography.labelLarge)
            } else {
                Button(
                    onClick = {
                        errorMessage = null
                        val webClientId = context.getString(R.string.default_web_client_id)
                        if (webClientId.isNotEmpty() && webClientId.contains("apps.googleusercontent.com")) {
                            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(webClientId)
                                .requestEmail()
                                .requestProfile()
                                .build()
                            val googleSignInClient = GoogleSignIn.getClient(context, gso)
                            // Sign out first to ensure account picker shows up
                            googleSignInClient.signOut().addOnCompleteListener {
                                googleSignInLauncher.launch(googleSignInClient.signInIntent)
                            }
                        } else {
                            errorMessage = "Developer Error: Invalid Web Client ID in strings.xml"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_compass),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Continue with Gmail",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "By signing in, you agree to our\nTerms of Service and Privacy Policy",
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

private fun firebaseAuthWithGoogle(
    acct: GoogleSignInAccount,
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val idToken = acct.idToken
    if (idToken == null) {
        onFailure("Google ID Token is null")
        return
    }

    val credential = GoogleAuthProvider.getCredential(idToken, null)
    auth.signInWithCredential(credential)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                val userData = hashMapOf(
                    "uid" to user?.uid,
                    "name" to user?.displayName,
                    "email" to user?.email,
                    "photoUrl" to user?.photoUrl.toString(),
                    "lastLogin" to com.google.firebase.Timestamp.now()
                )
                
                user?.let {
                    db.collection("users").document(it.uid)
                        .set(userData, SetOptions.merge())
                        .addOnSuccessListener { 
                            onSuccess() 
                        }
                        .addOnFailureListener { e -> 
                            onFailure("Firestore Error: ${e.message}") 
                        }
                }
            } else {
                val exception = task.exception
                val message = exception?.message ?: "Authentication Failed"
                onFailure(message)
            }
        }
}
