package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BipDevice
import com.example.data.model.ConnectionState
import com.example.ui.theme.BatteryGreen
import com.example.ui.theme.CalorieOrange
import com.example.ui.theme.CyanPrimaryDark
import com.example.ui.theme.DarkCard
import com.example.ui.theme.HeartRateRed
import com.example.ui.theme.SleepPurple
import com.example.ui.theme.SpO2Blue
import com.example.ui.theme.StepsGreen

@Composable
fun BipActivityRings(
    stepsProgress: Float,
    caloriesProgress: Float,
    distanceProgress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(150.dp)) {
            val strokeW = 12.dp.toPx()
            val centerOffset = Offset(size.width / 2, size.height / 2)

            // Outer Ring: Steps (Green)
            val radiusOuter = (size.minDimension / 2) - (strokeW / 2)
            drawCircle(
                color = StepsGreen.copy(alpha = 0.2f),
                radius = radiusOuter,
                center = centerOffset,
                style = Stroke(width = strokeW)
            )
            drawArc(
                color = StepsGreen,
                startAngle = -90f,
                sweepAngle = (stepsProgress.coerceIn(0f, 1f) * 360f),
                useCenter = false,
                topLeft = Offset(centerOffset.x - radiusOuter, centerOffset.y - radiusOuter),
                size = Size(radiusOuter * 2, radiusOuter * 2),
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )

            // Middle Ring: Calories (Orange)
            val radiusMiddle = radiusOuter - strokeW - 4.dp.toPx()
            drawCircle(
                color = CalorieOrange.copy(alpha = 0.2f),
                radius = radiusMiddle,
                center = centerOffset,
                style = Stroke(width = strokeW)
            )
            drawArc(
                color = CalorieOrange,
                startAngle = -90f,
                sweepAngle = (caloriesProgress.coerceIn(0f, 1f) * 360f),
                useCenter = false,
                topLeft = Offset(centerOffset.x - radiusMiddle, centerOffset.y - radiusMiddle),
                size = Size(radiusMiddle * 2, radiusMiddle * 2),
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )

            // Inner Ring: Distance (Cyan)
            val radiusInner = radiusMiddle - strokeW - 4.dp.toPx()
            drawCircle(
                color = CyanPrimaryDark.copy(alpha = 0.2f),
                radius = radiusInner,
                center = centerOffset,
                style = Stroke(width = strokeW)
            )
            drawArc(
                color = CyanPrimaryDark,
                startAngle = -90f,
                sweepAngle = (distanceProgress.coerceIn(0f, 1f) * 360f),
                useCenter = false,
                topLeft = Offset(centerOffset.x - radiusInner, centerOffset.y - radiusInner),
                size = Size(radiusInner * 2, radiusInner * 2),
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Watch,
                contentDescription = "Bip Max Sync",
                tint = CyanPrimaryDark,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "${(stepsProgress * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "GOAL",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BipHeroDeviceCard(
    device: BipDevice?,
    connectionState: ConnectionState,
    batteryPercent: Int,
    isCharging: Boolean,
    onFindWatch: () -> Unit,
    onSyncNow: () -> Unit,
    onPairClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bip_hero_device_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Watch,
                            contentDescription = "Bip Max Icon",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = device?.name ?: "Amazfit Bip Max",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (connectionState) {
                                            ConnectionState.CONNECTED -> StepsGreen
                                            ConnectionState.CONNECTING, ConnectionState.AUTHENTICATING, ConnectionState.SYNCING -> CalorieOrange
                                            else -> HeartRateRed
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (connectionState) {
    ConnectionState.CONNECTED -> "Connected (BLE)"
    ConnectionState.CONNECTING -> "Connecting..."
    ConnectionState.AUTHENTICATING -> "Auth Handshake..."
    ConnectionState.SYNCING -> "Syncing Telemetry..."
    ConnectionState.SCANNING -> "Scanning..."
    ConnectionState.DISCONNECTED -> "Disconnected"
    ConnectionState.BLUETOOTH_DISABLED -> "Bluetooth Off"
    ConnectionState.BLUETOOTH_UNAVAILABLE -> "Bluetooth Unsupported"
},
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Battery Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                        contentDescription = "Battery",
                        tint = if (isCharging) CalorieOrange else BatteryGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$batteryPercent%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Specs row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Model",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = device?.model?.substringBefore(" ") ?: "Bip Max",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Firmware",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = device?.firmwareVersion ?: "v1.4.2",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Signal",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${device?.rssi ?: -58} dBm",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = onSyncNow,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("bip_sync_now_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sync",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sync Data")
                }

                FilledTonalButton(
                    onClick = onFindWatch,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("bip_find_watch_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = "Find Watch",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Find Watch")
                }

                OutlinedButton(
                    onClick = onPairClick,
                    modifier = Modifier.testTag("bip_pair_switch_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bluetooth,
                        contentDescription = "Pair",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HeartRatePulseCard(
    currentBpm: Int,
    restingBpm: Int,
    maxBpm: Int,
    minBpm: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "heart_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("heart_rate_pulse_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Heart Rate",
                        tint = HeartRateRed,
                        modifier = Modifier
                            .size(24.dp)
                            .scale(pulseScale)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Heart Rate (PPG)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Live PPG Continuous",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "$currentBpm",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = HeartRateRed
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "BPM",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Zone pill
                val zoneText = when {
                    currentBpm < 60 -> "Resting"
                    currentBpm < 100 -> "Normal"
                    currentBpm < 140 -> "Fat Burn"
                    currentBpm < 170 -> "Cardio"
                    else -> "Peak"
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(HeartRateRed.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = zoneText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = HeartRateRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Resting: $restingBpm BPM",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Min: $minBpm BPM",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Max: $maxBpm BPM",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SleepStagesBar(
    totalMinutes: Int,
    deepMinutes: Int,
    lightMinutes: Int,
    remMinutes: Int,
    awakeMinutes: Int,
    modifier: Modifier = Modifier
) {
    val total = (deepMinutes + lightMinutes + remMinutes + awakeMinutes).coerceAtLeast(1).toFloat()
    val hours = totalMinutes / 60
    val mins = totalMinutes % 60

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sleep_stages_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sleep Quality & Stages",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${hours}h ${mins}m",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = SleepPurple
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Multi-segment horizontal stage bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
            ) {
                if (deepMinutes > 0) {
                    Box(
                        modifier = Modifier
                            .weight(deepMinutes / total)
                            .height(14.dp)
                            .background(Color(0xFF4C1D95))
                    )
                }
                if (lightMinutes > 0) {
                    Box(
                        modifier = Modifier
                            .weight(lightMinutes / total)
                            .height(14.dp)
                            .background(SleepPurple)
                    )
                }
                if (remMinutes > 0) {
                    Box(
                        modifier = Modifier
                            .weight(remMinutes / total)
                            .height(14.dp)
                            .background(CyanPrimaryDark)
                    )
                }
                if (awakeMinutes > 0) {
                    Box(
                        modifier = Modifier
                            .weight(awakeMinutes / total)
                            .height(14.dp)
                            .background(CalorieOrange)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StageLegendItem(color = Color(0xFF4C1D95), label = "Deep", duration = "${deepMinutes}m")
                StageLegendItem(color = SleepPurple, label = "Light", duration = "${lightMinutes}m")
                StageLegendItem(color = CyanPrimaryDark, label = "REM", duration = "${remMinutes}m")
                StageLegendItem(color = CalorieOrange, label = "Awake", duration = "${awakeMinutes}m")
            }
        }
    }
}

@Composable
private fun StageLegendItem(color: Color, label: String, duration: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = duration,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun HealthMetricTile(
    title: String,
    value: String,
    unit: String,
    subtext: String,
    accentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("health_tile_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor
                )
                if (unit.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
