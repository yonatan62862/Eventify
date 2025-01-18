package com.eventify.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class LauncherActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // אתחול Firebase Auth
        auth = FirebaseAuth.getInstance()

        // בדיקה אם המשתמש מחובר
        if (auth.currentUser != null) {
            // המשתמש מחובר - מעבר ל-MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            // המשתמש לא מחובר - מעבר ל-LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // סגירת ה-LauncherActivity
        finish()
    }
}