package com.example.zeus.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zeus.model.ZabPackage
import com.example.zeus.model.ZeusProject

@Composable
fun ZeusBleBridgeScreen(
    project: ZeusProject,
    isScanning: Boolean,
    isConnected: Boolean,
    connectedDeviceName: String?,
    connectedDeviceMac: String?,
    isDeploying: Boolean,
    deployProgress: Float,
    generatedPackage: ZabPackage?,
    onScanClick: () -> Unit,
    onConnectClick: (String, String) -> Unit,
    onDisconnectClick: () -> Unit,
    onDeployClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B1120))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // BLE Bridge Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(
                                    if (isConnected) Color(0xFF0284C7) else Color(0xFF334155),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isConnected) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                                contentDescription = "Bluetooth Status",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Zeus BLE Wireless Bridge",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isConnected) "Connected to $connectedDeviceName" else "No smartwatch connected",
                                color = if (isConnected) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (isConnected) {
                        OutlinedButton(
                            onClick = onDisconnectClick,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Disconnect", fontSize = 11.sp)
                        }
                    } else {
                        Button(
                            onClick = onScanClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isScanning,
                            modifier = Modifier.testTag("ble_scan_button")
                        ) {
                            if (isScanning) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Scanning...", fontSize = 12.sp)
                            } else {
                                Icon(imageVector = Icons.Default.BluetoothSearching, contentDescription = "Scan", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Scan Bip Max", fontSize = 12.sp)
                            }
                        }
                    }
                }

                if (isConnected) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("MAC Address", color = Color(0xFF64748B), fontSize = 10.sp)
                            Text(connectedDeviceMac ?: "D4:22:CD:88:F1:04", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text("Signal (RSSI)", color = Color(0xFF64748B), fontSize = 10.sp)
                            Text("-54 dBm (Excellent)", color = Color(0xFF22C55E), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Target Framebuffer", color = Color(0xFF64748B), fontSize = 10.sp)
                            Text("320x380 px", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 1-Click Flash / Deploy Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Wireless OTA Flash & Deploy",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Pushes compiled .zab package to Amazfit Bip Max over GATT channel.",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                if (generatedPackage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📦", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = generatedPackage.packageName,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Size: ${generatedPackage.fileSizeKb} KB • CRC: ${generatedPackage.checksumCrc32}",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                AnimatedVisibility(visible = isDeploying) {
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Flashing over BLE...", color = Color(0xFF38BDF8), fontSize = 11.sp)
                            Text("${(deployProgress * 100).toInt()}%", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { deployProgress },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = Color(0xFF38BDF8),
                            trackColor = Color(0xFF334155)
                        )
                    }
                }

                Button(
                    onClick = onDeployClick,
                    enabled = !isDeploying,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("flash_bip_max_button")
                ) {
                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = "Deploy")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isConnected) "Flash to Amazfit Bip Max (zeus bridge --install)" else "Connect & Flash to Bip Max",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Discovered Devices List
        Text(
            text = "Discovered Zepp OS Devices",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val devices = listOf(
                Triple("Amazfit Bip Max", "D4:22:CD:88:F1:04", "-52 dBm"),
                Triple("Amazfit Bip 5", "8C:DE:52:11:AB:29", "-74 dBm"),
                Triple("Amazfit Active", "F2:10:99:34:E8:11", "-81 dBm")
            )

            items(devices) { (name, mac, rssi) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161E2E)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Watch,
                                contentDescription = "Watch",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("$mac • $rssi", color = Color(0xFF94A3B8), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        }

                        Button(
                            onClick = { onConnectClick(name, mac) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isConnected && connectedDeviceMac == mac) Color(0xFF10B981) else Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (isConnected && connectedDeviceMac == mac) "Active" else "Pair & Link",
                                fontSize = 11.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
