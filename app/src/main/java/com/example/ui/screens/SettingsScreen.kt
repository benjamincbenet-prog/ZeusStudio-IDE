package com.example.ui.screens

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BipDevice
import com.example.data.model.WatchSettings
import com.example.ui.theme.CyanPrimaryDark
import com.example.ui.theme.HeartRateRed
import com.example.ui.theme.StepsGreen
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun SettingsScreen(
    device: BipDevice?,
    watchSettings: WatchSettings,
    logStream: SharedFlow<String>,
    onUpdateSettings: (WatchSettings) -> Unit,
    onDisconnect: () -> Unit,
    onPairNew: () -> Unit,
    onCheckFirmware: () -> Unit,
    modifier: Modifier = Modifier
) {
    val logs = remember { mutableStateListOf<String>() }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        logStream.collect { log ->
            logs.add(log)
            if (logs.size > 40) {
                logs.removeAt(0)
            }
            listState.animateScrollToItem((logs.size - 1).coerceAtLeast(0))
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hardware Specs Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("device_specs_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Watch, contentDescription = "Watch", tint = CyanPrimaryDark)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Amazfit Bip Max Specifications",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    SpecItem(label = "Model", value = device?.model ?: "Bip Max (A2170)")
                    SpecItem(label = "Bluetooth MAC", value = device?.macAddress ?: "D4:F5:13:B9:A8:4C")
                    SpecItem(label = "Display", value = "2.07\" HD AMOLED (432x514 px, 302 PPI)")
                    SpecItem(label = "Biometric Sensor", value = "BioTracker™ 5.0 PPG Optical Sensor")
                    SpecItem(label = "Water Resistance", value = "5 ATM (50 meters water-resistant)")
                    SpecItem(label = "Battery Capacity", value = "550 mAh (Up to 14 days typical)")
                    SpecItem(label = "Firmware OS", value = "${device?.firmwareVersion ?: "v1.4.2.18"} (Zepp OS Core)")
                    SpecItem(label = "Hardware Revision", value = device?.hardwareRevision ?: "V2.1 Nordic nRF52840")
                }
            }
        }

        // Watch Behavior Settings
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Device Preferences",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Lift Wrist to View Info", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = watchSettings.liftToWakeEnabled,
                            onCheckedChange = { onUpdateSettings(watchSettings.copy(liftToWakeEnabled = it)) }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("24-Hour Time Format", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = watchSettings.timeFormat24Hour,
                            onCheckedChange = { onUpdateSettings(watchSettings.copy(timeFormat24Hour = it)) }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Metric Distance (km)", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = watchSettings.distanceUnitKm,
                            onCheckedChange = { onUpdateSettings(watchSettings.copy(distanceUnitKm = it)) }
                        )
                    }
                }
            }
        }

        // OTA Firmware Update
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SystemUpdate, contentDescription = "OTA", tint = CyanPrimaryDark)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Firmware Update", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("Current: ${device?.firmwareVersion ?: "v1.4.2.18"}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Button(
                            onClick = onCheckFirmware,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("check_firmware_button")
                        ) {
                            Text("Check OTA", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // BLE Diagnostic Log Stream Terminal
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ble_log_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF070B14))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Build, contentDescription = "Diagnostic", tint = StepsGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "BLE GATT Live Packet Console",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = StepsGreen
                            )
                        }
                        Text(
                            text = "0xFEE0 / 0xFEE1",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF030712))
                            .padding(10.dp)
                    ) {
                        if (logs.isEmpty()) {
                            Text(
                                text = "> Waiting for BLE events...",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                fontFamily = FontFamily.Monospace
                            )
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(logs) { log ->
                                    Text(
                                        text = "> $log",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (log.contains("verified") || log.contains("Connected") || log.contains("complete")) StepsGreen else Color(0xFF94A3B8),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Device Management Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onPairNew,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pair_another_watch_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Bluetooth, contentDescription = "Pair", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pair Watch")
                }

                Button(
                    onClick = onDisconnect,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("disconnect_watch_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = HeartRateRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PowerSettingsNew, contentDescription = "Disconnect", modifier = Modifier.size(18.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Disconnect", color = Color.White)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SpecItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
    }
}
