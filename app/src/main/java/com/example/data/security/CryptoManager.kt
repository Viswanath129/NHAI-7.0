package com.example.data.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class CryptoManager {

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    private fun getKey(alias: String): SecretKey {
        val existingKey = keyStore.getEntry(alias, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey(alias)
    }

    private fun createKey(alias: String): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val parameterSpec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        keyGenerator.init(parameterSpec)
        return keyGenerator.generateKey()
    }

    fun getEncryptCipher(alias: String): Cipher {
        val cipher = Cipher.getInstance("${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_GCM}/${KeyProperties.ENCRYPTION_PADDING_NONE}")
        cipher.init(Cipher.ENCRYPT_MODE, getKey(alias))
        return cipher
    }

    fun getDecryptCipher(alias: String, iv: ByteArray): Cipher {
        val cipher = Cipher.getInstance("${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_GCM}/${KeyProperties.ENCRYPTION_PADDING_NONE}")
        cipher.init(Cipher.DECRYPT_MODE, getKey(alias), GCMParameterSpec(128, iv))
        return cipher
    }

    fun encrypt(alias: String, data: ByteArray): Pair<ByteArray, ByteArray> {
        val cipher = getEncryptCipher(alias)
        val iv = cipher.iv
        val encryptedData = cipher.doFinal(data)
        return Pair(encryptedData, iv)
    }

    fun decrypt(alias: String, iv: ByteArray, encryptedData: ByteArray): ByteArray {
        val cipher = getDecryptCipher(alias, iv)
        return cipher.doFinal(encryptedData)
    }
}
