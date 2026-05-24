package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.RecordsScreen
import com.example.ui.screens.ScanScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.EnrollmentDetailsScreen
import com.example.ui.screens.EnrollmentCameraScreen
import com.example.ui.screens.EnrollmentMetricsScreen
import com.example.ui.screens.DebugPanelScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("scan") { ScanScreen(navController) }
        composable("records") { RecordsScreen(navController) }
        composable("settings") { SettingsScreen(navController) }
        composable("enroll_details") { EnrollmentDetailsScreen(navController) }
        composable("enroll_camera") { EnrollmentCameraScreen(navController) }
        composable("enroll_metrics") { EnrollmentMetricsScreen(navController) }
        composable("debug_panel") { DebugPanelScreen(navController) }
    }
}
