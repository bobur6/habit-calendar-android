package com.bobur.habitcalendarchecker.data.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthCredentials(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val userId: String,
    val username: String,
    val email: String
)
