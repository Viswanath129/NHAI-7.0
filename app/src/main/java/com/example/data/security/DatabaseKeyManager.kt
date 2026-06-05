package com.example.data.security

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

class DatabaseKeyManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_db_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getDatabasePassphrase(): ByteArray {
        var keyBase64 = prefs.getString("db_key", null)
        if (keyBase64 == null) {
            val random = SecureRandom()
            val keyBytes = ByteArray(32)
            random.nextBytes(keyBytes)
            keyBase64 = Base64.encodeToString(keyBytes, Base64.DEFAULT)
            prefs.edit().putString("db_key", keyBase64).commit()
        }
        return Base64.decode(keyBase64, Base64.DEFAULT)
    }
}
