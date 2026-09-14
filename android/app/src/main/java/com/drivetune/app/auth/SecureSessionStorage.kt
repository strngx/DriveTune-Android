package com.drivetune.app.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureSessionStorage(context: Context) {

    private val sharedPreferences: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "drivetune_secure_session",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (t: Throwable) {
        // Fallback to standard private preferences if keystore issue occurs
        context.getSharedPreferences("drivetune_session_fallback", Context.MODE_PRIVATE)
    }

    fun saveUserSession(user: AuthenticatedUser) {
        sharedPreferences.edit()
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_EMAIL, user.email)
            .putString(KEY_DISPLAY_NAME, user.displayName)
            .putString(KEY_AVATAR_URL, user.avatarUrl)
            .putString(KEY_ACCOUNT_NAME, user.accountName)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }

    fun getSavedUser(): AuthenticatedUser? {
        val isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
        if (!isLoggedIn) return null

        val email = sharedPreferences.getString(KEY_EMAIL, null) ?: return null
        val id = sharedPreferences.getString(KEY_USER_ID, "") ?: ""
        val displayName = sharedPreferences.getString(KEY_DISPLAY_NAME, email.substringBefore("@")) ?: email
        val avatarUrl = sharedPreferences.getString(KEY_AVATAR_URL, null)
        val accountName = sharedPreferences.getString(KEY_ACCOUNT_NAME, email) ?: email

        return AuthenticatedUser(
            id = id,
            email = email,
            displayName = displayName,
            avatarUrl = avatarUrl,
            accountName = accountName
        )
    }

    fun clearSession() {
        sharedPreferences.edit().clear().apply()
    }

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "user_email"
        private const val KEY_DISPLAY_NAME = "user_display_name"
        private const val KEY_AVATAR_URL = "user_avatar_url"
        private const val KEY_ACCOUNT_NAME = "user_account_name"
    }
}
