package com.example.domain.models

data class LivenessResult(
    val isLive: Boolean,
    val spoofScore: Float,
    val failureReason: String? = null
)
