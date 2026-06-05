package com.example.ai.utils

import android.graphics.Bitmap
import android.util.Log

object AttackValidationSuite {
    private const val TAG = "AttackValidation"

    enum class AttackType {
        PRINTED_PHOTO,
        PHONE_SCREEN_REPLAY,
        CROPPED_FACE,
        PARTIAL_FACE,
        LOW_LIGHT,
        MULTIPLE_FACES
    }

    data class ValidationResult(
        val attackType: AttackType,
        val isAccepted: Boolean,
        val confidence: Float,
        val reason: String
    )

    fun testEnvironment(
        testBitmap: Bitmap,
        attackType: AttackType,
        livenessCheck: (Bitmap) -> Pair<Boolean, Float> // Returns <IsLive, SpoofScore>
    ): ValidationResult {
        Log.d(TAG, "Testing attack vector: $attackType")
        
        return try {
            val (isLive, spoofScore) = livenessCheck(testBitmap)
            val confidence = if (isLive) spoofScore else 1.0f - spoofScore
            
            ValidationResult(
                attackType = attackType,
                isAccepted = isLive,
                confidence = confidence,
                reason = if (isLive) "Model passed as live" else "Spoof detected (Score: $spoofScore)"
            )
        } catch (e: Exception) {
            ValidationResult(
                attackType = attackType,
                isAccepted = false,
                confidence = 0f,
                reason = "Pipeline Error: ${e.message}"
            )
        }
    }

    fun report(results: List<ValidationResult>) {
        Log.i(TAG, "==== ATTACK VALIDATION REPORT ====")
        results.forEach {
            Log.i(TAG, "[${it.attackType.name}] -> Accepted: ${it.isAccepted} | Confidence: ${it.confidence} | Reason: ${it.reason}")
        }
        Log.i(TAG, "==================================")
    }
}
