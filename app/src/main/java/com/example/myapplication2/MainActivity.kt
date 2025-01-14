package com.example.myapplication2

import android.app.AlertDialog
import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.widget.Toast
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar


class MainActivity : AppCompatActivity(){

//    private val patientId: Int by lazy {
//        intent.getIntExtra("patientId", -1).also {
//            if (it == -1) throw IllegalStateException("patientId must be provided")
//        }
//    }
//    private val userId: Int by lazy {
//        intent.getIntExtra("userId", -1).also {
//            if (it == -1) throw IllegalStateException("userId must be provided")
//        }
//    }
private val sharedPreferences by lazy { getSharedPreferences("AppPreferences", MODE_PRIVATE) }

    private val patientId: Int by lazy {
        sharedPreferences.getInt("patientId", -1).also {
            if (it == -1) {
                // Handle error gracefully if patientId is missing
                Toast.makeText(this, "Patient ID is missing!", Toast.LENGTH_SHORT).show()
                finish() // Close MainActivity or navigate back to HomePageActivity
                throw IllegalStateException("patientId must be provided")
            }
        }
    }

    private val userId: Int by lazy {
        sharedPreferences.getInt("userId", -1).also {
            if (it == -1) {
                // Handle error gracefully if userId is missing
                Toast.makeText(this, "User ID is missing!", Toast.LENGTH_SHORT).show()
                finish() // Close MainActivity or navigate back to HomePageActivity
                throw IllegalStateException("userId must be provided")
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        loadFragment(MedicalReportFragment(), patientId)

//        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
//        val userId = sharedPreferences.getInt("userId", -1)
//        val patientId = sharedPreferences.getInt("patientId", -1)

        // Load the default fragment
        if (savedInstanceState == null) {
            if (patientId != -1) {
                val fragment = MedicalReportFragment().apply {
                    arguments = Bundle().apply {
                        putInt("patientId", patientId) // Pass the patientId
                    }
                }
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit()
            } else {
                Toast.makeText(this, "Patient ID not found", Toast.LENGTH_SHORT).show()
            }
        }
        // Handle bottom navigation item clicks
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId){
                R.id.nav_medical_reports -> {
                    if(patientId != -1){
                        val fragment = MedicalReportFragment().apply {
                            arguments = Bundle().apply {
                                putInt("patientId", patientId) // Pass the patientId
                            }
                        }
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .commit()
                    }else{
                        Toast.makeText(this, "Patient Id not found", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                R.id.nav_trendline -> {
                    if (patientId != -1) {
                        val fragment = TrendlineFragment().apply {
                            arguments = Bundle().apply {
                                putInt("patientId", patientId) // Pass the patientId
                            }
                        }
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .commit()
                    } else {
                        Toast.makeText(this, "Patient Id not found", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                R.id.nav_profile -> {
                    if (userId != -1) {
                        val fragment = PatientDetailsFragment().apply {
                            arguments = Bundle().apply {
                                putInt("userId", userId) // Pass the patientId
                            }
                        }
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .commit()
                    } else {
                        Toast.makeText(this, "user Id not found", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment, patientId: Int) {
        val bundle = Bundle().apply {
            putInt("patientId", patientId)
        }
        fragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    // Inflate the menu resource (Toolbar menu with Home button)
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)  // Inflate the menu
        return true
    }

    // Handle menu item click (Home button)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.btnHome -> {
                // Handle the Home button click event
                val intent = Intent(this, HomePageActivity::class.java) // Navigate to HomePageActivity
                intent.putExtra("userId", userId)
                intent.putExtra("patientId", patientId)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}