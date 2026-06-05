package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.AppDatabase
import com.example.data.User
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.ui.components.getFragmentActivity
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailLoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Welcome Back",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Sign in to continue", color = Color.Gray)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    TextButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(if (passwordVisible) "Hide" else "Show")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )
            
            androidx.compose.animation.AnimatedVisibility(
                visible = errorMessage != null,
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage ?: "", color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            TextButton(
                onClick = { navController.navigate("reset_password") },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Forgot Password?")
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Please enter email and password"
                        return@Button
                    }
                    val sharedPref = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                    val lockoutTime = sharedPref.getLong("lockout_time", 0L)
                    if (lockoutTime > System.currentTimeMillis()) {
                        val remainingMinutes = ((lockoutTime - System.currentTimeMillis()) / 60000) + 1
                        errorMessage = "Account locked. Try again in $remainingMinutes minutes."
                        return@Button
                    }

                    isLoading = true
                    coroutineScope.launch {
                        delay(600) // UI feedback
                        val db = AppDatabase.getDatabase(context)
                        val user = db.userDao().getUserByEmail(email.trim())
                        if (user != null && user.passwordHash == password) {
                            sharedPref.edit().putInt("failed_attempts", 0).putLong("lockout_time", 0L).apply()
                            
                            val biometricManager = androidx.biometric.BiometricManager.from(context)
                            val canAuth = biometricManager.canAuthenticate(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG)
                            val fragmentActivity = context.getFragmentActivity()
                            
                            if (canAuth == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS && fragmentActivity != null) {
                                val executor = ContextCompat.getMainExecutor(context)
                                val biometricPrompt = BiometricPrompt(fragmentActivity, executor,
                                    object : BiometricPrompt.AuthenticationCallback() {
                                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                            super.onAuthenticationSucceeded(result)
                                            val sharedPref = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                                            sharedPref.edit().apply {
                                                putBoolean("is_logged_in", true)
                                                putString("user_email", email.trim())
                                                apply()
                                            }
                                            navController.navigate("home") {
                                                popUpTo("email_login") { inclusive = true }
                                            }
                                        }
                                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                            super.onAuthenticationError(errorCode, errString)
                                            errorMessage = "Biometric Auth Failed: $errString"
                                        }
                                    })
                                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                                    .setTitle("Secondary Authentication")
                                    .setSubtitle("Verify identity to complete login")
                                    .setDescription("Biometric authentication is required to verify your identity and ensure secure entry.")
                                    .setNegativeButtonText("Cancel")
                                    .build()
                                biometricPrompt.authenticate(promptInfo)
                            } else {
                                // Fallback if no biometric hardware/enrolled fingerprints, or fragment activity is null
                                val sharedPref = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                                sharedPref.edit().apply {
                                    putBoolean("is_logged_in", true)
                                    putString("user_email", email.trim())
                                    apply()
                                }
                                navController.navigate("home") {
                                    popUpTo("email_login") { inclusive = true }
                                }
                            }
                        } else {
                            var failedAttempts = sharedPref.getInt("failed_attempts", 0) + 1
                            if (failedAttempts >= 5) {
                                val newLockout = System.currentTimeMillis() + 30 * 60 * 1000 // 30 mins
                                sharedPref.edit().putInt("failed_attempts", failedAttempts).putLong("lockout_time", newLockout).apply()
                                errorMessage = "Account locked. Try again in 30 minutes."
                            } else {
                                sharedPref.edit().putInt("failed_attempts", failedAttempts).apply()
                                errorMessage = "Invalid credentials. ${5 - failedAttempts} attempts left."
                            }
                        }
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("LOGIN", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = { navController.navigate("signup") }
            ) {
                Text("Don't have an account? Sign up")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            TextButton(
                onClick = { navController.navigate("login") }
            ) {
                Text("Use Bio-tactical System")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavController) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Create Account",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Sign up to get started", color = Color.Gray)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it; errorMessage = null },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    TextButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(if (passwordVisible) "Hide" else "Show")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; errorMessage = null },
                label = { Text("Confirm Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )
            
            androidx.compose.animation.AnimatedVisibility(
                visible = errorMessage != null,
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage ?: "", color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank() || fullName.isBlank()) {
                        errorMessage = "Please fill in all fields"
                        return@Button
                    }
                    if (password != confirmPassword) {
                        errorMessage = "Passwords do not match"
                        return@Button
                    }
                    if (password.length < 6) {
                        errorMessage = "Password must be at least 6 characters"
                        return@Button
                    }
                    isLoading = true
                    coroutineScope.launch {
                        delay(600) // UI feedback
                        val db = AppDatabase.getDatabase(context)
                        val existingUser = db.userDao().getUserByEmail(email.trim())
                        if (existingUser != null) {
                            errorMessage = "Email is already registered"
                        } else {
                            db.userDao().insert(User(email.trim(), password, fullName))
                            val sharedPref = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                            sharedPref.edit().apply {
                                putBoolean("is_logged_in", true)
                                putString("user_email", email.trim())
                                apply()
                            }
                            navController.navigate("home") {
                                popUpTo("signup") { inclusive = true }
                                popUpTo("email_login") { inclusive = true }
                            }
                        }
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("SIGN UP", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = { navController.popBackStack() }
            ) {
                Text("Already have an account? Log in")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Reset Password",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Enter your email and new password", color = Color.Gray)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null; successMessage = null },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it; errorMessage = null; successMessage = null },
                label = { Text("New Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; errorMessage = null; successMessage = null },
                label = { Text("Confirm New Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )
            
            androidx.compose.animation.AnimatedVisibility(
                visible = errorMessage != null,
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage ?: "", color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                }
            }
            androidx.compose.animation.AnimatedVisibility(
                visible = successMessage != null,
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(successMessage ?: "", color = Color(0xFF4CAF50), fontSize = 14.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    if (email.isBlank() || newPassword.isBlank()) {
                        errorMessage = "Please fill in all fields"
                        return@Button
                    }
                    if (newPassword != confirmPassword) {
                        errorMessage = "Passwords do not match"
                        return@Button
                    }
                    if (newPassword.length < 6) {
                        errorMessage = "Password must be at least 6 characters"
                        return@Button
                    }
                    coroutineScope.launch {
                        val db = AppDatabase.getDatabase(context)
                        val user = db.userDao().getUserByEmail(email.trim())
                        if (user != null) {
                            db.userDao().updatePassword(email.trim(), newPassword)
                            errorMessage = null
                            successMessage = "Password reset successfully!"
                            kotlinx.coroutines.delay(1500)
                            navController.popBackStack()
                        } else {
                            errorMessage = "No account found with this email"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("RESET PASSWORD", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = { navController.popBackStack() }
            ) {
                Text("Back to Login")
            }
        }
    }
}
