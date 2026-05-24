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
import androidx.compose.ui.platform.LocalLifecycleOwner
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
import java.util.concurrent.Executors

enum class EnrollmentStep(val instructions: String) {
    FRONT("Look directly at the camera"),
    LEFT("Turn your head slightly to the left"),
    RIGHT("Turn your head slightly to the right"),
    BLINK("Blink both eyes slowly"),
    COMPLETE("Enrollment complete")
}

@Composable
fun EnrollmentCameraScreen(navController: NavController) {
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
        topBar = { StatusBar(title = "BIOMETRIC ENROLLMENT") }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            if (hasCameraPermission) {
                EnrollmentCameraUI(navController)
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
private fun EnrollmentCameraUI(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    val faceAnalyzer = remember { FaceAnalyzer() }
    val analyzerState by faceAnalyzer.state.collectAsStateWithLifecycle()
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

    var currentStep by remember { mutableStateOf(EnrollmentStep.FRONT) }

    DisposableEffect(Unit) {
        onDispose {
            analysisExecutor.shutdown()
        }
    }

    // Step logic based on face rotations
    LaunchedEffect(analyzerState.faces) {
        val faces = analyzerState.faces
        if (faces.isNotEmpty()) {
            val face = faces[0]
            val rotY = face.headEulerAngleY // Left/Right
            
            when (currentStep) {
                EnrollmentStep.FRONT -> {
                    if (rotY > -5 && rotY < 5) {
                        delay(1000)
                        currentStep = EnrollmentStep.LEFT
                    }
                }
                EnrollmentStep.LEFT -> {
                    if (rotY > 15) { // Turning rightwards but depending on mirrored camera
                        delay(1000)
                        currentStep = EnrollmentStep.RIGHT
                    }
                }
                EnrollmentStep.RIGHT -> {
                    if (rotY < -15) {
                        delay(1000)
                        currentStep = EnrollmentStep.BLINK
                    }
                }
                EnrollmentStep.BLINK -> {
                    val rightEyeOpen = face.rightEyeOpenProbability ?: 1.0f
                    val leftEyeOpen = face.leftEyeOpenProbability ?: 1.0f
                    if (rightEyeOpen < 0.2f && leftEyeOpen < 0.2f) { // Blink
                        delay(500)
                        currentStep = EnrollmentStep.COMPLETE
                    }
                }
                EnrollmentStep.COMPLETE -> {
                    delay(1500)
                    navController.navigate("enroll_metrics")
                }
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
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            EnrollmentStep.values().filter { it != EnrollmentStep.COMPLETE }.forEach { step ->
                val isActive = currentStep == step
                val isDone = currentStep.ordinal > step.ordinal
                val color = if (isDone) Color.Green else if (isActive) MaterialTheme.colorScheme.primary else Color.Gray
                Box(modifier = Modifier.size(16.dp).background(color, CircleShape))
            }
        }

        Box(
            modifier = Modifier
                .width(280.dp)
                .aspectRatio(3f / 4f)
                .border(2.dp, Color.LightGray, RoundedCornerShape(48.dp))
                .clip(RoundedCornerShape(48.dp))
        ) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                    }
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
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

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
                        } catch (exc: Exception) {
                            Log.e("EnrollmentCameraUI", "Use case binding failed", exc)
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
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
                progress = { (currentStep.ordinal) / 4f },
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
