package com.example.camera

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * State representing the face analysis pipeline
 */
data class FaceAnalyzerState(
    val faces: List<Face> = emptyList(),
    val isProcessing: Boolean = false,
    val error: String? = null,
    val lastProcessingTimeMs: Long = 0,
    val resolutionWidth: Int = 0,
    val resolutionHeight: Int = 0
)

class FaceAnalyzer : ImageAnalysis.Analyzer {

    private val _state = MutableStateFlow(FaceAnalyzerState())
    val state: StateFlow<FaceAnalyzerState> = _state.asStateFlow()

    // Configured for fast offline tracking of faces
    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .build()

    private val detector = FaceDetection.getClient(options)
    private var lastAnalyzedTimestamp = 0L

    companion object {
        private const val TAG = "FaceAnalyzer"
        // Throttling frames: process at most 10 frames per second (100ms)
        private const val FRAME_THROTTLE_MS = 100L
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val currentTimestamp = System.currentTimeMillis()
        
        // Prevent backpressure: skip frames if we're already processing or within throttle limit
        if (_state.value.isProcessing || currentTimestamp - lastAnalyzedTimestamp < FRAME_THROTTLE_MS) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            // Null frame, typically rare but must be handled
            imageProxy.close()
            return
        }

        // Direct zero-copy processing using YUV_420_888 which is ImageProxy natively
        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        
        _state.update { 
            it.copy(
                isProcessing = true, 
                error = null,
                resolutionWidth = imageProxy.width,
                resolutionHeight = imageProxy.height
            ) 
        }

        val startTime = System.currentTimeMillis()

        detector.process(inputImage)
            .addOnSuccessListener { faces ->
                val processingTime = System.currentTimeMillis() - startTime
                Log.d(TAG, "Processed ${faces.size} faces in ${processingTime}ms")
                
                _state.update {
                    it.copy(
                        faces = faces,
                        lastProcessingTimeMs = processingTime,
                        isProcessing = false
                    )
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Face detection failed", e)
                _state.update {
                    it.copy(
                        error = e.message ?: "Unknown error",
                        isProcessing = false
                    )
                }
            }
            .addOnCompleteListener {
                // REQUIRED: Always flush the frame buffer so the pipeline can continue
                imageProxy.close()
                lastAnalyzedTimestamp = System.currentTimeMillis()
            }
    }
}
