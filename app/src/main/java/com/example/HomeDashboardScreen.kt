package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.SensorsOff
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeDashboardScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9FF))
            .verticalScroll(rememberScrollState())
    ) {
        // Top Status Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // We use a custom box to mimic the NHAI logo
                Box(modifier = Modifier.height(32.dp).width(32.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)))
                Box(modifier = Modifier.height(24.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.5f)))
                Text("NHAI AUTH", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, letterSpacing = 0.5.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                        Text(
                            "SECURE NODE",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 0.5.sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Default.SensorsOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        
        Column(modifier = Modifier.padding(16.dp)) {
            // Hero Action
            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth().height(160.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6F00)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.Face, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Start Authentication", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("READY FOR SCAN", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Stats Grid
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f)),
                    shadowElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("SUCCESS AUTHS", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 0.5.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("1,284", style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        Text("+12 since sync", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f)),
                    shadowElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("PENDING SYNC", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 0.5.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("42", style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.tertiary)
                        Text("Local storage 2%", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("OPERATIONAL TASKS", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 2.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TaskItem(Icons.Default.PersonAdd, "Register Employee", "Add biometric profile", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f))
                TaskItem(Icons.Default.DataUsage, "Offline Records", "View local transaction log", MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondaryContainer)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Text("SYSTEM LOGS", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 2.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LogItem("14:22", "Operator ID #4421 Approved", MaterialTheme.colorScheme.primary, Icons.Default.Verified)
                LogItem("14:18", "Operator ID #4409 Approved", MaterialTheme.colorScheme.primary, Icons.Default.Verified)
                LogItem("14:05", "Authentication Failed: No Profile", MaterialTheme.colorScheme.error, Icons.Default.Error)
            }
        }
    }
}

@Composable
fun TaskItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, iconColor: Color, iconBgColor: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f)),
        shadowElevation = 0.dp
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier.size(48.dp).background(iconBgColor, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor)
            }
            Column {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun LogItem(time: String, text: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(modifier = Modifier.background(color = Color.White).border(width = 0.dp, color = Color.Transparent).padding(start = 4.dp).background(color = color).padding(start = 4.dp).background(Color.White).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(time, style = MaterialTheme.typography.labelMedium.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace), color = MaterialTheme.colorScheme.outline, modifier = Modifier.width(48.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }
    }
}
