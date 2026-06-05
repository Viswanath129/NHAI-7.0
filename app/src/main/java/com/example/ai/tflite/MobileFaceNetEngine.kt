package com.example.ai.tflite

import android.content.Context
import android.graphics.Bitmap
import com.example.domain.ai.FaceRecognitionEngine
import com.example.domain.models.VerificationResult
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.io.File
import kotlin.math.sqrt

class ModelNotAvailableException(message: String) : Exception(message)

class MobileFaceNetEngine(
    private val context: Context,
    private val threshold: Float = 0.40f
) : FaceRecognitionEngine {

    private var interpreter: Interpreter? = null
    private var isOutputFloat = true
    private var outputBuffer: ByteBuffer? = null

    private val inputBuffer by lazy {
        ByteBuffer.allocateDirect(1 * 112 * 112 * 3).apply {
            order(java.nio.ByteOrder.nativeOrder())
        }
    }

    override fun initialize() {
        try {
            val modelBuffer = org.tensorflow.lite.support.common.FileUtil.loadMappedFile(context, "mobilefacenet_05x_widened_int8_final.tflite")
            val options = DelegateManager.createInterpreterOptions(context, useNnApi = true)
            val newInterpreter = Interpreter(modelBuffer, options)
            
            val outputTensor = newInterpreter.getOutputTensor(0)
            isOutputFloat = outputTensor.dataType() != org.tensorflow.lite.DataType.INT8
            
            val size = if (isOutputFloat) 512 * 4 else 512
            outputBuffer = ByteBuffer.allocateDirect(size).apply {
                order(java.nio.ByteOrder.nativeOrder())
            }
            
            interpreter = newInterpreter
        } catch (e: Exception) {
            e.printStackTrace()
            interpreter = null
        }
    }

    override fun extractEmbedding(faceBitmap: Bitmap): FloatArray? {
        if (interpreter == null) {
            throw ModelNotAvailableException("mobilefacenet_05x_widened_int8_final.tflite is missing or failed to initialize.")
        }
        
        try {
            android.util.Log.d("MobileFaceNet", "VERIFY_STEP_1A: Preprocessing scaled bitmap")
            // 1. Preprocess: Resize to 112x112, Normalize, Convert to ByteBuffer
            val scaledBitmap = Bitmap.createScaledBitmap(faceBitmap, 112, 112, false)
            
            inputBuffer.rewind()
            val intValues = IntArray(112 * 112)
            scaledBitmap.getPixels(intValues, 0, 112, 0, 0, 112, 112)
            
            // MobileFaceNet INT8 specific scaling can be applied here. 
            // For standard INT8, we cast rgb to bytes
            for (pixelValue in intValues) {
                val r = (pixelValue shr 16 and 0xFF).toByte()
                val g = (pixelValue shr 8 and 0xFF).toByte()
                val b = (pixelValue and 0xFF).toByte()
                inputBuffer.put(r)
                inputBuffer.put(g)
                inputBuffer.put(b)
            }
            
            android.util.Log.d("MobileFaceNet", "VERIFY_STEP_1B: Preprocessing successful. Initializing output buffer. Tensor sizes... Input shape: ${interpreter?.getInputTensor(0)?.shape()?.contentToString()}, Output shape: ${interpreter?.getOutputTensor(0)?.shape()?.contentToString()}")
            val outBuf = outputBuffer ?: throw ModelNotAvailableException("Output buffer not initialized")
            outBuf.rewind()
            
            android.util.Log.d("MobileFaceNet", "VERIFY_STEP_1C: Calling interpreter.run()")
            interpreter?.run(inputBuffer, outBuf)
            android.util.Log.d("MobileFaceNet", "VERIFY_STEP_1D: interpreter.run() finished")
            outBuf.rewind()
            
            // 3. Postprocess: L2 Normalize the output array
            val embedding = FloatArray(512)
            if (isOutputFloat) {
                for (i in 0 until 512) {
                    embedding[i] = outBuf.float
                }
            } else {
                for (i in 0 until 512) {
                    embedding[i] = outBuf.get().toFloat()
                }
            }
            
            android.util.Log.d("MobileFaceNet", "VERIFY_STEP_1E: Output buffering and post-processing successful")
            var sum = 0.0f
            for (v in embedding) sum += v * v
            val norm = Math.sqrt(sum.toDouble()).toFloat()
            if (norm > 0) {
                for (i in embedding.indices) {
                    embedding[i] /= norm
                }
            }
            
            return embedding 
        } catch (e: Exception) {
            android.util.Log.e("MobileFaceNet", "VERIFY_STEP_1_ERROR: " + e.message, e)
            throw e
        }
    }

    override fun verifyIdentity(probeEmbedding: FloatArray, enrolledEmbedding: FloatArray): VerificationResult {
        val similarity = cosineSimilarity(probeEmbedding, enrolledEmbedding)
        return VerificationResult(
            isValid = similarity >= threshold,
            confidenceScore = similarity
        )
    }

    override fun close() {
        interpreter?.close()
        interpreter = null
    }

    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        var dotProduct = 0.0
        var normA = 0.0
        var normB = 0.0
        for (i in a.indices) {
            dotProduct += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        val denom = sqrt(normA) * sqrt(normB)
        return if (denom == 0.0) 0.0f else (dotProduct / denom).toFloat()
    }
}
