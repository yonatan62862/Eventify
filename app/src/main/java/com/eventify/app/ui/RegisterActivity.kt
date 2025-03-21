package com.eventify.app.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.eventify.app.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (validateInput(email, password, confirmPassword)) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            user?.let {
                                saveUserToFirestore(it.uid, it.email ?: "")
                            }
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            binding.etEmail.error = "Registration failed: ${task.exception?.message}"
                        }
                    }

            }
        }


        binding.loginNow.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun saveUserToFirestore(userId: String, email: String) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(userId)

        val user = hashMapOf(
            "uid" to userId,
            "email" to email,
            "name" to "New User",
            "profileImageUrl" to "",
            "createdAt" to System.currentTimeMillis()
        )

        userRef.set(user)
            .addOnSuccessListener {
                Log.d("Firestore", "User saved successfully: $email")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to save user: ${e.message}")
            }
    }


    private fun validateInput(email: String, password: String, confirmPassword: String): Boolean {
        if (email.isEmpty()) {
            binding.etEmail.error = "Email is required"
            binding.etEmail.requestFocus()
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Please enter a valid email"
            binding.etEmail.requestFocus()
            return false
        }
        if (password.isEmpty()) {
            binding.etPassword.error = "Password is required"
            binding.etPassword.requestFocus()
            return false
        }
        if (password.length < 6) {
            binding.etPassword.error = "Password must be at least 6 characters"
            binding.etPassword.requestFocus()
            return false
        }
        if (password != confirmPassword) {
            binding.etConfirmPassword.error = "Passwords do not match"
            binding.etConfirmPassword.requestFocus()
            return false
        }
        return true
    }

    private fun registerUser(email: String, password: String) {
        showProgressBar(true)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showProgressBar(false)
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                    updateUI(user)
                } else {
                    Toast.makeText(
                        this,
                        "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    updateUI(null)
                }
            }
    }

    private fun showProgressBar(isVisible: Boolean) {
        binding.progressBar.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun updateUI(user: Any?) {
        if (user != null) {
            Toast.makeText(this, "Welcome ${auth.currentUser?.email}", Toast.LENGTH_SHORT).show()
            // Navigate to the next screen, if necessary
        }
    }
}
