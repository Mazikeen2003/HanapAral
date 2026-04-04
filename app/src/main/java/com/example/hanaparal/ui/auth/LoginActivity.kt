package com.example.hanaparal.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hanaparal.R
import com.example.hanaparal.ui.main.MainActivity
import com.example.hanaparal.ui.theme.HanapAralTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            HanapAralTheme {
                LoginScreen(
                    onSignInClick = { signIn() }
                )
            }
        }
    }

    @Composable
    fun LoginScreen(onSignInClick: () -> Unit) {
        var isLoading by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E2E5D), Color(0xFF12141C))
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.weight(1f))

                // Graduation Cap Logo
                Surface(
                    modifier = Modifier.size(140.dp),
                    shape = CircleShape,
                    color = Color(0xFFB6C4FF)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            modifier = Modifier.size(70.dp),
                            tint = Color(0xFF1E2E5D)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "HanapAral",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "Study Together, Succeed Together",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(64.dp))

                if (isLoading) {
                    CircularProgressIndicator(color = Color(0xFFB6C4FF))
                } else {
                    Button(
                        onClick = { 
                            isLoading = true
                            onSignInClick() 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB6C4FF),
                            contentColor = Color(0xFF12141C)
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Using a compass icon as a placeholder for Google/Gmail symbol
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_compass),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Continue with Gmail",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "By signing in, you agree to our\nTerms of Service and Privacy Policy",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 32.dp),
                    lineHeight = 18.sp
                )
            }
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.e("LoginActivity", "Google sign in failed", e)
                Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
                // Reset loading state
                setContent {
                    HanapAralTheme {
                        LoginScreen(onSignInClick = { signIn() })
                    }
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                    setContent {
                        HanapAralTheme {
                            LoginScreen(onSignInClick = { signIn() })
                        }
                    }
                }
            }
    }
}
