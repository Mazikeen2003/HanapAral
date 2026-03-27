package com.example.hanaparal.ui.profile

import android.os.Bundle
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.hanaparal.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class EditProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        auth = FirebaseAuth.getInstance()



        val etName = findViewById<EditText>(R.id.etName)
        val btnSave = findViewById<Button>(R.id.btnSave)

        val user = auth.currentUser

        etName.setText(user?.displayName)

        btnSave.setOnClickListener {
            val newName = etName.text.toString()

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build()

            val db = FirebaseFirestore.getInstance()
            val user = auth.currentUser
            val userId = user?.uid

            val userMap = hashMapOf(
                "name" to newName,
                "email" to user?.email,
                "course" to "BSIT" // you can change later
            )

            if (userId != null) {
                db.collection("users")
                    .document(userId)
                    .set(userMap)
            }

            user?.updateProfile(profileUpdates)
        }
    }
}