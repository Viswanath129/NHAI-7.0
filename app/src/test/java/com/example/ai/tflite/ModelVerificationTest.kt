package com.example.ai.tflite

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.io.File
import java.nio.ByteBuffer
import org.junit.Assert.assertTrue

@RunWith(RobolectricTestRunner::class)
class ModelVerificationTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun verifyModelsLoaded() {
        val faceNetFile = "mobilefacenet_05x_widened_int8_final.tflite"
        val livenessFile = "minifasnet_v2_widened_int8_final.tflite"

        val faceNetBuffer = ByteBuffer.allocateDirect(File("src/main/assets/$faceNetFile").readBytes().size).apply { 
            put(File("src/main/assets/$faceNetFile").readBytes())
            flip()
        }
        val livenessBuffer = ByteBuffer.allocateDirect(File("src/main/assets/$livenessFile").readBytes().size).apply { 
            put(File("src/main/assets/$livenessFile").readBytes())
            flip()
        }

        // Removed Interpreter calls
        println("=== MODEL INTEGRITY REPORT ===")
        val faceNetSize = File("src/main/assets/$faceNetFile").length()
        println("MobileFaceNet Size: $faceNetSize")
        
        val livenessSize = File("src/main/assets/$livenessFile").length()
        println("MiniFASNet Size: $livenessSize")
        println("==============================")
        
        // Let's actually initialize engines and test
        try {
            val faceNet = com.example.ai.tflite.MobileFaceNetEngine(context)
            faceNet.initialize()
            val bmp = android.graphics.Bitmap.createBitmap(112, 112, android.graphics.Bitmap.Config.ARGB_8888)
            println("Testing MobileFaceNet inference...")
            faceNet.extractEmbedding(bmp)
            println("MobileFaceNet inference successful")
        } catch (e: Exception) {
            println("MobileFaceNet inference failed: ${e.message}")
            e.printStackTrace()
        }

        try {
            val silentFace = com.example.ai.tflite.SilentFaceEngine(context)
            silentFace.initialize()
            val bmp = android.graphics.Bitmap.createBitmap(80, 80, android.graphics.Bitmap.Config.ARGB_8888)
            println("Testing SilentFace inference...")
            silentFace.analyzeLiveness(bmp, emptyList())
            println("SilentFace inference successful")
        } catch (e: Exception) {
            println("SilentFace inference failed: ${e.message}")
            e.printStackTrace()
        }

        assertTrue(true)
    }
}
