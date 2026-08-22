package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CustomWatchFaceConfig
import com.example.data.model.WatchFace
import com.example.ui.theme.BatteryGreen
import com.example.ui.theme.CalorieOrange
import com.example.ui.theme.CyanPrimaryDark
import com.example.ui.theme.EmeraldTertiaryDark
import com.example.ui.theme.HeartRateRed
import com.example.ui.theme.StepsGreen

@Composable
fun WatchFaceScreen(
    watchFaces: List<WatchFace>,
    watchFaceProgress: Float?,
    onInstallWatchFace: (WatchFace) -> Unit,
    onSaveCustomWatchFace: (CustomWatchFaceConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedFaceForDetail by remember { mutableStateOf<WatchFace?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("watchface_screen")
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Bip Max Gallery") },
                icon = { Icon(Icons.Default.Watch, contentDescription = "Gallery", modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("watchface_gallery_tab")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("DIY Face Builder") },
                icon = { Icon(Icons.Default.Palette, contentDescription = "Builder", modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("watchface_builder_tab")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item(span = { GridItemSpan(2) }) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Info",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Amazfit Bip Max HD Display (1.91\" 320x380 px). Select a watch face to flash to your watch via BLE.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                items(watchFaces) { face ->
                    WatchFaceCard(
                        watchFace = face,
                        onSelect = { selectedFaceForDetail = face },
                        onInstall = { onInstallWatchFace(face) }
                    )
                }

                item(span = { GridItemSpan(2) }) {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        } else {
            CustomWatchFaceBuilderView(
                onApply = { config ->
                    onSaveCustomWatchFace(config)
                }
            )
        }
    }

    // Detail & Install Modal Dialog
    selectedFaceForDetail?.let { face ->
        WatchFaceDetailDialog(
            watchFace = face,
            onInstall = {
                selectedFaceForDetail = null
                onInstallWatchFace(face)
            },
            onDismiss = { selectedFaceForDetail = null }
        )
    }

    // Flashing Progress Dialog
    watchFaceProgress?.let { progress ->
        Dialog(onDismissRequest = {}) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("flashing_progress_dialog"),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Upload,
                        contentDescription = "Uploading",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Flashing Watch Face to Bip Max",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Transferring .bin chunks via BLE GATT... ${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    }
}

@Composable
fun WatchFaceCard(
    watchFace: WatchFace,
    onSelect: () -> Unit,
    onInstall: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("watchface_card_${watchFace.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Simulated Bip Max Watch Frame Screen
            BipMaxWatchDisplay(
                watchFace = watchFace,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.85f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = watchFace.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = "${watchFace.style} • ${watchFace.fileSizeKb} KB",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (watchFace.isInstalled) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(StepsGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Active",
                        tint = StepsGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Active on Watch",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = StepsGreen
                    )
                }
            } else {
                Button(
                    onClick = onInstall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .testTag("install_btn_${watchFace.id}"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Install",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Install", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun BipMaxWatchDisplay(
    watchFace: WatchFace,
    modifier: Modifier = Modifier
) {
    val primaryColor = try {
        Color(android.graphics.Color.parseColor(watchFace.primaryColorHex))
    } catch (e: Exception) {
        CyanPrimaryDark
    }
    val accentColor = try {
        Color(android.graphics.Color.parseColor(watchFace.accentColorHex))
    } catch (e: Exception) {
        CalorieOrange
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF070B14))
            .border(2.5.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            watchFace.id.contains("matrix") -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "10:42",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = primaryColor,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "7,432", style = MaterialTheme.typography.labelSmall, color = accentColor)
                        Text(text = "•", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(text = "72 bpm", style = MaterialTheme.typography.labelSmall, color = HeartRateRed)
                    }
                }
            }
            watchFace.id.contains("tri_ring") -> {
                Box(contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(54.dp)) {
                        drawCircle(color = StepsGreen.copy(alpha = 0.3f), style = Stroke(4f))
                        drawArc(color = StepsGreen, startAngle = -90f, sweepAngle = 260f, useCenter = false, style = Stroke(4f, cap = StrokeCap.Round))
                        drawCircle(color = CalorieOrange.copy(alpha = 0.3f), radius = size.minDimension / 2.8f, style = Stroke(4f))
                        drawArc(color = CalorieOrange, startAngle = -90f, sweepAngle = 180f, useCenter = false, style = Stroke(4f, cap = StrokeCap.Round))
                    }
                    Text(text = "10:42", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            watchFace.id.contains("chrono") -> {
                Canvas(modifier = Modifier.size(56.dp)) {
                    drawCircle(color = Color(0xFF1E293B))
                    drawCircle(color = primaryColor, style = Stroke(2f))
                    // Clock hands
                    val centerOffset = Offset(size.width / 2, size.height / 2)
                    drawLine(color = Color.White, start = centerOffset, end = Offset(centerOffset.x + 12f, centerOffset.y - 12f), strokeWidth = 3f, cap = StrokeCap.Round)
                    drawLine(color = accentColor, start = centerOffset, end = Offset(centerOffset.x - 8f, centerOffset.y - 18f), strokeWidth = 2f, cap = StrokeCap.Round)
                }
            }
            watchFace.id.contains("aurora") -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(primaryColor.copy(alpha = 0.5f), accentColor.copy(alpha = 0.3f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "10:42", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(text = "AUG 22 SAT", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                    }
                }
            }
            watchFace.id.contains("pixel") -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "10:42", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = primaryColor, fontFamily = FontFamily.Monospace)
                    Text(text = "👾 7,432", style = MaterialTheme.typography.labelSmall, color = accentColor)
                }
            }
            else -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "10:42", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = primaryColor)
                    Text(text = "Bip Max", style = MaterialTheme.typography.labelSmall, color = accentColor)
                }
            }
        }
    }
}

@Composable
fun CustomWatchFaceBuilderView(
    onApply: (CustomWatchFaceConfig) -> Unit
) {
    var themeColor by remember { mutableStateOf("#38BDF8") }
    var backgroundStyle by remember { mutableStateOf("Neon Dark") }
    var isDigital by remember { mutableStateOf(true) }
    var showSteps by remember { mutableStateOf(true) }
    var showHeartRate by remember { mutableStateOf(true) }
    var showBattery by remember { mutableStateOf(true) }
    var showWeather by remember { mutableStateOf(true) }

    val colorOptions = listOf("#38BDF8", "#F87171", "#34D399", "#F59E0B", "#A855F7", "#EC4899")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("custom_builder_view"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Preview Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Live Bip Max Display Preview",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .size(160.dp, 190.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0B1120))
                        .border(3.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (showBattery) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Watch, contentDescription = "Bip", tint = BatteryGreen, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("84%", style = MaterialTheme.typography.labelSmall, color = BatteryGreen)
                            }
                        }

                        Text(
                            text = if (isDigital) "10:42" else "● 10:42 ●",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(android.graphics.Color.parseColor(themeColor))
                        )

                        Row(
                            horizontalArrangement = Arrangement.SpaceAround,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (showSteps) {
                                Text("7.4k steps", style = MaterialTheme.typography.labelSmall, color = StepsGreen)
                            }
                            if (showHeartRate) {
                                Text("72 bpm", style = MaterialTheme.typography.labelSmall, color = HeartRateRed)
                            }
                        }
                    }
                }
            }
        }

        // Color selector
        Text(text = "Primary Theme Accent", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            colorOptions.forEach { hex ->
                val col = Color(android.graphics.Color.parseColor(hex))
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(col)
                        .clickable { themeColor = hex }
                        .border(
                            width = if (themeColor == hex) 3.dp else 0.dp,
                            color = if (themeColor == hex) Color.White else Color.Transparent,
                            shape = CircleShape
                        )
                )
            }
        }

        // Complications Toggles
        Text(text = "Display Complications", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Step Counter Arc", style = MaterialTheme.typography.bodyMedium)
            Switch(checked = showSteps, onCheckedChange = { showSteps = it })
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Heart Rate Indicator", style = MaterialTheme.typography.bodyMedium)
            Switch(checked = showHeartRate, onCheckedChange = { showHeartRate = it })
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Battery Percentage", style = MaterialTheme.typography.bodyMedium)
            Switch(checked = showBattery, onCheckedChange = { showBattery = it })
        }

        Button(
            onClick = {
                onApply(
                    CustomWatchFaceConfig(
                        themeColorHex = themeColor,
                        isDigital = isDigital,
                        showSteps = showSteps,
                        showHeartRate = showHeartRate,
                        showBattery = showBattery,
                        showWeather = showWeather,
                        backgroundStyle = backgroundStyle
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("apply_custom_watchface_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Upload, contentDescription = "Flash")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Build & Flash to Bip Max")
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun WatchFaceDetailDialog(
    watchFace: WatchFace,
    onInstall: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("watchface_detail_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = watchFace.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "By ${watchFace.author} • ${watchFace.fileSizeKb} KB",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                BipMaxWatchDisplay(
                    watchFace = watchFace,
                    modifier = Modifier
                        .size(150.dp, 175.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = watchFace.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = onInstall,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_install_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Install", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Flash Face")
                    }
                }
            }
        }
    }
}
