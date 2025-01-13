package com.example.myapplication2

data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val userId: Int?
)