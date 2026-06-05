package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.SensorsOff
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(0) } // 0: Home, 1: Scan, 2: Records, 3: Settings

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha=0.05f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().height(80.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavItem(Icons.Default.Home, "Home", selectedTab == 0) { selectedTab = 0 }
                    BottomNavItem(Icons.Default.Face, "Scan", selectedTab == 1) { selectedTab = 1 }
                    BottomNavItem(Icons.Outlined.Storage, "Records", selectedTab == 2) { selectedTab = 2 }
                    BottomNavItem(Icons.Default.Settings, "Settings", selectedTab == 3) { selectedTab = 3 }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (selectedTab) {
                0 -> HomeDashboardScreen()
                1 -> FaceScanScreen()
                2 -> SyncCenterScreen()
                3 -> SettingsScreen()
            }
        }
    }
}

@Composable
fun BottomNavItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .then(if (isSelected) Modifier.background(MaterialTheme.colorScheme.primary) else Modifier)
            .padding(horizontal = 24.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = if (isSelected) Color.White else Color.Gray)
            Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp), color = if (isSelected) Color.White else Color.Gray, fontWeight = FontWeight.Bold)
        }
    }
}


@Composable
fun SyncCenterScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .background(Color(0xFFF5F7FA))
    ) {
        // TopAppBar equivalent padding
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "DATA TRANSMISSION",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Sync Center",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Queued Records Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.5f))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("QUEUED RECORDS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold)
                    Icon(Icons.Outlined.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("1,248", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("+42 NEW", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Security Status Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.5f))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("SECURITY STATUS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold)
                    Icon(Icons.Outlined.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("AES-256 AWS ENCRYPTED", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Tunnel: nhai-auth-secure-node-04", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Sync Progress Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.5f))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Column {
                        Text("SYNC PROGRESS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        Text("Transmitting batch 09/24", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                    Text("72%", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { 0.72f },
                    modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("UPLOADING 1.2MB/S", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("ETA: 1m 14s", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("FORCE MANUAL SYNC", style = MaterialTheme.typography.labelLarge, color = Color.White)
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Text("SYNC HISTORY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        HistoryItem(
            title = "AUTOMATIC UPLOAD SUCCESS",
            subtitle = "14:02 PM • 458 Records • 12.4 MB",
            icon = Icons.Outlined.CheckCircle,
            iconTint = MaterialTheme.colorScheme.primary,
            iconBg = MaterialTheme.colorScheme.surfaceContainerHighest
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        HistoryItem(
            title = "MANUAL SYNC TRIGGERED",
            subtitle = "10:15 AM • 1,022 Records • 28.1 MB",
            icon = Icons.Outlined.CheckCircle,
            iconTint = MaterialTheme.colorScheme.primary,
            iconBg = MaterialTheme.colorScheme.surfaceContainerHighest
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        HistoryItem(
            title = "CONNECTION INTERRUPTED",
            subtitle = "08:44 AM • Auth Timeout • Retry in 5m",
            icon = Icons.Outlined.Warning,
            iconTint = MaterialTheme.colorScheme.onErrorContainer,
            iconBg = MaterialTheme.colorScheme.errorContainer,
            titleColor = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun HistoryItem(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconTint: Color, iconBg: Color, titleColor: Color = MaterialTheme.colorScheme.onSurface) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.5f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(iconBg, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelSmall, color = titleColor, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}
