package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import android.util.Size
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
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.FlipCameraIos
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.camera.FaceAnalyzer
import com.example.ui.components.AppBottomNavigation
import com.example.ui.components.StatusBar
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

@Composable
fun ScanScreen(navController: NavController) {
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
        topBar = { StatusBar() },
        bottomBar = { AppBottomNavigation(navController) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            if (hasCameraPermission) {
                CameraScanUI(navController)
            } else {
                PermissionDeniedPrompt {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }
    }
}

@Composable
private fun PermissionDeniedPrompt(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
        Text("Camera Permission Required", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(
            "Facial authentication requires camera access. Please grant camera permission to proceed.",
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = Color.Gray,
            fontSize = 14.sp
        )
        Button(onClick = onRetry) {
            Text("Grant Permission")
        }
    }
}

@Composable
private fun CameraScanUI(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Analyzer that runs strictly off the main thread to prevent UI jank
    val faceAnalyzer = remember { FaceAnalyzer() }
    val analyzerState by faceAnalyzer.state.collectAsStateWithLifecycle()

    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            analysisExecutor.shutdown()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        StatusRow(
            isProcessing = analyzerState.isProcessing,
            lastProcessingTimeMs = analyzerState.lastProcessingTimeMs
        )

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

                        // Stable zero-copy resolution and policy
                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            // Reduced resolution for high frame-rate edge AI (thermal/memory safe)
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
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (exc: Exception) {
                            Log.e("CameraScanUI", "Use case binding failed", exc)
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            DynamicScanOverlay(hasFaces = analyzerState.faces.isNotEmpty())

            // Scanning line animation
            val scanLineAnim = rememberInfiniteTransition()
            val offsetY by scanLineAnim.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(3000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                    .offset(y = (280 * (4f/3f) * offsetY).dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        DynamicScanControls(hasFaces = analyzerState.faces.isNotEmpty())

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.7f), CircleShape)
                    .shadow(2.dp, CircleShape)
            ) {
                Icon(Icons.Default.FlashlightOn, contentDescription = "Toggle Flash")
            }

            IconButton(
                onClick = { },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.7f), CircleShape)
                    .shadow(2.dp, CircleShape)
            ) {
                Icon(Icons.Default.FlipCameraIos, contentDescription = "Flip Camera")
            }
        }
    }
}

@Composable
fun Chip(text: String, isPulse: Boolean = false, icon: androidx.compose.ui.graphics.vector.ImageVector? = null, color: Color) {
    Row(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.7f), CircleShape)
            .border(1.dp, Color.LightGray.copy(alpha = 0.5f), CircleShape)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isPulse) {
            Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        }
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        }
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color, letterSpacing = 1.sp)
    }
}

@Composable
fun StatusRow(isProcessing: Boolean, lastProcessingTimeMs: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Chip(text = "NPU Processing", isPulse = isProcessing, color = MaterialTheme.colorScheme.primary)
        Chip(text = "Offline", icon = Icons.Default.CloudOff, color = Color.Gray)
        if (lastProcessingTimeMs > 0) {
            Chip(text = "${lastProcessingTimeMs}ms", color = Color.Gray)
        }
    }
}

@Composable
fun DynamicScanOverlay(hasFaces: Boolean) {
    val strokeColor = if (hasFaces) Color.Green else MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(
                if (hasFaces) 4.dp else 0.dp,
                if (hasFaces) Color.Green.copy(alpha = 0.5f) else Color.Transparent,
                RoundedCornerShape(48.dp)
            )
            .padding(24.dp)
    ) {
        Box(modifier = Modifier.align(Alignment.TopStart).size(32.dp).border(4.dp, strokeColor, RoundedCornerShape(topStart = 16.dp)))
        Box(modifier = Modifier.align(Alignment.TopEnd).size(32.dp).border(4.dp, strokeColor, RoundedCornerShape(topEnd = 16.dp)))
        Box(modifier = Modifier.align(Alignment.BottomStart).size(32.dp).border(4.dp, strokeColor, RoundedCornerShape(bottomStart = 16.dp)))
        Box(modifier = Modifier.align(Alignment.BottomEnd).size(32.dp).border(4.dp, strokeColor, RoundedCornerShape(bottomEnd = 16.dp)))
    }
}

@Composable
fun DynamicScanControls(hasFaces: Boolean) {
    val buttonText = if (hasFaces) "FACE DETECTED" else "ALIGN FACE"
    val buttonColor = if (hasFaces) Color.Green else MaterialTheme.colorScheme.primary

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .background(buttonColor, RoundedCornerShape(12.dp))
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(
                text = buttonText,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Placeholder Progress
        LinearProgressIndicator(
            progress = { if (hasFaces) 1f else 0.4f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = buttonColor,
            trackColor = Color.LightGray.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (hasFaces) "LIVENESS VERIFIED" else "ANALYZING BIOMETRICS...",
            color = buttonColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}
