package com.example.myapplication2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder

class TrendlineFragment : Fragment() {

    private lateinit var spinnerMetrics: Spinner
    private lateinit var lineChart: LineChart

    private val patientId: Int by lazy {
        arguments?.getInt("patientId") ?: throw IllegalStateException("patientId must be provided")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_trendline, container, false)

        // Initialize views
        spinnerMetrics = view.findViewById(R.id.spinnerMetrics)
        lineChart = view.findViewById(R.id.lineChart)

        // Set up the dropdown menu
        setupSpinner()

        return view
    }

    private fun setupSpinner() {
        val metrics = listOf("Blood Pressure", "BMI", "Hemoglobin A1c", "Pulse Rate", "Random Blood Sugar")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, metrics)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMetrics.adapter = adapter

        spinnerMetrics.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedMetric = metrics[position]
                fetchTrendlineData(selectedMetric)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun fetchTrendlineData(selectedMetric: String) {
        // Create Retrofit instance
        val retrofit = Retrofit.Builder()
            .baseUrl("http://172.53.231.75/Final%20Year%20Project/") // tuah
            //.baseUrl("http://172.55.69.142/Final%20Year%20Project/") // lestari wifi
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .build()

        val healthMetricsService = retrofit.create(HealthMetricsService::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = healthMetricsService.getHealthMetrics(patientId)
                withContext(Dispatchers.Main) {
                    if (response.isNotEmpty()) {
                        val dataPoints = when (selectedMetric) {
                            "Blood Pressure" -> response.mapIndexed { index, metric ->
                                Entry(index.toFloat(), metric.blood_pressure?.toFloat()?:0.0f)
                            }
                            "BMI" -> response.mapIndexed { index, metric ->
                                Entry(index.toFloat(), metric.body_mass_index?.toFloat()?:0.0f)
                            }
                            "Hemoglobin A1c" -> response.mapIndexed { index, metric ->
                                Entry(index.toFloat(), metric.hemoglobin_a1c?.toFloat()?:0.0f)
                            }
                            "Pulse Rate" -> response.mapIndexed { index, metric ->
                                Entry(index.toFloat(), metric.pulse_rate?.toFloat()?:0.0f)
                            }
                            "Random Blood Sugar" -> response.mapIndexed { index, metric ->
                                Entry(index.toFloat(), metric.random_blood_sugar?.toFloat()?:0.0f)
                            }
                            else -> emptyList()
                        }

                        updateChart(dataPoints, selectedMetric)
                    } else {
                        // Handle empty response
                        updateChart(emptyList(), selectedMetric)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Handle error
                    updateChart(emptyList(), selectedMetric)
                }
            }
        }
    }

    private fun updateChart(dataPoints: List<Entry>, metric: String) {
        val dataSet = LineDataSet(dataPoints, "$metric Trendline")
        dataSet.lineWidth = 2f
        dataSet.setDrawValues(false)
        dataSet.setDrawCircles(true)

        val lineData = LineData(dataSet)
        lineChart.data = lineData
        lineChart.description.text = "$metric Over Time"
        lineChart.invalidate() // Refresh the chart
    }
}


