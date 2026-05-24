package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.components.AppBottomNavigation
import com.example.ui.components.StatusBar
import kotlinx.coroutines.delay

@Composable
fun RecordsScreen(navController: NavController) {
    var syncing by remember { mutableStateOf(false) }
    var progress = 0.72f

    LaunchedEffect(syncing) {
        if (syncing) {
            delay(2000)
            syncing = false
        }
    }

    Scaffold(
        topBar = { StatusBar(showOffline = true) },
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
                Column {
                    Text("DATA TRANSMISSION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, letterSpacing = 2.sp)
                    Text("Sync Center", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Card(
                        modifier = Modifier.weight(1f).height(140.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = borderStroke()
                    ) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("QUEUED RECORDS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Icon(Icons.Default.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("1,248", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                                Text("+42 NEW", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Red, modifier = Modifier.padding(bottom = 6.dp))
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f).height(140.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = borderStroke()
                    ) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("SECURITY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                            }
                            Column {
                                Text("AES-256 AWS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("Tunnel: secure-node-04", fontSize = 9.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = borderStroke()
                ) {
                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                            Column {
                                Text("SYNC PROGRESS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Text("Batch 09/24 Transmitting", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                            Text("72%", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            trackColor = Color.LightGray.copy(alpha = 0.5f)
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                                Text("UPDATING DATA... 1.2MB/S", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            }
                            Text("ETA: 1M 14S", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { syncing = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = !syncing
                ) {
                    Icon(if (syncing) Icons.Default.Sync else Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text(if (syncing) "INITIALIZING LINK..." else "FORCE MANUAL SYNC", fontWeight = FontWeight.Bold)
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("SYNC HISTORY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 2.sp)

                    HistoryItem("Automatic Upload Success", "14:02 PM • 458 Records • 12.4 MB", Icons.Default.CheckCircle, MaterialTheme.colorScheme.primary)
                    HistoryItem("Manual Sync Triggered", "10:15 AM • 1,022 Records • 28.1 MB", Icons.Default.CheckCircle, MaterialTheme.colorScheme.primary)
                    HistoryItem("Connection Interrupted", "08:44 AM • Auth Timeout • Retry 5m", Icons.Default.Warning, MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun HistoryItem(title: String, meta: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = borderStroke()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color)
            }
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(meta, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            }
        }
    }
}
