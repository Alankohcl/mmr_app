package com.example.myapplication2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import android.app.DatePickerDialog
import android.util.Log
import android.widget.*
import java.util.*
import com.google.gson.GsonBuilder

class RegisterActivity :AppCompatActivity(){

    private lateinit var apiService: ApiService
    private lateinit var genderSpinner: Spinner
    private lateinit var dobTextView: TextView
    private lateinit var roleTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        Log.d("RegisterActivity", "OnCreate called")

        val name = findViewById<EditText>(R.id.editTextName)
        val email = findViewById<EditText>(R.id.editTextEmail)
        val password = findViewById<EditText>(R.id.editTextPassword)
        val phone = findViewById<EditText>(R.id.editTextPhone)
        roleTextView = findViewById<TextView>(R.id.textViewRole)
        val address = findViewById<EditText>(R.id.editTextAddress)
        val registerButton = findViewById<Button>(R.id.buttonRegister)

        Log.d("RegisterActivity", "Views initialized")

        roleTextView.text = "patient"
        genderSpinner = findViewById(R.id.spinnerGender)
        dobTextView = findViewById(R.id.textViewDob)

        val genderOptions = arrayOf("Select Gender", "Male", "Female")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = adapter

        dobTextView.setOnClickListener{
            showDatePickerDialog()
        }

        val gson = GsonBuilder()
            .setLenient()
            .create()

        //initialize Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("http://172.53.231.75/Final%20Year%20Project/")
//            .baseUrl("http://172.55.205.96/Final%20Year%20Project/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        apiService = retrofit.create(ApiService::class.java)

        registerButton.setOnClickListener {
            val nameInput = name.text.toString()
            val emailInput = email.text.toString()
            val passwordInput = password.text.toString()
            val phoneInput = phone.text.toString()
            val genderInput = genderSpinner.selectedItem.toString()
            val dobInput = dobTextView.text.toString()
            val roleInput = roleTextView.text.toString()
            val addressInput = address.text.toString()

            if(nameInput.isEmpty() || emailInput.isEmpty() || passwordInput.isEmpty()
                || phoneInput.isEmpty() || genderInput == "Select Gender" || dobInput.isEmpty()
                || roleInput.isEmpty() || addressInput.isEmpty()){
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
//                startActivity(Intent(this, LoginActivity::class.java))
            }
            registerUser(nameInput, emailInput, passwordInput, phoneInput, genderInput, dobInput, roleInput, addressInput)
        }
    }

    private fun showDatePickerDialog(){
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                dobTextView.text = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun registerUser(name: String, email:String, password: String, phone: String, gender: String, dob: String, role: String, address: String, specialization: String? = null){
        apiService.registerUser(name, email, password, phone, gender, dob, role, address, specialization)
            .enqueue(object : Callback<RegisterResponse>{
                override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>){
                    if(response.isSuccessful && response.body() != null ){
                        val registerResponse = response.body()
                        if(registerResponse?.success == true){
                            Toast.makeText(this@RegisterActivity, "Registration successful", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                            finish()
                        }else{
                            Toast.makeText(this@RegisterActivity, registerResponse?.message ?: "Registration Failed", Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        Log.e("API Error", "Response code: ${response.code()}, error body: ${response.errorBody()?.string() ?: "Unknown Error"}")
                        Toast.makeText(this@RegisterActivity, "API error_Server error: ${response.errorBody()?.string() ?: "Unknown Error"}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<RegisterResponse>, t: Throwable){
                    Log.e("Network Error", "Error: ${t.localizedMessage}")
                    Toast.makeText(
                        this@RegisterActivity,
                        "Failed to connect to server g: ${t.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        })
    }
}