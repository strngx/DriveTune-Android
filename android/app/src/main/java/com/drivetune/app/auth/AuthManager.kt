package com.drivetune.app.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthManager(
    private val context: Context,
    private val sessionStorage: SecureSessionStorage = SecureSessionStorage(context)
) {

    val driveReadOnlyScope = Scope(DriveScopes.DRIVE_READONLY)
    val driveFileScope = Scope(DriveScopes.DRIVE_FILE)

    private val googleSignInOptions: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestProfile()
        .requestScopes(driveReadOnlyScope, driveFileScope)
        .build()

    val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkExistingSession()
    }

    private fun checkExistingSession() {
        try {
            val lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(context)
            val savedUser = sessionStorage.getSavedUser()

            if (lastSignedInAccount != null && GoogleSignIn.hasPermissions(lastSignedInAccount, driveReadOnlyScope)) {
                val user = AuthenticatedUser(
                    id = lastSignedInAccount.id ?: savedUser?.id ?: "",
                    email = lastSignedInAccount.email ?: savedUser?.email ?: "",
                    displayName = lastSignedInAccount.displayName ?: savedUser?.displayName ?: "Google User",
                    avatarUrl = lastSignedInAccount.photoUrl?.toString() ?: savedUser?.avatarUrl,
                    accountName = lastSignedInAccount.account?.name ?: savedUser?.accountName ?: ""
                )
                sessionStorage.saveUserSession(user)
                _authState.value = AuthState.Authenticated(user)
            } else if (savedUser != null) {
                _authState.value = AuthState.Authenticated(savedUser)
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        } catch (t: Throwable) {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun hasUploadPermission(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return false
        return GoogleSignIn.hasPermissions(account, driveFileScope)
    }

    fun getSignInIntent(): Intent {
        _authState.value = AuthState.Loading
        return googleSignInClient.signInIntent
    }

    fun handleSignInResult(data: Intent?): Result<AuthenticatedUser> {
        if (data == null) {
            _authState.value = AuthState.Unauthenticated
            return Result.failure(Exception("Sign in was cancelled"))
        }

        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account: GoogleSignInAccount = task.getResult(ApiException::class.java)

            val hasDrivePermission = GoogleSignIn.hasPermissions(account, driveReadOnlyScope)
            if (!hasDrivePermission) {
                val errorMsg = "Drive access permission is required to browse your music files."
                _authState.value = AuthState.Error(errorMsg)
                return Result.failure(Exception(errorMsg))
            }

            val user = AuthenticatedUser(
                id = account.id ?: "",
                email = account.email ?: "",
                displayName = account.displayName ?: account.email?.substringBefore("@") ?: "Google User",
                avatarUrl = account.photoUrl?.toString(),
                accountName = account.account?.name ?: account.email ?: ""
            )

            sessionStorage.saveUserSession(user)
            _authState.value = AuthState.Authenticated(user)
            Result.success(user)
        } catch (e: ApiException) {
            val message = when (e.statusCode) {
                CommonStatusCodes.SIGN_IN_REQUIRED -> "Please sign in with your Google account."
                CommonStatusCodes.NETWORK_ERROR -> "Network error connecting to Google. Check your internet connection."
                CommonStatusCodes.INVALID_ACCOUNT -> "Invalid Google account selected."
                CommonStatusCodes.CANCELED -> "Sign in cancelled."
                else -> "Authentication failed (${e.statusCode}): ${e.localizedMessage ?: "Unknown error"}"
            }
            if (e.statusCode == CommonStatusCodes.CANCELED) {
                _authState.value = AuthState.Unauthenticated
            } else {
                _authState.value = AuthState.Error(message)
            }
            Result.failure(e)
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: "Unexpected error during Google sign in"
            _authState.value = AuthState.Error(msg)
            Result.failure(e)
        }
    }

    fun getGoogleCredential(): GoogleAccountCredential? {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return null
        return GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_READONLY, DriveScopes.DRIVE_FILE)
        ).apply {
            selectedAccount = account.account
        }
    }

    fun signOut(onComplete: () -> Unit = {}) {
        sessionStorage.clearSession()
        googleSignInClient.signOut().addOnCompleteListener {
            _authState.value = AuthState.Unauthenticated
            onComplete()
        }
    }

    fun getAuthenticatedUser(): AuthenticatedUser? {
        return (authState.value as? AuthState.Authenticated)?.user ?: sessionStorage.getSavedUser()
    }
}
