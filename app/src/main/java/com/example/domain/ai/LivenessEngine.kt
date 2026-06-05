package com.example.domain.ai

import android.graphics.Bitmap
import com.example.domain.models.LivenessResult

interface LivenessEngine {
    fun initialize()
    fun analyzeLiveness(faceCropBitmap: Bitmap, pitch: Float, yaw: Float, roll: Float, ear: Float): LivenessResult
    fun close()
}
