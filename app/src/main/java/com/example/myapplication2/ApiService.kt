package com.example.myapplication2

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @FormUrlEncoded
    @POST("registerApp.php")
    fun registerUser(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("phone") phone: String,
        @Field("gender") gender: String,
        @Field("dob") dob: String,
        @Field("role") role: String,
        @Field("address") address: String,
        @Field("specialization") specialization: String?
    ): Call<RegisterResponse>

    @FormUrlEncoded
    @POST("loginApp.php")
    fun loginUser(
        @Field("email") email:String,
        @Field("password") password: String
    ): Call<LoginResponse>
}

interface MedicalReportService {
    @GET("getMedicalReportsApp.php")
    suspend fun getMedicalReports(
        @Query("patient_id") patientId: Int
    ): List<MedicalReport>

    @GET("getReportDetailsApp.php")
    suspend fun getReportDetails(
        @Query("report_id") reportId: Int
    ): ReportDetailsResponse
}

interface UserService {
    @GET("getUserDetailsApp.php")
    suspend fun getUserDetails(
        @Query("user_id") userId: Int
    ): UserDetailsResponse

    @FormUrlEncoded
    @POST("updateUserDetailsApp.php")
    suspend fun updateUserDetails(
        @Field("user_id") userId: Int,
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("phone_number") phoneNumber: String,
        @Field("gender") gender: String,
        @Field("date_of_birth") dateOfBirth: String,
        @Field("address") address: String
    ): Response<UpdateUserResponse>
}

interface HealthMetricsService {
    @GET("getHealthMetricsApp.php")
    suspend fun getHealthMetrics(
        @Query("patient_id") patientId: Int
    ): List<HealthMetric>
}

interface ReportService {
    @GET("download_report.php")
    suspend fun downloadReport(
        @Query("id") reportId: Int
    ): Response<ResponseBody>
}
