package com.drivetune.app.auth

data class AuthenticatedUser(
    val id: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String?,
    val accountName: String
)

sealed interface AuthState {
    data object Unauthenticated : AuthState
    data object Loading : AuthState
    data class Authenticated(val user: AuthenticatedUser) : AuthState
    data class Error(val message: String, val canRetry: Boolean = true) : AuthState
}
