package com.example.myapplication2

data class LoginResponse (
    val success:Boolean,
    val message: String,
    val user: User?
)

data class User(
    val user_id: Int,
    val name: String,
    val email: String,
    val role: String
)