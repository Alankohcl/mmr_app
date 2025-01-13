package com.example.myapplication2



import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.appcompat.widget.Toolbar

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import java.util.*

class PatientDetailsFragment : Fragment() {

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPassword: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvGender: TextView
    private lateinit var tvDateOfBirth: TextView
    private lateinit var tvAddress: TextView

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etPhone: EditText
    private lateinit var etDateOfBirth: EditText
    private lateinit var etAddress: EditText

    private lateinit var btnEdit: Button
    private lateinit var btnUpdate: Button

    private lateinit var displayContainer: LinearLayout
    private lateinit var editContainer: LinearLayout

    private val userId: Int by lazy {
        arguments?.getInt("userId") ?: throw IllegalStateException("uerId must be provided")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_patient_details, container, false)

//        // Setup Toolbar as the ActionBar
//        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
//        val activity = activity as? AppCompatActivity
//        activity?.setSupportActionBar(toolbar)
//
//        // Enable back button in the toolbar
//        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
//
//        // Handle the back button press
//        toolbar.setNavigationOnClickListener {
//            handleBackPressed()
//        }
//
//        // Handle the back press using OnBackPressedDispatcher
//        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                handleBackPressed()
//            }
//        })

        // Initialize views
        tvName = view.findViewById(R.id.tvName)
        tvEmail = view.findViewById(R.id.tvEmail)
        tvPassword = view.findViewById(R.id.tvPassword)
        tvPhone = view.findViewById(R.id.tvPhone)
        tvGender = view.findViewById(R.id.tvGender)
        tvDateOfBirth = view.findViewById(R.id.tvDateOfBirth)
        tvAddress = view.findViewById(R.id.tvAddress)

        etName = view.findViewById(R.id.etName)
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        etPhone = view.findViewById(R.id.etPhone)
        etDateOfBirth = view.findViewById(R.id.etDateOfBirth)
        etAddress = view.findViewById(R.id.etAddress)

        btnEdit = view.findViewById(R.id.btnEdit)
        btnUpdate = view.findViewById(R.id.btnUpdate)

        displayContainer = view.findViewById(R.id.displayContainer)
        editContainer = view.findViewById(R.id.editContainer)

        // Fetch and display user details
        fetchUserDetails()

        // Set click listeners
        btnEdit.setOnClickListener {
            toggleEditMode(true)
        }

        btnUpdate.setOnClickListener {
            showUpdateConfirmationDialog()
        }

        // Set date picker for Date of Birth field
        etDateOfBirth.setOnClickListener {
            showDatePicker()
        }

        return view

    }

    private fun fetchUserDetails() {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://172.53.231.75/Final%20Year%20Project/") // Replace with your actual base URL
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .build()
        val userService = retrofit.create(UserService::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = userService.getUserDetails(userId)
                withContext(Dispatchers.Main) {
                    Log.d("API Response", response.toString())
                    if (response.success && response.user != null) {
                        val user = response.user
                        tvName.text = user.name
                        tvEmail.text = user.email
                        tvPassword.text = user.password
                        tvPhone.text = user.phone_number
                        tvGender.text = user.gender
                        tvDateOfBirth.text = user.date_of_birth
                        tvAddress.text = user.address

                        etName.setText(user.name)
                        etEmail.setText(user.email)
                        etPassword.setText(user.password)
                        etPhone.setText(user.phone_number)
                        etDateOfBirth.setText(user.date_of_birth)
                        etAddress.setText(user.address)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to load user details",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Error fetching user details: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("API Error", "Error fetching user details", e)
                }
            }
        }
    }

    private fun toggleEditMode(isEditMode: Boolean) {
        val visibility = if (isEditMode) View.VISIBLE else View.GONE
        etName.visibility = visibility
        etEmail.visibility = visibility
        etPassword.visibility = visibility
        etPhone.visibility = visibility
        etDateOfBirth.visibility = visibility
        etAddress.visibility = visibility
        btnUpdate.visibility = visibility

        val displayVisibility = if (isEditMode) View.GONE else View.VISIBLE
        tvName.visibility = displayVisibility
        tvEmail.visibility = displayVisibility
        tvPassword.visibility = displayVisibility
        tvPhone.visibility = displayVisibility
        tvGender.visibility = displayVisibility
        tvDateOfBirth.visibility = displayVisibility
        tvAddress.visibility = displayVisibility

        btnEdit.visibility = displayVisibility
    }

    private fun showUpdateConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Update")
            .setMessage("Are you sure you want to update your profile?")
            .setPositiveButton("Yes") { _, _ ->
                updateTheUserDetails()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun updateTheUserDetails() {
        val name = etName.text.toString()
        val email = etEmail.text.toString()
        val password = etPassword.text.toString()
        val phone = etPhone.text.toString()
        val gender = tvGender.text.toString()
        val dob = etDateOfBirth.text.toString()
        val address = etAddress.text.toString()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://172.53.231.75/Final%20Year%20Project/") // Replace with your actual base URL
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .build()
        val userService = retrofit.create(UserService::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = userService.updateUserDetails(
                    userId, name, email, password, phone, gender, dob, address
                )
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val updateResponse = response.body()
                        if (updateResponse?.success == true) {
                            Toast.makeText(
                                requireContext(),
                                "User details updated successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            fetchUserDetails()
                            toggleEditMode(false)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Failed to update user details",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Error updating user details: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDate = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear)
            etDateOfBirth.setText(formattedDate)
        }, year, month, day).show()
    }
//
//
//    private fun handleBackPressed() {
//        // Custom back press logic here
//        if (shouldExitApp()) {
//            requireActivity().finish() // Exit the app
//        } else {
//            // Handle fragment-specific back logic if necessary
//            requireActivity().supportFragmentManager.popBackStack()
//        }
//    }
//
//    private fun shouldExitApp(): Boolean {
//        // Add your logic to determine if the app should close (e.g., if on the home fragment)
//        return requireActivity().supportFragmentManager.backStackEntryCount == 0
//    }
//
//    // Inflate the menu resource (Logout button)
//    override fun onCreateOptionsMenu(menu: Menu,inflater: MenuInflater) {
//        inflater.inflate(R.menu.toolbar_menu, menu)
//        super.onCreateOptionsMenu(menu, inflater)
//    }
//
//    // Handle menu item click (Logout action)
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.action_logout -> {
//                // Handle logout action
//                Toast.makeText(requireContext(), "Logging out...", Toast.LENGTH_SHORT).show()
//                logout()
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }
//    // Implement logout functionality
//    private fun logout() {
//        // Implement your logout functionality here, like clearing shared preferences or navigating to the login screen
//        val intent = Intent(requireContext(), LoginActivity::class.java)
//        startActivity(intent)
//        requireActivity().finish() // Close the current activity
//    }
}

data class UserDetailsResponse(
    val success: Boolean,
    val message: String,
    val user: UserDetails?
)

data class UserDetails(
    val user_id: Int,
    val name: String,
    val email: String,
    val password: String,
    val phone_number: String,
    val gender: String,
    val date_of_birth: String,
    val address: String
)

data class UpdateUserResponse(
    val success: Boolean,
    val message: String,
    val user: User?
)