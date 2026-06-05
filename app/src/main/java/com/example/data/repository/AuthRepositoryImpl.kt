package com.example.data.repository

import com.example.data.EmployeeDao
import com.example.data.security.SessionManager
import com.example.data.AuditLogDao
import com.example.data.AuditLog
import com.example.domain.ai.FaceRecognitionEngine
import com.example.domain.models.VerificationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val employeeDao: EmployeeDao,
    private val auditLogDao: AuditLogDao,
    private val sessionManager: SessionManager,
    private val faceRecognitionEngine: FaceRecognitionEngine
) : AuthRepository {

    override suspend fun loginWithBiometric(employeeId: String, probeEmbedding: FloatArray): VerificationResult = withContext(Dispatchers.IO) {
        if (sessionManager.isLockedOut()) {
            return@withContext VerificationResult(false, 0f, error = "Account locked out due to too many failed attempts")
        }

        val profile = employeeDao.getProfile(employeeId)
            ?: return@withContext VerificationResult(false, 0f, error = "Employee not found")

        val enrolledEmbeddingRaw = profile.faceEmbedding
        val verification = faceRecognitionEngine.verifyIdentity(probeEmbedding, enrolledEmbeddingRaw)
        
        if (verification.isValid) {
            sessionManager.resetFailedAttempts()
            sessionManager.createSession(employeeId)
            auditLogDao.insertLog(AuditLog(eventType = "LOGIN_SUCCESS", employeeId = employeeId, details = "Biometric authenticated"))
        } else {
            sessionManager.recordFailedAttempt()
            auditLogDao.insertLog(AuditLog(eventType = "LOGIN_FAILED", employeeId = employeeId, details = "Biometric mismatch. Score: ${verification.confidenceScore}"))
        }

        return@withContext verification.copy(matchedEmployeeId = if (verification.isValid) employeeId else null)
    }

    override suspend fun loginWithPin(employeeId: String, pin: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (sessionManager.isLockedOut()) {
            return@withContext Result.failure(Exception("Account locked out due to too many failed attempts"))
        }

        val profile = employeeDao.getProfile(employeeId)
            ?: return@withContext Result.failure(Exception("Employee not found"))

        // Add real PIN comparison here, hashed with Argon2 or BCrypt
        if (profile.department == "Demo") { // Placeholder condition
            sessionManager.resetFailedAttempts()
            sessionManager.createSession(employeeId)
            Result.success(Unit)
        } else {
            sessionManager.recordFailedAttempt()
            Result.failure(Exception("Invalid PIN"))
        }
    }

    override suspend fun logout() = withContext(Dispatchers.IO) {
        sessionManager.clearSession()
    }

    override fun isSessionValid(): Boolean {
        return sessionManager.getSessionUserId() != null
    }

    override fun getLoggedInUserId(): String? {
        return sessionManager.getSessionUserId()
    }
}
