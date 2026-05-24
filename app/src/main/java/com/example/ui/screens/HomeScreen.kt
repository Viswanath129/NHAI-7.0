package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.components.AppBottomNavigation
import com.example.ui.components.StatusBar
import com.example.ui.theme.SafetyOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        topBar = { StatusBar() },
        bottomBar = { AppBottomNavigation(navController) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Button(
                    onClick = { navController.navigate("scan") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = SafetyOrange, ambientColor = SafetyOrange),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Default.Face, contentDescription = "Scan", modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Start Authentication", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(text = "READY FOR SCAN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = borderStroke(),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("SUCCESS AUTHS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("1,284", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                            Text("+12 since sync", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = borderStroke(),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("PENDING SYNC", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Icon(Icons.Default.Sync, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("42", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.tertiary)
                            Text("Local storage 2%", fontSize = 10.sp, color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f))
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("OPERATIONAL TASKS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 2.sp)

                    TaskCard(
                        icon = Icons.Default.PersonAdd,
                        title = "Register Employee",
                        subtitle = "Add biometric profile",
                        iconBg = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                        iconTint = MaterialTheme.colorScheme.primary,
                        onClick = { }
                    )

                    TaskCard(
                        icon = Icons.Default.Storage,
                        title = "Offline Records",
                        subtitle = "View local transaction log",
                        iconBg = MaterialTheme.colorScheme.secondaryContainer,
                        iconTint = MaterialTheme.colorScheme.secondary,
                        onClick = { navController.navigate("records") }
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("SYSTEM LOGS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 2.sp)

                    LogItem("14:22", "Operator ID #4421 Approved", Icons.Default.CheckCircle, MaterialTheme.colorScheme.primary)
                    LogItem("14:18", "Operator ID #4409 Approved", Icons.Default.CheckCircle, MaterialTheme.colorScheme.primary)
                    LogItem("14:05", "Authentication Failed: No Profile", Icons.Default.Warning, MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun TaskCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    iconBg: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = borderStroke()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconBg, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint)
            }
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun LogItem(
    time: String,
    message: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = borderStroke()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(time, color = Color.Gray, modifier = Modifier.width(40.dp), style = MaterialTheme.typography.bodySmall)
            Text(message, fontWeight = FontWeight.Medium, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Icon(icon, contentDescription = null, tint = color)
        }
    }
}

@Composable
fun borderStroke() = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
