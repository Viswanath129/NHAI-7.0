package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.NHAiAuthTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("is_logged_in", false)
        val startDest = if (isLoggedIn) "home" else "email_login"

        setContent {
            NHAiAuthTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val appContainer = (application as NHAIApplication).container
                    val enrollViewModel: EnrollViewModel = viewModel(
                        factory = EnrollViewModelFactory(appContainer.employeeRepository)
                    )
                    
                    NavHost(navController = navController, startDestination = startDest) {
                        composable("email_login") { EmailLoginScreen(navController) }
                        composable("signup") { SignUpScreen(navController) }
                        composable("reset_password") { ResetPasswordScreen(navController) }
                        composable("login") { LoginScreen(navController) }
                        composable("home") { HomeScreen(navController) }
                        composable("scan") { ScanScreen(navController) }
                        composable("enroll_details") { EnrollDetailsScreen(navController, enrollViewModel) }
                        composable("enroll_prep") { EnrollPrepScreen(navController, enrollViewModel) }
                        composable("enroll_camera") { EnrollmentCameraScreen(navController, enrollViewModel) }
                        composable("enroll_processing") { EnrollProcessingScreen(navController, enrollViewModel) }
                        composable("enroll_result/{success}") { backStackEntry ->
                            val success = backStackEntry.arguments?.getString("success")?.toBoolean() ?: false
                            EnrollResultScreen(navController, enrollViewModel, success)
                        }
                        composable("records") { RecordsScreen(navController) }
                    }
                }
            }
        }
    }
}
