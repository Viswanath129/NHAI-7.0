package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SessionManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_session_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun createSession(userId: String) {
        prefs.edit()
            .putString("session_user_id", userId)
            .putLong("session_expiry", System.currentTimeMillis() + 3600 * 1000) // 1 hour session
            .putInt("failed_attempts", 0)
            .apply()
    }

    fun getSessionUserId(): String? {
        val expiry = prefs.getLong("session_expiry", 0)
        if (System.currentTimeMillis() > expiry) {
            clearSession()
            return null
        }
        return prefs.getString("session_user_id", null)
    }

    fun clearSession() {
        prefs.edit()
            .remove("session_user_id")
            .remove("session_expiry")
            .apply()
    }

    fun recordFailedAttempt() {
        val attempts = prefs.getInt("failed_attempts", 0) + 1
        prefs.edit().putInt("failed_attempts", attempts).apply()
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            prefs.edit().putLong("lockout_until", System.currentTimeMillis() + LOCKOUT_DURATION_MS).apply()
        }
    }

    fun resetFailedAttempts() {
        prefs.edit()
            .putInt("failed_attempts", 0)
            .remove("lockout_until")
            .apply()
    }

    fun isLockedOut(): Boolean {
        val lockoutUntil = prefs.getLong("lockout_until", 0)
        return System.currentTimeMillis() < lockoutUntil
    }

    companion object {
        const val MAX_FAILED_ATTEMPTS = 5
        const val LOCKOUT_DURATION_MS = 15 * 60 * 1000L // 15 mins
    }
}
