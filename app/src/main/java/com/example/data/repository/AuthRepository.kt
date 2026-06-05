package com.example.data.repository

import com.example.domain.models.VerificationResult

interface AuthRepository {
    suspend fun loginWithBiometric(employeeId: String, probeEmbedding: FloatArray): VerificationResult
    suspend fun loginWithPin(employeeId: String, pin: String): Result<Unit>
    suspend fun logout()
    fun isSessionValid(): Boolean
    fun getLoggedInUserId(): String?
}
