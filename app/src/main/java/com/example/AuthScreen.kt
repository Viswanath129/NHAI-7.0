package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.SensorsOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AuthScreen(onAuthorize: () -> Unit) {
    Scaffold(
        containerColor = Color(0xFFF9F9FF),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.9f))
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "NHAI AUTH",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
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
                        Icon(
                            imageVector = Icons.Default.SensorsOff,
                            contentDescription = "Offline",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .widthIn(max = 400.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Surface(
                            modifier = Modifier.size(128.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(4.dp).background(MaterialTheme.colorScheme.surface, CircleShape)) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(56.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    Brush.linearGradient(listOf(Color(0xFF004D99), Color(0xFF1565C0))),
                                    CircleShape
                                )
                                .border(4.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = "Operator Access", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "DEVICE ID",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 1.sp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text("NHAI-772-NODE-X", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "SECURITY PIN",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Forgot?",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(6) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(12.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.5f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("•", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = onAuthorize,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.linearGradient(listOf(Color(0xFF004D99), Color(0xFF1565C0)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Login, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("AUTHORIZE ACCESS", style = MaterialTheme.typography.labelLarge, color = Color.White)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.surfaceContainerHighest)
                    ) {
                        Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("BIOMETRIC LOGIN", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Terminal, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.6f), modifier = Modifier.size(16.dp))
                            Text("V4.2.1 SECURE KERNEL", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.6f))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.6f), modifier = Modifier.size(16.dp))
                            Text("05:38 UTC", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.6f))
                        }
                    }
                }
            }
        }
    }
}
