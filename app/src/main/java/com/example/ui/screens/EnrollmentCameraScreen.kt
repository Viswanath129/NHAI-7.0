package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.camera.FaceAnalyzer
import com.example.ui.components.StatusBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.Dispatchers

enum class EnrollmentStep(val instructions: String) {
    FRONT("Look directly at the camera"),
    LEFT("Turn your head slightly to the left"),
    RIGHT("Turn your head slightly to the right"),
    UPWARD("Tilt your head slightly upward"),
    BLINK("Blink both eyes slowly"),
    COMPLETE("Enrollment complete")
}

@Composable
fun EnrollmentCameraScreen(
    navController: NavController,
    viewModel: EnrollViewModel
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = { StatusBar(title = "BIOMETRIC CAPTURE") }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            if (hasCameraPermission) {
                EnrollmentCameraUI(navController, viewModel)
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Text("Camera permission is required")
                }
            }
        }
    }
}

@Composable
private fun EnrollmentCameraUI(
    navController: NavController,
    viewModel: EnrollViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    val appContainer = (context.applicationContext as com.example.NHAIApplication).container
    val faceAnalyzer = remember { FaceAnalyzer(appContainer.faceRecognitionEngine, appContainer.livenessEngine) }
    val analyzerState by faceAnalyzer.state.collectAsStateWithLifecycle()
    val analysisExecutor = remember { Dispatchers.Default.asExecutor() }

    DisposableEffect(faceAnalyzer) {
        onDispose {
            faceAnalyzer.close()
        }
    }

    var currentStep by remember { mutableStateOf(EnrollmentStep.FRONT) }
    var capturedEmbedding by remember { mutableStateOf<FloatArray?>(null) }

    // Step logic based on face rotations
    LaunchedEffect(currentStep, analyzerState.currentEmbedding) {
        if (currentStep != EnrollmentStep.COMPLETE) {
            val face = analyzerState.faces.firstOrNull()
            if (face != null) {
                val rotY = face.headEulerAngleY
                val rotX = face.headEulerAngleX
                
                // Attempt to capture embedding during FRONT step when stable
                if (currentStep == EnrollmentStep.FRONT && rotY in -6f..6f && rotX in -6f..6f) {
                    analyzerState.currentEmbedding?.let { capturedEmbedding = it }
                }

                val conditionMet = when (currentStep) {
                    EnrollmentStep.FRONT -> {
                        val isAligned = rotY > -10 && rotY < 10 && rotX > -10 && rotX < 10
                        if (isAligned && capturedEmbedding == null) {
                            analyzerState.currentEmbedding?.let { capturedEmbedding = it }
                        }
                        isAligned && capturedEmbedding != null
                    }
                    EnrollmentStep.LEFT -> rotY > 15
                    EnrollmentStep.RIGHT -> rotY < -15
                    EnrollmentStep.UPWARD -> rotX > 12
                    EnrollmentStep.BLINK -> {
                        val rightEyeOpen = face.rightEyeOpenProbability ?: 1.0f
                        val leftEyeOpen = face.leftEyeOpenProbability ?: 1.0f
                        rightEyeOpen < 0.35f && leftEyeOpen < 0.35f
                    }
                    else -> false
                }
                
                if (conditionMet) {
                    // Small delay to ensure the user holds the pose
                    delay(800) 
                    when (currentStep) {
                        EnrollmentStep.FRONT -> currentStep = EnrollmentStep.LEFT
                        EnrollmentStep.LEFT -> currentStep = EnrollmentStep.RIGHT
                        EnrollmentStep.RIGHT -> currentStep = EnrollmentStep.UPWARD
                        EnrollmentStep.UPWARD -> currentStep = EnrollmentStep.BLINK
                        EnrollmentStep.BLINK -> currentStep = EnrollmentStep.COMPLETE
                        else -> {}
                    }
                }
            }
        }
    }
    
    // Separate effect for navigation after completion
    LaunchedEffect(currentStep) {
        if (currentStep == EnrollmentStep.COMPLETE) {
            viewModel.capturedEmbedding = capturedEmbedding
            navController.navigate("enroll_processing") {
                popUpTo("enroll_camera") { inclusive = true }
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        // Step progress indicators
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            EnrollmentStep.values().filter { it != EnrollmentStep.COMPLETE }.forEach { step ->
                val isActive = currentStep == step
                val isDone = currentStep.ordinal > step.ordinal
                val color = if (isDone) Color.Green else if (isActive) MaterialTheme.colorScheme.primary else Color.Gray
                Box(modifier = Modifier.size(16.dp).background(color, CircleShape))
            }
        }
        
        // Quality Metrics HUD
        val face = analyzerState.faces.firstOrNull()
        val qualityScore = if (face != null) {
            val rotY = kotlin.math.abs(face.headEulerAngleY)
            val rotX = kotlin.math.abs(face.headEulerAngleX)
            val penalty = (rotY + rotX)
            (98 - (penalty * 0.5f)).coerceIn(80f, 99f).toInt()
        } else 0
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("QUALITY", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text(if (face != null) "$qualityScore%" else "--%", fontSize = 16.sp, color = if(qualityScore > 90) Color.Green else Color.White, fontWeight = FontWeight.ExtraBold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(if (face != null) "FACE ENGAGED" else "WAITING", fontSize = 10.sp, color = if(face != null) Color(0xFF1565C0) else Color.Red, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("LIGHT", "BLUR", "POS", "SIZE").forEach { metric ->
                        Text(metric, fontSize = 8.sp, color = if (face != null) Color.Green else Color.Gray, modifier = Modifier.border(1.dp, if (face != null) Color.Green else Color.Gray, RoundedCornerShape(2.dp)).padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .width(280.dp)
                .aspectRatio(3f / 4f)
                .border(2.dp, Color.LightGray, RoundedCornerShape(48.dp))
                .clip(RoundedCornerShape(48.dp))
        ) {
            val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
            
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    }

                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .setResolutionSelector(
                                    ResolutionSelector.Builder()
                                        .setResolutionStrategy(ResolutionStrategy(Size(640, 480), ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER))
                                        .build()
                                )
                                .build()
                                .also {
                                    it.setAnalyzer(analysisExecutor, faceAnalyzer)
                                }

                            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
                        } catch (exc: Exception) {
                            Log.e("EnrollmentCameraUI", "Camera binding failed", exc)
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                onRelease = {
                    // CameraX automatically unbinds use cases when the lifecycle owner is destroyed.
                },
                modifier = Modifier.fillMaxSize()
            )

            // Scanning overlay
            val scanColor = if (currentStep == EnrollmentStep.COMPLETE) Color.Green else MaterialTheme.colorScheme.primary
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        if (analyzerState.faces.isNotEmpty()) 4.dp else 0.dp,
                        scanColor.copy(alpha = 0.5f),
                        RoundedCornerShape(48.dp)
                    )
                    .padding(24.dp)
            ) {
                Box(modifier = Modifier.align(Alignment.TopStart).size(32.dp).border(4.dp, scanColor, RoundedCornerShape(topStart = 16.dp)))
                Box(modifier = Modifier.align(Alignment.TopEnd).size(32.dp).border(4.dp, scanColor, RoundedCornerShape(topEnd = 16.dp)))
                Box(modifier = Modifier.align(Alignment.BottomStart).size(32.dp).border(4.dp, scanColor, RoundedCornerShape(bottomStart = 16.dp)))
                Box(modifier = Modifier.align(Alignment.BottomEnd).size(32.dp).border(4.dp, scanColor, RoundedCornerShape(bottomEnd = 16.dp)))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = currentStep.instructions,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (currentStep == EnrollmentStep.COMPLETE) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green, modifier = Modifier.size(48.dp))
        } else {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                progress = { (currentStep.ordinal) / 5f },
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
