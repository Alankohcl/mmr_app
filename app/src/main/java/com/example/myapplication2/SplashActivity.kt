package com.example.myapplication2

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check login state from SharedPreferences
        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        // Retrieve user data from SharedPreferences
        val userId = sharedPreferences.getInt("userId", -1)
        val patientId = sharedPreferences.getInt("patientId", -1)
        val username = sharedPreferences.getString("username", "User") ?: "User"

        // Delay for 2 seconds to simulate a splash screen
        Handler(Looper.getMainLooper()).postDelayed({
            if (isLoggedIn) {
                // Navigate to HomePageActivity with userId and patientId
                val homeIntent = Intent(this, HomePageActivity::class.java)
                startActivity(homeIntent)
            } else {
                // Navigate to LoginActivity
                sharedPreferences.edit().clear().apply() // Clear all session data
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish() // Close the SplashActivity
        }, 2000) // 2-second delay
    }
}
