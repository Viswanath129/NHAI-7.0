package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockPerson
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9FF))
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar
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
                Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("NHAI Auth", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, letterSpacing = 0.5.sp)
            }
            IconButton(onClick = { }, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow, CircleShape)) {
                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Column(modifier = Modifier.padding(24.dp)) {
            // Profile Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.3f)),
                shadowElevation = 1.dp
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Box(modifier = Modifier.size(80.dp).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha=0.2f), RoundedCornerShape(16.dp)).border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha=0.2f), RoundedCornerShape(16.dp)), contentAlignment=Alignment.Center) {
                        Icon(Icons.Default.AccountCircle, contentDescription=null, modifier=Modifier.fillMaxSize(), tint = MaterialTheme.colorScheme.primary.copy(alpha=0.5f))
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Priya Sharma", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                            Surface(color = Color(0xFFFEF3C7), shape = RoundedCornerShape(12.dp)) {
                                Text("Certified Pro", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Color(0xFF92400E), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Operator ID: NHAI-8829-X", style = MaterialTheme.typography.labelSmall.copy(fontSize=12.sp), color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.VerifiedUser, contentDescription=null, tint=MaterialTheme.colorScheme.primary, modifier=Modifier.size(16.dp))
                            Text("SECURE TERMINAL ACTIVE", style=MaterialTheme.typography.labelSmall.copy(fontSize=10.sp), color=MaterialTheme.colorScheme.primary, fontWeight=FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("APPLICATION SETTINGS", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp, fontSize = 10.sp), color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(16.dp))

            // Settings Grid (Assuming we have columns in compose equivalents)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SettingsCard(Icons.Default.LockPerson, "Security & Access") {
                        SettingsRow("Biometric PIN", "6-digit backup enabled", true, customIcon = Icons.Default.Fingerprint)
                        SettingsRow("Auto-Lock Duration", "5 minutes", false)
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SettingsCard(Icons.Default.Psychology, "AI Recognition") {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Threshold", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(4.dp)) {
                                    Text("94%", style=MaterialTheme.typography.labelSmall.copy(fontSize=10.sp), color=Color.White, modifier=Modifier.padding(horizontal=8.dp, vertical=2.dp))
                                }
                            }
                            Spacer(modifier=Modifier.height(8.dp))
                            Slider(value=0.94f, onValueChange={}, modifier=Modifier.height(24.dp))
                        }
                        SettingsRow("Liveness Detection", "", true) // Toggle
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal=32.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("TERMINATE SESSION", style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Encryption Layer: AES-256-GCM Active", style = MaterialTheme.typography.labelSmall.copy(fontSize=10.sp), color = MaterialTheme.colorScheme.outline, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.3f)),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(24.dp))
            content()
        }
    }
}

@Composable
fun SettingsRow(title: String, subtitle: String, hasToggle: Boolean, checked: Boolean = true, customIcon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            if (subtitle.isNotEmpty()) {
                Text(subtitle, style = MaterialTheme.typography.labelSmall.copy(fontSize=10.sp), color = MaterialTheme.colorScheme.outline)
            }
        }
        if (customIcon != null) {
            Icon(customIcon, contentDescription=null, tint=MaterialTheme.colorScheme.primary)
        } else if (hasToggle) {
            Switch(checked = checked, onCheckedChange = { }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primary), modifier=Modifier.scale(0.8f))
        } else {
            Surface(color=MaterialTheme.colorScheme.surfaceContainerLow, shape=RoundedCornerShape(8.dp)) {
                Text("5 Minutes", style=MaterialTheme.typography.labelSmall.copy(fontSize=12.sp), color=MaterialTheme.colorScheme.primary, fontWeight=FontWeight.Bold, modifier=Modifier.padding(horizontal=12.dp, vertical=6.dp))
            }
        }
    }
}
