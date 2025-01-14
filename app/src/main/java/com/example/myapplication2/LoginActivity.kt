package com.example.myapplication2

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginActivity : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var togglePassword: ImageButton
    private lateinit var toggleConfirmPassword: ImageButton
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize views
        email = findViewById(R.id.editTextEmail)
        password = findViewById(R.id.editTextPassword)
        confirmPassword = findViewById(R.id.editTextConfirmPassword)
        togglePassword = findViewById(R.id.buttonTogglePassword)
        toggleConfirmPassword = findViewById(R.id.buttonToggleConfirmPassword)
        loginButton = findViewById(R.id.buttonLogin)
        registerButton = findViewById(R.id.buttonRegister)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://172.53.231.75/Final%20Year%20Project/")//tuah
            //.baseUrl("http://172.55.69.142/Final%20Year%20Project/") // lestari wifi
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)

        // Toggle password visibility
        togglePassword.setOnClickListener {
            togglePasswordVisibility(password, togglePassword)
        }

        toggleConfirmPassword.setOnClickListener {
            togglePasswordVisibility(confirmPassword, toggleConfirmPassword)
        }

        // Login button click listener
        loginButton.setOnClickListener {
            if (validateInputs()) {
                val emailInput = email.text.toString().trim()
                val passwordInput = password.text.toString().trim()

                apiService.loginUser(emailInput, passwordInput)
                    .enqueue(object : Callback<LoginResponse>{
                    override fun onResponse(call: Call<LoginResponse>, response:Response<LoginResponse>){
                        if (response.isSuccessful && response.body() != null){
                            val loginResponse = response.body()
                            if (loginResponse?.success == true){
                                val user = loginResponse.user
                                if(user != null && user.role =="patient"){
                                    val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
                                    sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
                                    sharedPreferences.edit().putInt("patientId", user.user_id).apply()
                                    sharedPreferences.edit().putInt("userId", user.user_id).apply()
                                    sharedPreferences.edit().putString("username", user.name).apply()

                                    Toast.makeText(this@LoginActivity, "Login Successful!", Toast.LENGTH_SHORT).show()

                                    val intent = Intent(this@LoginActivity, LoadingScreenActivity::class.java)
                                    intent.putExtra("patientId", user.user_id)
                                    intent.putExtra("userId", user.user_id)
                                    intent.putExtra("username", user.name)
                                    startActivity(intent)
                                    finish()
                                }
                            }else{
                                Toast.makeText(this@LoginActivity, loginResponse?.message ?: "Login Failed", Toast.LENGTH_SHORT).show()
                            }
                        }else{
                            Toast.makeText(this@LoginActivity, "API error: ${response.errorBody()?.string() ?: "Unknown Error"}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        Toast.makeText(
                            this@LoginActivity,
                            "Failed to connect to server: ${t.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            }
        }

        // Register button click listener
        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Password strength validation
        password.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validatePasswordStrength(s.toString())
            }
        })
    }

    private fun togglePasswordVisibility(editText: EditText, button: ImageButton) {
        if (editText.inputType == 129) { // 129 = textPassword
            editText.inputType = 1 // 1 = text
            button.setImageResource(R.drawable.ic_visibility)
        } else {
            editText.inputType = 129
            button.setImageResource(R.drawable.ic_visibility_off)
        }
        editText.setSelection(editText.text.length) // Move cursor to the end
    }

    private fun validateInputs(): Boolean {
        val emailInput = email.text.toString().trim()
        val passwordInput = password.text.toString().trim()
        val confirmPasswordInput = confirmPassword.text.toString().trim()

        if (emailInput.isEmpty()) {
            email.error = "Email is required"
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            email.error = "Invalid email format"
            return false
        }

        if (passwordInput.isEmpty()) {
            password.error = "Password is required"
            return false
        } else if (!validatePasswordStrength(passwordInput)) {
            return false
        }

        if (confirmPasswordInput.isEmpty()) {
            confirmPassword.error = "Please re-confirm your password"
            return false
        } else if (passwordInput != confirmPasswordInput) {
            confirmPassword.error = "Passwords do not match"
            return false
        }

        return true
    }

    private fun validatePasswordStrength(password: String): Boolean {
        val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"
        if (!password.matches(passwordPattern.toRegex())) {
            this.password.error = "Password must contain at least 8 characters, 1 number, 1 uppercase, 1 lowercase, and 1 special character"
            return false
        }
        return true
    }
}