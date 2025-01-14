package com.example.myapplication2

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity

class LoadingScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading_screen)

        // Retrieve the data passed from LoginActivity
        val userId = intent.getIntExtra("userId", -1)
        val patientId = intent.getIntExtra("patientId", -1)
        val username = intent.getStringExtra("username") ?: "User"

        // Delay for 3 seconds (3000 milliseconds) before navigating to the homepage
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, HomePageActivity::class.java)
            intent.putExtra("userId", userId)
            intent.putExtra("patientId", patientId)
            intent.putExtra("username", username) // Pass username
            startActivity(intent)
            finish() // Close the loading screen activity
        }, 3000)
    }
}
