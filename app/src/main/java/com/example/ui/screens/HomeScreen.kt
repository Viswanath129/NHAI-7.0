package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.data.AppDatabase
import com.example.ui.components.AppLogo
import com.example.ui.theme.successGreen

import androidx.compose.material.icons.filled.ExitToApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val employees by db.employeeDao().getAllEmployees().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("NHAI AUTH DASHBOARD") },
                navigationIcon = {
                    AppLogo(size = 32.dp, modifier = Modifier.padding(start = 16.dp))
                },
                actions = {
                    IconButton(onClick = { 
                        val sharedPref = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                        sharedPref.edit().clear().apply()
                        navController.navigate("email_login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stats Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("SYSTEM STATUS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("${employees.size}", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                            Text("Enrolled Agents", style = MaterialTheme.typography.bodySmall)
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.successGreen.copy(alpha = 0.2f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                "ACTIVE",
                                color = MaterialTheme.colorScheme.successGreen,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("QUICK ACTIONS", style = MaterialTheme.typography.labelLarge, modifier = Modifier.align(Alignment.Start))

            HomeButton(
                title = "VERIFY IDENTITY",
                icon = Icons.Default.Search,
                onClick = { navController.navigate("scan") }
            )
            HomeButton(
                title = "ENROLL NEW AGENT",
                icon = Icons.Default.Add,
                onClick = { navController.navigate("enroll_details") }
            )
            HomeButton(
                title = "ACCESS RECORDS",
                icon = Icons.Default.List,
                onClick = { navController.navigate("records") }
            )
        }
    }
}

@Composable
fun HomeButton(title: String, icon: ImageVector, onClick: () -> Unit) {
    ElevatedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(80.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
    }
}
