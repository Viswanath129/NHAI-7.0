package com.example.camera

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.ai.utils.EARCalculator
import com.example.ai.utils.FacePreprocessor
import com.example.domain.ai.FaceRecognitionEngine
import com.example.domain.ai.LivenessEngine
import com.example.domain.models.LivenessResult
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class FaceAnalyzerState(
    val faces: List<Face> = emptyList(),
    val currentEmbedding: FloatArray? = null,
    val livenessResult: LivenessResult? = null,
    val isProcessing: Boolean = false,
    val error: String? = null,
    val lastProcessingTimeMs: Long = 0,
    val resolutionWidth: Int = 0,
    val resolutionHeight: Int = 0
)

class FaceAnalyzer(
    private val faceRecognitionEngine: FaceRecognitionEngine,
    private val livenessEngine: LivenessEngine
) : ImageAnalysis.Analyzer {

    private val _state = MutableStateFlow(FaceAnalyzerState())
    val state: StateFlow<FaceAnalyzerState> = _state.asStateFlow()

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()

    private val engineLock = Any()
    private var isClosed = false
    private val detector = FaceDetection.getClient(options)
    private var lastAnalyzedTimestamp = 0L

    companion object {
        private const val TAG = "FaceAnalyzer"
        private const val FRAME_THROTTLE_MS = 150L
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        synchronized(engineLock) {
            if (isClosed) {
                imageProxy.close()
                return
            }
        }
        val currentTimestamp = System.currentTimeMillis()
        
        if (_state.value.isProcessing || currentTimestamp - lastAnalyzedTimestamp < FRAME_THROTTLE_MS) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        var bitmap: Bitmap? = null
        try {
           bitmap = imageProxy.toBitmap()
        } catch (e: Exception) {
           Log.e(TAG, "Failed to get bitmap", e)
        }

        // Use InputImage.fromBitmap to ensure coordinate space matches exactly the upright bitmap
        val inputImage = if (bitmap != null) {
            InputImage.fromBitmap(bitmap, 0)
        } else {
            InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        }

        synchronized(engineLock) {
            if (isClosed) {
                imageProxy.close()
                return
            }
            _state.update { 
                it.copy(isProcessing = true, error = null, resolutionWidth = imageProxy.width, resolutionHeight = imageProxy.height) 
            }
        }

        val startTime = System.currentTimeMillis()

        try {
            detector.process(inputImage)
                .addOnSuccessListener { faces ->
                    var embedding: FloatArray? = null
                    var livenessResult: LivenessResult? = null

                    synchronized(engineLock) {
                        if (isClosed) return@addOnSuccessListener
                        val processingTime = System.currentTimeMillis() - startTime
                        
                        if (faces.isNotEmpty() && bitmap != null) {
                            val face = faces[0]
                            try {
                                Log.d(TAG, "VERIFY_STEP_1: Start extractEmbedding")
                                
                                val faceCrop = FacePreprocessor.cropAndAlignFace(bitmap, face.boundingBox)
                                
                                embedding = faceRecognitionEngine.extractEmbedding(faceCrop)
                                Log.d(TAG, "VERIFY_STEP_2: ExtractEmbedding succeeded")
                                
                                val leftEyeContour = face.getContour(FaceContour.LEFT_EYE)?.points ?: emptyList()
                                val rightEyeContour = face.getContour(FaceContour.RIGHT_EYE)?.points ?: emptyList()
                                
                                val leftEar = EARCalculator.calculateEAR(leftEyeContour)
                                val rightEar = EARCalculator.calculateEAR(rightEyeContour)
                                val avgEar = if (leftEar > 0 && rightEar > 0) (leftEar + rightEar) / 2f else 0f
                                
                                Log.d(TAG, "VERIFY_STEP_3: Start analyzeLiveness")
                                livenessResult = livenessEngine.analyzeLiveness(
                                    faceCrop,
                                    face.headEulerAngleX,
                                    face.headEulerAngleY,
                                    face.headEulerAngleZ,
                                    avgEar
                                )
                                Log.d(TAG, "VERIFY_STEP_4: AnalyzeLiveness succeeded")
                            } catch (e: Exception) {
                                Log.e(TAG, "VERIFY_STEP_ERROR: " + e.message, e)
                                _state.update { copy -> copy.copy(error = e.message ?: "Model Execution Failed") }
                            }
                        }

                        _state.update {
                            it.copy(
                                faces = faces,
                                currentEmbedding = embedding,
                                livenessResult = livenessResult,
                                lastProcessingTimeMs = processingTime,
                                isProcessing = false
                            )
                        }
                    }
                }
                .addOnFailureListener { e ->
                    synchronized(engineLock) {
                        if (isClosed) return@addOnFailureListener
                        _state.update {
                            it.copy(error = e.message ?: "Unknown error", isProcessing = false)
                        }
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                    synchronized(engineLock) {
                        if (!isClosed) {
                            lastAnalyzedTimestamp = System.currentTimeMillis()
                        }
                    }
                }
        } catch (e: Exception) {
            synchronized(engineLock) {
                _state.update { it.copy(isProcessing = false) }
            }
            imageProxy.close()
        }
    }

    fun close() {
        synchronized(engineLock) {
            if (isClosed) return
            isClosed = true
            try {
                detector.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error closing detector", e)
            }
            try {
                faceRecognitionEngine.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error closing faceRecognitionEngine", e)
            }
            try {
                livenessEngine.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error closing livenessEngine", e)
            }
        }
    }
}
