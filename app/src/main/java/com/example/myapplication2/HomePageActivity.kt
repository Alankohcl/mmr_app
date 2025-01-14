package com.example.myapplication2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HomePageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check login state
        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (!isLoggedIn) {
            // If not logged in, redirect to LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Load layout after confirming login state
        setContentView(R.layout.activity_home_page)

        // Retrieve data passed from LoadingScreenActivity
//        val userId = intent.getIntExtra("userId", -1)
//        val patientId = intent.getIntExtra("patientId", -1)
//        val username = sharedPreferences.getString("username", "User") ?: "User"

        val userId = sharedPreferences.getInt("userId", -1)
        val patientId = sharedPreferences.getInt("patientId", -1)
        val username = sharedPreferences.getString("username", "User") ?: "User"

        // Set greeting message
        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        tvGreeting.text = "Welcome back, $username!"

        // Navigate to MainActivity
        val btnEnterMainActivity = findViewById<Button>(R.id.btnEnterMainActivity)
        btnEnterMainActivity.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply{
//                intent.putExtra("userId", userId)
//                intent.putExtra("patientId", patientId)
            }
            startActivity(intent)
        }

        // Logout button
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            // Create an AlertDialog to confirm logout
            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            builder.setTitle("Logout")
            builder.setMessage("Are you sure you want to logout?")

            // Add "Yes" button
            builder.setPositiveButton("Yes") { _, _ ->
                // Clear user session or token if needed
                val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
                sharedPreferences.edit().clear().apply() // Clear all session data

                startActivity(Intent(this, LoginActivity::class.java))
                finish() // Close the homepage
            }
            // Add "Cancel" button
            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss() // Dismiss the dialog
            }

            // Show the AlertDialog
            builder.create().show()
        }
    }
}
