package com.example.domain.models

data class VerificationResult(
    val isValid: Boolean,
    val confidenceScore: Float,
    val matchedEmployeeId: String? = null,
    val error: String? = null
)
