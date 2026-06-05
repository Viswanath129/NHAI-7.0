package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FaceScanScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().background(Color(0xFFF9F9FF))) {
        // Gradient backdrop
        Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha=0.6f)))
        
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.9f))
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .statusBarsPadding()
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.height(32.dp).width(32.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)))
                Box(modifier = Modifier.height(24.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.5f)))
                Text("NHAI AUTH", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, letterSpacing = 0.5.sp)
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(top = 96.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.3f))
                ) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                        Text("NPU Processing", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.primary)
                    }
                }
                Surface(
                    color = Color.White.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.3f))
                ) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.CloudOff, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Offline", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Face Frame
            Box(
                modifier = Modifier
                    .width(280.dp)
                    .aspectRatio(3f/4f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(4.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(48.dp))
                )
                
                // Corners
                Box(modifier = Modifier.size(32.dp).align(Alignment.TopStart).offset(32.dp, 32.dp).border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(topStart = 8.dp)))
                Box(modifier = Modifier.size(32.dp).align(Alignment.TopEnd).offset((-32).dp, 32.dp).border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(topEnd = 8.dp)))
                Box(modifier = Modifier.size(32.dp).align(Alignment.BottomStart).offset(32.dp, (-32).dp).border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(bottomStart = 8.dp)))
                Box(modifier = Modifier.size(32.dp).align(Alignment.BottomEnd).offset((-32).dp, (-32).dp).border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(bottomEnd = 8.dp)))
                
                // Instructions
                Box(
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).offset(y = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(12.dp), shadowElevation = 4.dp) {
                            Text("ALIGN FACE", style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, letterSpacing = 2.sp), fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(3.dp))) {
                    Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(0.34f).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(3.dp)))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("ANALYZING BIOMETRICS... 34%", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 1.sp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    IconButton(
                        onClick = { },
                        modifier = Modifier.size(48.dp).background(Color.White.copy(alpha = 0.7f), CircleShape).border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.3f), CircleShape)
                    ) {
                        Icon(Icons.Default.FlashlightOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(
                        onClick = { },
                        modifier = Modifier.size(48.dp).background(Color.White.copy(alpha = 0.7f), CircleShape).border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.3f), CircleShape)
                    ) {
                        Icon(Icons.Default.Cameraswitch, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}
