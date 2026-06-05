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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.camera.FaceAnalyzer
import com.example.ui.components.AppBottomNavigation
import com.example.ui.components.StatusBar
import com.example.data.AppDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.Dispatchers

import com.example.ui.components.getFragmentActivity
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricPrompt
import java.util.concurrent.Executor

enum class AuthStep {
    ALIGN,
    LIVENESS,
    VERIFYING,
    SUCCESS,
    FAILED
}

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
    val coroutineScope = rememberCoroutineScope()
    
    // Analyzer that runs strictly off the main thread to prevent UI jank
    val appContainer = (context.applicationContext as com.example.NHAIApplication).container
    val faceAnalyzer = remember { FaceAnalyzer(appContainer.faceRecognitionEngine, appContainer.livenessEngine) }
    val analyzerState by faceAnalyzer.state.collectAsStateWithLifecycle()
    val analysisExecutor = remember { Dispatchers.Default.asExecutor() }

    DisposableEffect(faceAnalyzer) {
        onDispose {
            faceAnalyzer.close()
        }
    }

    var authStep by remember { mutableStateOf(AuthStep.ALIGN) }
    var matchedUserName by remember { mutableStateOf("") }

    LaunchedEffect(authStep) {
        if (authStep == AuthStep.ALIGN || authStep == AuthStep.LIVENESS) {
            var conditionStartTime = 0L
            while (true) {
                val face = analyzerState.faces.firstOrNull()
                if (face != null) {
                    if (authStep == AuthStep.ALIGN) {
                        val rotY = face.headEulerAngleY
                        if (rotY in -12.0..12.0) {
                            if (conditionStartTime == 0L) conditionStartTime = System.currentTimeMillis()
                            else if (System.currentTimeMillis() - conditionStartTime > 1200) {
                                authStep = AuthStep.LIVENESS
                                break
                            }
                        } else {
                            conditionStartTime = 0L
                        }
                    } else if (authStep == AuthStep.LIVENESS) {
                        val rightEyeOpen = face.rightEyeOpenProbability ?: 1.0f
                        val leftEyeOpen = face.leftEyeOpenProbability ?: 1.0f
                        // Improved blink detection logic (more forgiving threshold)
                        if (rightEyeOpen < 0.35f && leftEyeOpen < 0.35f) {
                            authStep = AuthStep.VERIFYING
                            break
                        }
                    }
                } else {
                    conditionStartTime = 0L
                }
                delay(100)
            }
        } else if (authStep == AuthStep.VERIFYING) {
            delay(1500)
            val currentEmbedding = analyzerState.currentEmbedding
            val livenessResult = analyzerState.livenessResult
            
            if (currentEmbedding != null && livenessResult != null && livenessResult.isLive) {
                // Offload heavy computations to a background worker thread
                coroutineScope.launch(Dispatchers.Default) {
                    val db = AppDatabase.getDatabase(context)
                    val profiles = db.employeeDao().getAllProfiles()
                    
                    var bestMatch: com.example.data.EmployeeProfile? = null
                    var maxSimilarity = -1f

                    profiles.forEach { profile ->
                        val storedEmbedding = profile.faceEmbedding
                        val verificationResult = appContainer.faceRecognitionEngine.verifyIdentity(currentEmbedding, storedEmbedding)
                        
                        Log.d("ScanScreen", "Comparing with ${profile.fullName}: ${verificationResult.confidenceScore}")
                        
                        if (verificationResult.isValid && verificationResult.confidenceScore > maxSimilarity) {
                            maxSimilarity = verificationResult.confidenceScore
                            bestMatch = profile
                        }
                    }

                    // Return to the Main dispatcher to modify Compose state variables safely
                    kotlinx.coroutines.withContext(Dispatchers.Main) {
                        if (bestMatch != null) {
                            matchedUserName = bestMatch!!.fullName
                            authStep = AuthStep.SUCCESS
                        } else {
                            Log.d("ScanScreen", "No match found or below threshold (Best: $maxSimilarity)")
                            authStep = AuthStep.FAILED
                        }
                    }
                }
            } else {
                authStep = AuthStep.FAILED
            }
        } else if (authStep == AuthStep.SUCCESS) {
            delay(2000)
            val sharedPref = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
            sharedPref.edit().putBoolean("is_logged_in", true).apply()
            navController.navigate("home") {
                popUpTo("login") { inclusive = false }
            }
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
            lastProcessingTimeMs = analyzerState.lastProcessingTimeMs,
            authStep = authStep
        )
        
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
                Text("INTEGRITY CONFIDENCE", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text(if (face != null) "$qualityScore%" else "--%", fontSize = 16.sp, color = if(qualityScore > 85) Color.Green else Color.White, fontWeight = FontWeight.ExtraBold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(if (face != null) "BIOMETRIC LOCKED" else "SEEKING SUBJECT", fontSize = 10.sp, color = if(face != null) Color(0xFF1565C0) else Color.Red, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("LIVENESS", "EMBED", "MATCH").forEach { metric ->
                        val color = if (authStep == AuthStep.SUCCESS || (metric == "LIVENESS" && authStep == AuthStep.VERIFYING)) Color.Green else if (face != null) Color.Cyan else Color.Gray
                        Text(metric, fontSize = 8.sp, color = color, modifier = Modifier.border(1.dp, color, RoundedCornerShape(2.dp)).padding(horizontal = 4.dp, vertical = 2.dp))
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

                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (exc: Exception) {
                            Log.e("CameraScanUI", "Camera binding failed", exc)
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                onRelease = {
                    // CameraX automatically unbinds use cases when the lifecycle owner is destroyed.
                    // Doing it manually here can cause race conditions during navigation.
                },
                modifier = Modifier.fillMaxSize()
            )

            DynamicScanOverlay(hasFaces = analyzerState.faces.isNotEmpty(), authStep = authStep)

            // Scanning line animation
            if (authStep != AuthStep.SUCCESS && authStep != AuthStep.FAILED) {
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
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        DynamicScanControls(
            hasFaces = analyzerState.faces.isNotEmpty(),
            authStep = authStep,
            matchedUserName = matchedUserName,
            onRetry = { authStep = AuthStep.ALIGN },
            navController = navController
        )

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
                Icon(Icons.Default.Info, contentDescription = "Toggle Flash")
            }

            IconButton(
                onClick = { },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.7f), CircleShape)
                    .shadow(2.dp, CircleShape)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Flip Camera")
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
fun StatusRow(isProcessing: Boolean, lastProcessingTimeMs: Long, authStep: AuthStep) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Chip(text = "NPU Processing", isPulse = isProcessing, color = MaterialTheme.colorScheme.primary)
        Chip(text = "Offline", icon = Icons.Default.Warning, color = Color.Gray)
        if (lastProcessingTimeMs > 0) {
            Chip(text = "${lastProcessingTimeMs}ms", color = Color.Gray)
        }
    }
}

@Composable
fun DynamicScanOverlay(hasFaces: Boolean, authStep: AuthStep) {
    val strokeColor = when (authStep) {
        AuthStep.SUCCESS -> Color.Green
        AuthStep.FAILED -> Color.Red
        else -> if (hasFaces) Color.Cyan else MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(
                if (hasFaces || authStep == AuthStep.SUCCESS || authStep == AuthStep.FAILED) 4.dp else 0.dp,
                if (authStep == AuthStep.SUCCESS) Color.Green.copy(alpha = 0.5f)
                else if (authStep == AuthStep.FAILED) Color.Red.copy(alpha = 0.5f)
                else if (hasFaces) Color.Cyan.copy(alpha = 0.5f)
                else Color.Transparent,
                RoundedCornerShape(48.dp)
            )
            .padding(24.dp)
    ) {
        Box(modifier = Modifier.align(Alignment.TopStart).size(32.dp).border(4.dp, strokeColor, RoundedCornerShape(topStart = 16.dp)))
        Box(modifier = Modifier.align(Alignment.TopEnd).size(32.dp).border(4.dp, strokeColor, RoundedCornerShape(topEnd = 16.dp)))
        Box(modifier = Modifier.align(Alignment.BottomStart).size(32.dp).border(4.dp, strokeColor, RoundedCornerShape(bottomStart = 16.dp)))
        Box(modifier = Modifier.align(Alignment.BottomEnd).size(32.dp).border(4.dp, strokeColor, RoundedCornerShape(bottomEnd = 16.dp)))
        
        if (authStep == AuthStep.SUCCESS) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green, modifier = Modifier.align(Alignment.Center).size(64.dp))
        } else if (authStep == AuthStep.FAILED) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.align(Alignment.Center).size(64.dp))
        }
    }
}

@Composable
fun DynamicScanControls(hasFaces: Boolean, authStep: AuthStep, matchedUserName: String, onRetry: () -> Unit, navController: NavController) {
    val context = LocalContext.current
    val executor = ContextCompat.getMainExecutor(context)
    val buttonText = when (authStep) {
        AuthStep.ALIGN -> if (hasFaces) "FACE DETECTED" else "ALIGN FACE"
        AuthStep.LIVENESS -> "BLINK TO VERIFY"
        AuthStep.VERIFYING -> "EXTRACTING EMBEDDING..."
        AuthStep.SUCCESS -> "IDENTITY VERIFIED"
        AuthStep.FAILED -> "ACCESS DENIED"
    }
    
    val buttonColor = when (authStep) {
        AuthStep.SUCCESS -> Color.Green
        AuthStep.FAILED -> Color.Red
        AuthStep.VERIFYING -> Color(0xFFFFA500)
        else -> if (hasFaces) Color.Cyan else MaterialTheme.colorScheme.primary
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .background(buttonColor, RoundedCornerShape(12.dp))
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(
                text = buttonText,
                color = if (authStep == AuthStep.VERIFYING) Color.Black else Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (authStep == AuthStep.SUCCESS) {
            Text(
                text = "Welcome, $matchedUserName",
                color = Color.Green,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        } else if (authStep == AuthStep.FAILED) {
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("RETRY AUTHENTICATION")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val biometricManager = androidx.biometric.BiometricManager.from(context)
                    val canAuth = biometricManager.canAuthenticate(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG)
                    if (canAuth == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS) {
                        val fragmentActivity = context.getFragmentActivity()
                        if (fragmentActivity != null) {
                            val biometricPrompt = BiometricPrompt(fragmentActivity, executor,
                                object : BiometricPrompt.AuthenticationCallback() {
                                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                        super.onAuthenticationSucceeded(result)
                                        val sharedPref = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                                        sharedPref.edit().putBoolean("is_logged_in", true).apply()
                                        navController.navigate("home") {
                                            popUpTo("login") { inclusive = false }
                                        }
                                    }
                                })

                            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                                .setTitle("Biometric Authentication")
                                .setSubtitle("Use your fingerprint to authenticate")
                                .setNegativeButtonText("Cancel")
                                .build()
                                
                            biometricPrompt.authenticate(promptInfo)
                        }
                    } else {
                        navController.navigate("login")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("USE FINGERPRINT")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Secure terminal requires enrollment.",
                color = Color.Red,
                fontSize = 10.sp
            )
        } else {
            val progress = when(authStep) {
                AuthStep.ALIGN -> if (hasFaces) 0.5f else 0.2f
                AuthStep.LIVENESS -> 0.7f
                AuthStep.VERIFYING -> 0.9f
                else -> 1f
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = buttonColor,
                trackColor = Color.LightGray.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "NODE SECURITY ACTIVE",
                color = buttonColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

private fun calculateCosineSimilarity(f1: FloatArray, f2: FloatArray): Float {
    var dotProduct = 0.0f
    var normA = 0.0f
    var normB = 0.0f
    for (i in f1.indices) {
        dotProduct += f1[i] * f2[i]
        normA += f1[i] * f1[i]
        normB += f2[i] * f2[i]
    }
    val result = dotProduct / (kotlin.math.sqrt(normA.toDouble()) * kotlin.math.sqrt(normB.toDouble())).toFloat()
    return if (result.isNaN()) 0f else result
}
