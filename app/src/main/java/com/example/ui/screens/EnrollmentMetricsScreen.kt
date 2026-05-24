package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.components.StatusBar

@Composable
fun EnrollmentMetricsScreen(navController: NavController) {
    Scaffold(
        topBar = { StatusBar(title = "PERFORMANCE METRICS") }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.Green.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Green, modifier = Modifier.size(32.dp))
            }
            Text("ENROLLMENT SUCCESSFUL", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Green)
            Text("Vector embeddings stored securely offline.", fontSize = 12.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))

            MetricsCard()

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { navController.navigate("home") { popUpTo("login") { inclusive = false } } },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("DASHBOARD", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MetricsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.List, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("PIPELINE LATENCY", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            HorizontalDivider()
            MetricRow("Face Detection Latency", "12ms", Color.Green)
            MetricRow("Recognition Latency", "48ms", Color.Green)
            MetricRow("Liveness Verification", "34ms", Color.Green)
            MetricRow("Encryption AES-256", "2ms", Color.Green)
            MetricRow("Local DB Write", "8ms", MaterialTheme.colorScheme.primary)
            HorizontalDivider()
            MetricRow("Total Pipeline", "104ms", MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String, valueColor: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}
