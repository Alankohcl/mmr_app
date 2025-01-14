package com.example.myapplication2

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReportDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_details)

        // Get the report details from the intent
//        val report = intent.extras?.getParcelable<MedicalReport>("report")
        val report:MedicalReport = intent.getSerializableExtra("report") as MedicalReport

        if (report != null) {
            // Display the report details
            fetchUserDetails(report)
        } else {
            Toast.makeText(this, "Failed to load report details", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun fetchUserDetails(report: MedicalReport) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://172.53.231.75/Final%20Year%20Project/") // tuah
            //.baseUrl("http://172.55.69.142/Final%20Year%20Project/") // lestari wifi
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .build()
        val userService = retrofit.create(UserService::class.java)

        // Fetch patient name
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val patientResponse = userService.getUserDetails(report.patient_id)
                val labAssistantResponse = userService.getUserDetails(report.lab_assistant_id)
                val doctorResponse = userService.getUserDetails(report.doctor_id)

                withContext(Dispatchers.Main) {
                    // Update the report object with the fetched names
                    report.patientName = patientResponse.user?.name ?: "Unknown"
                    report.labAssistantName = labAssistantResponse.user?.name ?: "Unknown"
                    report.doctorName = doctorResponse.user?.name ?: "Unknown"

                    // Display the updated report details
                    displayReportDetails(report)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ReportDetailsActivity, "Failed to fetch user details: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun displayReportDetails(report: MedicalReport) {
        // Display the report details in the UI
        findViewById<TextView>(R.id.tvReportId).text = "Report ID: ${report.report_id}"
        findViewById<TextView>(R.id.tvPatientName).text = "Patient Name: ${report.patientName}"
        findViewById<TextView>(R.id.tvLabAssistantName).text = "Lab Assistant Name: ${report.labAssistantName}"
        findViewById<TextView>(R.id.tvDoctorName).text = "Doctor Name: ${report.  doctorName}"
        findViewById<TextView>(R.id.tvRemarks).text = "Remarks: ${report.remarks}"
        findViewById<TextView>(R.id.tvCreatedAt).text = "Created At: ${report.report_created_at}"

        // Display blood test details
        report.bloodTest?.let { bloodTest ->
            findViewById<TextView>(R.id.tvHaemoglobinLevel).text = "Haemoglobin Level: ${bloodTest.haemoglobin_level}"
            findViewById<TextView>(R.id.tvPlateletCount).text = "Platelet Count: ${bloodTest.platelet_count}"
            findViewById<TextView>(R.id.tvNeutrophilsPercent).text = "Neutrophils Percent: ${bloodTest.neutrophils_percent}"
            findViewById<TextView>(R.id.tvLymphocytesPercent).text = "Lymphocytes Percent: ${bloodTest.lymphocytes_percent}"
            findViewById<TextView>(R.id.tvMonocytesPercent).text = "Monocytes Percent: ${bloodTest.monocytes_percent}"
            findViewById<TextView>(R.id.tvEosinophilsPercent).text = "Eosinophils Percent: ${bloodTest.eosinophils_percent}"
            findViewById<TextView>(R.id.tvBasophilsPercent).text = "Basophils Percent: ${bloodTest.basophils_percent}"
        }

        // Display health metric details
        report.healthMetric?.let { healthMetric ->
            findViewById<TextView>(R.id.tvBloodPressure).text = "Blood Pressure: ${healthMetric.blood_pressure}"
            findViewById<TextView>(R.id.tvBodyMassIndex).text = "Body Mass Index: ${healthMetric.body_mass_index}"
            findViewById<TextView>(R.id.tvHemoglobinA1c).text = "Hemoglobin A1c: ${healthMetric.hemoglobin_a1c}"
            findViewById<TextView>(R.id.tvPulseRate).text = "Pulse Rate: ${healthMetric.pulse_rate}"
            findViewById<TextView>(R.id.tvRandomBloodSugar).text = "Random Blood Sugar: ${healthMetric.random_blood_sugar}"
        }
    }
}
data class UserResponse(
    val user_id: Int,
    val name: String,
    val email: String,
    val role: String
)
