package com.example.domain.ai

import android.graphics.Bitmap
import com.example.domain.models.VerificationResult

interface FaceRecognitionEngine {
    fun initialize()
    fun extractEmbedding(faceBitmap: Bitmap): FloatArray?
    fun verifyIdentity(probeEmbedding: FloatArray, enrolledEmbedding: FloatArray): VerificationResult
    fun close()
}
