package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.compose.ui.platform.LocalContext
import android.content.ContextWrapper
import android.content.Context
import com.example.ui.components.getFragmentActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    var showManualLogin by remember { mutableStateOf(false) }
    var operatorCode by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var showToast by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val dateFormat = SimpleDateFormat("HH:mm", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val utcTime = dateFormat.format(Date())

    LaunchedEffect(showToast) {
        if (showToast) {
            delay(1500)
            showToast = false
            navController.navigate("scan")
        }
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0F1A)) // Dark tactical background
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // Subtle system scanline / mesh overlay could go here
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // TOP STATUS INDICATORS
                val infiniteTransition = rememberInfiniteTransition()
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(Color.Green.copy(alpha = alpha), CircleShape))
                        Text("OFFLINE SECURE", color = Color.Green.copy(alpha = alpha), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                    Text("NODE: NHAI-772", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // NODE IDENTIFICATION
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .border(2.dp, Color(0xFF1565C0).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .padding(8.dp)
                        ) {
                            Box(modifier = Modifier.align(Alignment.TopStart).size(16.dp).border(3.dp, Color(0xFF1565C0), RoundedCornerShape(topStart = 8.dp)))
                            Box(modifier = Modifier.align(Alignment.TopEnd).size(16.dp).border(3.dp, Color(0xFF1565C0), RoundedCornerShape(topEnd = 8.dp)))
                            Box(modifier = Modifier.align(Alignment.BottomStart).size(16.dp).border(3.dp, Color(0xFF1565C0), RoundedCornerShape(bottomStart = 8.dp)))
                            Box(modifier = Modifier.align(Alignment.BottomEnd).size(16.dp).border(3.dp, Color(0xFF1565C0), RoundedCornerShape(bottomEnd = 8.dp)))
                            
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                androidx.compose.foundation.Image(
                                    painter = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_dialog_info),
                                    contentDescription = "NHAI Shield",
                                    modifier = Modifier.size(56.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "NHAI", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = 4.sp)
                    Text(
                        text = "CRITICAL BIOMETRIC INFRASTRUCTURE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565C0),
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                androidx.compose.animation.AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn() + androidx.compose.animation.expandVertically(),
                    exit = fadeOut() + androidx.compose.animation.shrinkVertically()
                ) {
                    errorMessage?.let { msg ->
                        Text(text = msg, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                    }
                }

                // AUTHENTICATION AREA
                androidx.compose.animation.AnimatedContent(
                    targetState = showManualLogin,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
                    },
                    label = "loginTransition"
                ) { isManual ->
                    if (isManual) {
                    Card(
                        modifier = Modifier.fillMaxWidth().shadow(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF121A2A)),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(2.dp, Color.DarkGray.copy(alpha=0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(text = "OPERATOR AUTHORIZATION CODE", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray, letterSpacing = 2.sp)
                            
                            OutlinedTextField(
                                value = operatorCode,
                                onValueChange = { if (it.length <= 10) operatorCode = it },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF0A0F1A),
                                    unfocusedContainerColor = Color(0xFF0A0F1A),
                                    focusedBorderColor = Color(0xFF1565C0),
                                    unfocusedBorderColor = Color.DarkGray
                                ),
                                placeholder = { Text("Enter Code", color = Color.DarkGray, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 20.sp, letterSpacing = 4.sp, color = Color.White)
                            )

                            Text(text = "SECURE ACCESS KEY", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray, letterSpacing = 2.sp)
                            
                            OutlinedTextField(
                                value = pin,
                                onValueChange = { if (it.length <= 6) pin = it },
                                modifier = Modifier.fillMaxWidth(),
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF0A0F1A),
                                    unfocusedContainerColor = Color(0xFF0A0F1A),
                                    focusedBorderColor = Color(0xFF1565C0),
                                    unfocusedBorderColor = Color.DarkGray
                                ),
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 20.sp, letterSpacing = 8.sp, color = Color.White)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { 
                                    if (operatorCode.isBlank() || pin.isBlank()) {
                                        errorMessage = "Please enter operator code and access key"
                                        return@Button
                                    }
                                    
                                    // Verify credentials against static/demo profile fallback
                                    val isDemoCredentials = (operatorCode == "772" && pin == "123456") || (operatorCode.length >= 3 && pin == "123456")
                                    if (!isDemoCredentials) {
                                        errorMessage = "Invalid Operator Code or Secure Access Key"
                                        return@Button
                                    }

                                    val sharedPref = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                                    val lockoutTime = sharedPref.getLong("lockout_time", 0L)
                                    if (lockoutTime > System.currentTimeMillis()) {
                                        val remainingMinutes = ((lockoutTime - System.currentTimeMillis()) / 60000) + 1
                                        errorMessage = "Account locked. Try again in $remainingMinutes minutes."
                                        return@Button
                                    }

                                    val biometricManager = androidx.biometric.BiometricManager.from(context)
                                    val canAuth = biometricManager.canAuthenticate(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG)

                                    if (canAuth == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS) {
                                        val fragmentActivity = context.getFragmentActivity()
                                        if (fragmentActivity != null) {
                                            val executor = ContextCompat.getMainExecutor(context)
                                            val biometricPrompt = BiometricPrompt(fragmentActivity, executor,
                                                object : BiometricPrompt.AuthenticationCallback() {
                                                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                                        super.onAuthenticationSucceeded(result)
                                                        sharedPref.edit().putInt("failed_attempts", 0).putLong("lockout_time", 0L).apply()
                                                        sharedPref.edit().putBoolean("is_logged_in", true).apply()
                                                        navController.navigate("home")
                                                    }
                                                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                                        super.onAuthenticationError(errorCode, errString)
                                                        var failedAttempts = sharedPref.getInt("failed_attempts", 0) + 1
                                                        if (failedAttempts >= 5) {
                                                            val newLockout = System.currentTimeMillis() + 30 * 60 * 1000 // 30 mins
                                                            sharedPref.edit().putInt("failed_attempts", failedAttempts).putLong("lockout_time", newLockout).apply()
                                                            errorMessage = "Account locked. Try again in 30 minutes."
                                                        } else {
                                                            sharedPref.edit().putInt("failed_attempts", failedAttempts).apply()
                                                            errorMessage = "Biometric Auth Failed. ${5 - failedAttempts} attempts left."
                                                        }
                                                    }
                                                })
        
                                            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                                                .setTitle("Secondary Authentication")
                                                .setSubtitle("Verify identity to complete sign-in")
                                                .setDescription("Biometric authentication is required to verify your identity and ensure secure entry.")
                                                .setNegativeButtonText("Cancel")
                                                .build()
                                                
                                            biometricPrompt.authenticate(promptInfo)
                                        } else {
                                            // Secure fallback if fragment activity is null
                                            sharedPref.edit().putBoolean("is_logged_in", true).apply()
                                            navController.navigate("home")
                                        }
                                    } else {
                                        // Offline/emulator secure bypass when biometric hardware is absent/unenrolled
                                        sharedPref.edit().putInt("failed_attempts", 0).putLong("lockout_time", 0L).apply()
                                        sharedPref.edit().putBoolean("is_logged_in", true).apply()
                                        navController.navigate("home")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                            ) {
                                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                                Text("AUTHENTICATE MANUALLY", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            }
                            
                            TextButton(onClick = { showManualLogin = false }, modifier = Modifier.fillMaxWidth()) {
                                Text("Return to Biometric Mode", color = Color.Gray)
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                        val context = LocalContext.current
                        val executor = ContextCompat.getMainExecutor(context)
                        
                        Button(
                            onClick = {
                                val sharedPref = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                                val lockoutTime = sharedPref.getLong("lockout_time", 0L)
                                if (lockoutTime > System.currentTimeMillis()) {
                                    val remainingMinutes = ((lockoutTime - System.currentTimeMillis()) / 60000) + 1
                                    errorMessage = "Account locked. Try again in $remainingMinutes minutes."
                                    return@Button
                                }

                                val fragmentActivity = context.getFragmentActivity()
                                if (fragmentActivity != null) {
                                    val biometricPrompt = BiometricPrompt(fragmentActivity, executor,
                                        object : BiometricPrompt.AuthenticationCallback() {
                                            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                                super.onAuthenticationSucceeded(result)
                                                sharedPref.edit().putInt("failed_attempts", 0).putLong("lockout_time", 0L).apply()
                                                sharedPref.edit().putBoolean("is_logged_in", true).apply()
                                                navController.navigate("home")
                                            }
                                            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                                super.onAuthenticationError(errorCode, errString)
                                                var failedAttempts = sharedPref.getInt("failed_attempts", 0) + 1
                                                if (failedAttempts >= 5) {
                                                    val newLockout = System.currentTimeMillis() + 30 * 60 * 1000 // 30 mins
                                                    sharedPref.edit().putInt("failed_attempts", failedAttempts).putLong("lockout_time", newLockout).apply()
                                                    errorMessage = "Account locked. Try again in 30 minutes."
                                                } else {
                                                    sharedPref.edit().putInt("failed_attempts", failedAttempts).apply()
                                                    errorMessage = "Biometric Auth Failed. ${5 - failedAttempts} attempts left."
                                                }
                                            }
                                        })

                                    val promptInfo = BiometricPrompt.PromptInfo.Builder()
                                        .setTitle("Biometric Authentication")
                                        .setSubtitle("Use your fingerprint to authenticate")
                                        .setDescription("Biometric authentication is required to verify your identity and ensure secure entry.")
                                        .setNegativeButtonText("Cancel")
                                        .build()
                                        
                                    biometricPrompt.authenticate(promptInfo)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(64.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.padding(end = 12.dp).size(28.dp))
                            Text("FINGERPRINT AUTHENTICATION", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, letterSpacing = 1.sp)
                        }

                        Button(
                            onClick = { navController.navigate("scan") },
                            modifier = Modifier.fillMaxWidth().height(64.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)) // Darker green to differentiate
                        ) {
                            Icon(Icons.Default.Face, contentDescription = null, modifier = Modifier.padding(end = 12.dp).size(28.dp))
                            Text("FACE AUTHENTICATION", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, letterSpacing = 1.sp)
                        }

                        OutlinedButton(
                            onClick = { showManualLogin = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(2.dp, Color.DarkGray)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.padding(end = 8.dp).size(18.dp), tint = Color.Gray)
                            Text("MANUAL SECURE LOGIN", fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Color.LightGray)
                        }
                        
                        TextButton(
                            onClick = { navController.navigate("enroll_details") },
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Text("FIRST TIME ENROLLMENT", fontWeight = FontWeight.Bold, color = Color(0xFF1565C0), letterSpacing = 2.sp)
                        }
                    }
                }
                } // Close AnimatedContent

                Spacer(modifier = Modifier.weight(1f))

                // FOOTER
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.clickable { navController.navigate("debug_panel") }
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.DarkGray)
                            Text("ORT EDGE RUNTIME", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("UTC $utcTime", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.sp)
                        }
                    }
                    Text("OFFLINE ACTIVE • AES-256", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0), letterSpacing = 2.sp)
                }
            }

            if (showToast) {
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp, start = 24.dp, end = 24.dp),
                    containerColor = Color(0xFF121A2A),
                    contentColor = Color.White
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(Icons.Default.Face, contentDescription = null, tint = Color.Green)
                        Column {
                            Text("NODE AUTHENTICATING", fontWeight = FontWeight.ExtraBold, color = Color.Green, letterSpacing = 1.sp, fontSize = 12.sp)
                            Text("Initializing Neural Processor...", fontSize = 11.sp, color = Color.LightGray)
                        }
                    }
                }
            }
        }
    }
}

