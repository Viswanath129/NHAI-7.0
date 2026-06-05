package com.example.ai.tflite

import android.content.Context
import android.graphics.Bitmap
import com.example.domain.ai.LivenessEngine
import com.example.domain.models.LivenessResult
import org.tensorflow.lite.Interpreter

class LivenessModelUnavailableException(message: String) : Exception(message)

class SilentFaceEngine(
    private val context: Context,
    private val spoofThreshold: Float = 0.85f
) : LivenessEngine {

    private var interpreter: Interpreter? = null
    private var isOutputFloat = true
    private var outputBuffer: java.nio.ByteBuffer? = null

    private val inputBuffer by lazy {
        java.nio.ByteBuffer.allocateDirect(1 * 80 * 80 * 3).apply {
            order(java.nio.ByteOrder.nativeOrder())
        }
    }

    override fun initialize() {
        try {
            val modelBuffer = org.tensorflow.lite.support.common.FileUtil.loadMappedFile(context, "minifasnet_v2_widened_int8_final.tflite")
            val options = DelegateManager.createInterpreterOptions(context, useNnApi = true)
            val newInterpreter = Interpreter(modelBuffer, options)
            
            val outputTensor = newInterpreter.getOutputTensor(0)
            isOutputFloat = outputTensor.dataType() != org.tensorflow.lite.DataType.INT8
            
            val size = if (isOutputFloat) 3 * 4 else 3
            outputBuffer = java.nio.ByteBuffer.allocateDirect(size).apply {
                order(java.nio.ByteOrder.nativeOrder())
            }
            
            interpreter = newInterpreter
        } catch (e: Exception) {
            e.printStackTrace()
            interpreter = null
        }
    }

    override fun analyzeLiveness(faceCropBitmap: Bitmap, pitch: Float, yaw: Float, roll: Float, ear: Float): LivenessResult {
        if (interpreter == null) {
            throw LivenessModelUnavailableException("minifasnet_v2_widened_int8_final.tflite is missing or failed to initialize.")
        }
        
        try {
            android.util.Log.d("SilentFace", "VERIFY_STEP_3A: Starting analysis")
            
            if (ear > 0.0f && ear < 0.2f) { // Blink detected
                 android.util.Log.d("SilentFace", "Blink detected, EAR: $ear")
            }
            
            // 2. Head Pose Validation
            if (!isHeadPoseValid(pitch, yaw, roll)) {
                android.util.Log.d("SilentFace", "VERIFY_STEP_3B: Head pose invalid. Pitch: $pitch, Yaw: $yaw, Roll: $roll")
                return LivenessResult(isLive = false, spoofScore = 1.0f, failureReason = "Invalid Head Pose")
            }
            
            android.util.Log.d("SilentFace", "VERIFY_STEP_3C: Preprocessing for liveness model")
            // 3. Texture Analysis - MiniFASNet inference
            val scaledBitmap = Bitmap.createScaledBitmap(faceCropBitmap, 80, 80, false)
            
            inputBuffer.rewind()
            val intValues = IntArray(80 * 80)
            scaledBitmap.getPixels(intValues, 0, 80, 0, 0, 80, 80)
            for (pixelValue in intValues) {
                inputBuffer.put((pixelValue shr 16 and 0xFF).toByte())
                inputBuffer.put((pixelValue shr 8 and 0xFF).toByte())
                inputBuffer.put((pixelValue and 0xFF).toByte())
            }
            
            android.util.Log.d("SilentFace", "VERIFY_STEP_3D: Allocating output buffer. Input shape: ${interpreter?.getInputTensor(0)?.shape()?.contentToString()}, Output shape: ${interpreter?.getOutputTensor(0)?.shape()?.contentToString()}")
            val outBuf = outputBuffer ?: throw LivenessModelUnavailableException("Output buffer not initialized")
            outBuf.rewind()
            
            android.util.Log.d("SilentFace", "VERIFY_STEP_3E: Calling interpreter.run()")
            interpreter?.run(inputBuffer, outBuf)
            android.util.Log.d("SilentFace", "VERIFY_STEP_3F: interpreter.run() finished")
            
            outBuf.rewind()
            // Read 3 values (float or byte), map appropriately
            val outputScores = FloatArray(3)
            if (isOutputFloat) {
                for (i in 0 until 3) {
                    outputScores[i] = outBuf.float
                }
            } else {
                for (i in 0 until 3) {
                    val value = outBuf.get().toInt()
                    outputScores[i] = if (value < 0) (value + 256) / 255f else value / 127f
                }
            }
            
            val liveScore = outputScores[0]
            val spoofScore = 1.0f - liveScore 
            
            return LivenessResult(
                isLive = spoofScore < spoofThreshold,
                spoofScore = spoofScore
            )
        } catch (e: Exception) {
            android.util.Log.e("SilentFace", "VERIFY_STEP_3_ERROR: " + e.message, e)
            throw e
        }
    }

    override fun close() {
        interpreter?.close()
        interpreter = null
    }

    private fun isHeadPoseValid(pitch: Float, yaw: Float, roll: Float): Boolean {
        // Implement basic Head Pose validation
        val maxPitch = 20f
        val maxYaw = 20f
        val maxRoll = 20f
        return kotlin.math.abs(pitch) <= maxPitch &&
               kotlin.math.abs(yaw) <= maxYaw &&
               kotlin.math.abs(roll) <= maxRoll
    }
}
