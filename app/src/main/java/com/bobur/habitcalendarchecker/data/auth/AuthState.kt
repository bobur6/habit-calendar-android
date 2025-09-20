package com.bobur.habitcalendarchecker.data.auth

sealed class AuthState {
    data class Authenticated(
        val token: String,
        val userId: String,
        val username: String,
        val email: String
    ) : AuthState()
    
    object Unauthenticated : AuthState()
}
