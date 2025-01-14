package com.example.myapplication2

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder
import android.widget.Toast
import android.util.Log
import okhttp3.ResponseBody
import java.io.Serializable

class MedicalReportFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MedicalReportAdapter

    // Retrieve patientId from arguments
    private val patientId: Int by lazy {
        arguments?.getInt("patientId") ?: throw IllegalStateException("patientId must be provided")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("MedicalReportFragment", "Arguments: ${arguments?.keySet()}")
        val view = inflater.inflate(R.layout.fragment_medical_report, container, false)
        recyclerView = view.findViewById(R.id.rvMedicalReports)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = MedicalReportAdapter(
            onViewClick = { report -> viewReport(report) },
            onDownloadClick = { report ->
                if(report.report_id > 0){
                    downloadPdf(report.report_id)
                }else{
                    Toast.makeText(requireContext(), " cant download, no id", Toast.LENGTH_SHORT).show()
                }
            }
        )
        recyclerView.adapter = adapter

        fetchMedicalReports(patientId)
        return view
    }

    private fun fetchMedicalReports(patientId: Int) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://172.53.231.75/Final%20Year%20Project/") //Tuah IP Address
            //.baseUrl("http://172.55.69.142/Final%20Year%20Project/") // lestari wifi
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .build()
        val medicalReportService = retrofit.create(MedicalReportService::class.java)

        // Fetch medical reports from the backend and update the adapter
        CoroutineScope(Dispatchers.IO).launch {
            try{
                val response = medicalReportService.getMedicalReports(patientId)
                Log.d("MedicalReportFragment", "API Response: $response")
                withContext(Dispatchers.Main) {
                    if(isAdded){
                        if (response.isNotEmpty()) {
                            adapter.setReports(response)
                            // Show the RecyclerView and hide the "No medical report" message
                            recyclerView.visibility = View.VISIBLE
                            view?.findViewById<TextView>(R.id.tvNoReportsMessage)?.visibility = View.GONE
                        }else{
                            // Show the "No medical report" message and hide the RecyclerView
                            recyclerView.visibility = View.GONE
                            view?.findViewById<TextView>(R.id.tvNoReportsMessage)?.visibility = View.VISIBLE
                            Toast.makeText(requireContext(), "No medical reports found", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }catch(e: Exception){
                Log.d("MedicalReportFragment", "API Error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    if(isAdded){
                        // Handle error (e.g., show a Toast)
                        Toast.makeText(
                            requireContext(),
                            "Failed to fetch medical reports: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun viewReport(report: MedicalReport){
        val retrofit = Retrofit.Builder()
            .baseUrl("http://172.53.231.75/Final%20Year%20Project/") // tuah
            //.baseUrl("http://172.55.69.142/Final%20Year%20Project/") // lestari wifi
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .build()
        val medicalReportService = retrofit.create(MedicalReportService::class.java)
        // Fetch full report details from the backend
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = medicalReportService.getReportDetails(report.report_id)
                withContext(Dispatchers.Main) {
                    // Open a new activity or fragment to display the report details
                    //openReportDetailsActivity(response)
                    if (response.success) {
                        response.report_details?.let {
                            openReportDetailsActivity(it)
                        } ?: Toast.makeText(requireContext(), "Report details not available", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Failed to fetch report details", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Failed to fetch report details: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        Toast.makeText(requireContext(), "Viewing Report: ${report.report_id}", Toast.LENGTH_SHORT).show()
    }

    private fun openReportDetailsActivity(report: MedicalReport) {
        if(report != null){
            // Pass the report details to a new activity or fragment
            val intent = Intent(requireContext(), ReportDetailsActivity::class.java).apply {
                putExtra("report", report)
            }
            startActivity(intent)
        }else{
            Toast.makeText(requireContext(), "Report details not available", Toast.LENGTH_SHORT).show()
        }

    }

    private fun downloadPdf(reportId: Int) {
        if (reportId <= 0) {
            Toast.makeText(requireContext(), "Invalid report ID", Toast.LENGTH_SHORT).show()
            return
        }
        val retrofit = Retrofit.Builder()
            .baseUrl("http://172.53.231.75/Final%20Year%20Project/") //tuah
            //.baseUrl("http://172.55.69.142/Final%20Year%20Project/") // lestari wifi
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .build()
        val service = retrofit.create(ReportService::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = service.downloadReport(reportId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        // Save the PDF file locally
                        savePdfToFile(response.body())
                        Toast.makeText(
                            requireContext(),
                            "Downloading report...",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Log.e("Download Error", "Failed to download the report")
                        Toast.makeText(
                            requireContext(),
                            "Failed to download report",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("Download Error", "Exception: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Error occurred while downloading",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun savePdfToFile(responseBody: ResponseBody?) {
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Medical_Report_${System.currentTimeMillis()}.pdf")

        try {
            val inputStream: InputStream = responseBody!!.byteStream()
            val outputStream: OutputStream = FileOutputStream(file)

            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            // Inform the user that the file is saved
            Log.d("Download Success", "File saved to: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e("Download Error", "Error saving file", e)
        }
    }
}
data class ReportDetailsResponse(
    val success: Boolean,
    val report_details: MedicalReport? // You can make this nullable if no data is returned
)

data class MedicalReport(
    val report_id: Int,
    val patient_id: Int,
    val lab_assistant_id: Int,
    val doctor_id: Int,
    val remarks: String?,
    val report_created_at: String?,
//    val pdfUrl: String?,
    val blood_test_id: Int?,
    val health_metric_id: Int?,
    val bloodTest: BloodTest?,
    val healthMetric: HealthMetric?,
    var patientName: String?,
    var labAssistantName: String?,
    var doctorName: String?
): Serializable

data class BloodTest(
    val blood_test_id: Int,
    val haemoglobin_level: Double?,
    val platelet_count: Int?,
    val neutrophils_percent: Double?,
    val lymphocytes_percent: Double?,
    val monocytes_percent: Double?,
    val eosinophils_percent: Double?,
    val basophils_percent: Double?
):Serializable

data class HealthMetric(
    val health_metric_id: Int,
    val patient_id: Int,
    val blood_pressure: String?,
    val body_mass_index: Double?,
    val hemoglobin_a1c: Double?,
    val pulse_rate: Int?,
    val random_blood_sugar: Double?,
    val created_at: String?
):Serializable


class MedicalReportAdapter(
    private val onViewClick: (MedicalReport) -> Unit,
    private val onDownloadClick: (MedicalReport) -> Unit // Lambda for download click
) : RecyclerView.Adapter<MedicalReportAdapter.ReportViewHolder>() {

    private val reports = mutableListOf<MedicalReport>()

    fun setReports(newReports: List<MedicalReport>) {
        reports.clear()
        reports.addAll(newReports)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_medical_report, parent, false)
        return ReportViewHolder(view, onViewClick, onDownloadClick)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reports[position]
        holder.bind(report)
    }

    override fun getItemCount() = reports.size

    class ReportViewHolder(
        itemView: View,
        private val onViewClick: (MedicalReport) -> Unit,
        private val onDownloadClick: (MedicalReport) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.tvReportTitle)
        private val subtitle: TextView = itemView.findViewById(R.id.tvReportSubtitle)
        private val btnView: Button = itemView.findViewById(R.id.btnViewReport)
        private val btnDownload: Button = itemView.findViewById(R.id.btnDownloadReport)

        fun bind(report: MedicalReport) {
            title.text = report.remarks ?: "Report ${report.report_id}"
            subtitle.text = report.report_created_at

            btnView.setOnClickListener{
                onViewClick(report)
            }
            // Set click listener for downloading PDF
            btnDownload.setOnClickListener {
                if (report.report_id == 0) {
                    Toast.makeText(itemView.context, "no id passed", Toast.LENGTH_SHORT).show()
                } else {
                    onDownloadClick(report)
                }
            }
        }
    }
}

