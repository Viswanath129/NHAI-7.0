package com.example.ai.utils

import com.google.mlkit.vision.face.Face

object HeadPoseEstimator {
    /**
     * Extracts pitch, yaw, and roll from an ML Kit Face object.
     * pitch: HeadEulerAngleX (up/down)
     * yaw: HeadEulerAngleY (left/right)
     * roll: HeadEulerAngleZ (tilt)
     */
    fun getHeadPose(face: Face): FloatArray {
        return floatArrayOf(
            face.headEulerAngleX,
            face.headEulerAngleY,
            face.headEulerAngleZ
        )
    }

    fun isPoseValid(pitch: Float, yaw: Float, roll: Float, tolerance: Float = 15.0f): Boolean {
        return kotlin.math.abs(pitch) <= tolerance &&
               kotlin.math.abs(yaw) <= tolerance &&
               kotlin.math.abs(roll) <= tolerance
    }
}
