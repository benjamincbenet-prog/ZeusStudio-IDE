package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.SportsGymnastics
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WorkoutRecord
import com.example.data.model.WorkoutType
import com.example.ui.theme.CalorieOrange
import com.example.ui.theme.CyanPrimaryDark
import com.example.ui.theme.EmeraldTertiaryDark
import com.example.ui.theme.HeartRateRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WorkoutScreen(
    workouts: List<WorkoutRecord>,
    isWorkoutActive: Boolean,
    isWorkoutPaused: Boolean,
    activeType: WorkoutType,
    activeSeconds: Long,
    activeDistanceKm: Float,
    activeCalories: Int,
    liveHeartRate: Int?,
    onStartWorkout: (WorkoutType) -> Unit,
    onPauseWorkout: () -> Unit,
    onResumeWorkout: () -> Unit,
    onStopWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTypeForNew by remember { mutableStateOf(WorkoutType.OUTDOOR_RUN) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("workout_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (isWorkoutActive) {
            // Live Active Workout HUD
            item {
                ActiveWorkoutHud(
                    type = activeType,
                    seconds = activeSeconds,
                    distanceKm = activeDistanceKm,
                    calories = activeCalories,
                    heartRate = liveHeartRate ?: 0,
                    isPaused = isWorkoutPaused,
                    onPause = onPauseWorkout,
                    onResume = onResumeWorkout,
                    onStop = onStopWorkout
                )
            }
        } else {
            // Workout Launcher Panel
            item {
                WorkoutModeSelectorCard(
                    selectedType = selectedTypeForNew,
                    onSelectType = { selectedTypeForNew = it },
                    onStart = { onStartWorkout(selectedTypeForNew) }
                )
            }
        }

        // Past Workouts History Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Workout History (${workouts.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Synced with Bip Max",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (workouts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No workouts logged yet.\nSelect a mode above and start tracking!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(workouts) { record ->
                WorkoutHistoryItemCard(record = record)
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ActiveWorkoutHud(
    type: WorkoutType,
    seconds: Long,
    distanceKm: Float,
    calories: Int,
    heartRate: Int,
    isPaused: Boolean,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit
) {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    val timeFormatted = if (hrs > 0) {
        String.format(Locale.US, "%02d:%02d:%02d", hrs, mins, secs)
    } else {
        String.format(Locale.US, "%02d:%02d", mins, secs)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_workout_hud"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = getWorkoutIcon(type),
                        contentDescription = type.title,
                        tint = CyanPrimaryDark,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = type.title.uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimaryDark
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isPaused) CalorieOrange.copy(alpha = 0.2f) else EmeraldTertiaryDark.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isPaused) "PAUSED" else "LIVE SYNC (BIP MAX)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isPaused) CalorieOrange else EmeraldTertiaryDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Timer
            Text(
                text = timeFormatted,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "DURATION",
                style = MaterialTheme.typography.labelSmall,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 3-Metric Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format(Locale.US, "%.2f", distanceKm),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "KM DISTANCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.LightGray
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$calories",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = CalorieOrange
                    )
                    Text(
                        text = "KCAL BURNT",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.LightGray
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$heartRate",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = HeartRateRed
                    )
                    Text(
                        text = "BPM HEART",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.LightGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (isPaused) {
                    Button(
                        onClick = onResume,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("resume_workout_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldTertiaryDark),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Resume")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Resume", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    FilledTonalButton(
                        onClick = onPause,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pause_workout_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = "Pause")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pause", fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onStop,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stop_workout_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = HeartRateRed),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Finish & Save", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun WorkoutModeSelectorCard(
    selectedType: WorkoutType,
    onSelectType: (WorkoutType) -> Unit,
    onStart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("workout_mode_selector"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Start Workout on Bip Max",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Live telemetry & GPS will stream directly between your Bip Max watch and phone.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Workout modes grid
            val modes = listOf(
                WorkoutType.OUTDOOR_RUN,
                WorkoutType.OUTDOOR_CYCLING,
                WorkoutType.OUTDOOR_WALK,
                WorkoutType.POOL_SWIMMING,
                WorkoutType.TREADMILL,
                WorkoutType.FREESTYLE
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                modes.take(3).forEach { mode ->
                    WorkoutModeChip(
                        type = mode,
                        isSelected = selectedType == mode,
                        onSelect = { onSelectType(mode) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                modes.drop(3).forEach { mode ->
                    WorkoutModeChip(
                        type = mode,
                        isSelected = selectedType == mode,
                        onSelect = { onSelectType(mode) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("start_workout_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Start")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start ${selectedType.title}",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun WorkoutModeChip(
    type: WorkoutType,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
            )
            .clickable { onSelect() }
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = getWorkoutIcon(type),
                contentDescription = type.title,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = type.title.substringBefore(" "),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
fun WorkoutHistoryItemCard(record: WorkoutRecord) {
    val dateStr = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.US).format(Date(record.timestamp))
    val durationMins = record.durationSeconds / 60
    val durationSecs = record.durationSeconds % 60

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("workout_record_${record.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getWorkoutIcon(record.type),
                    contentDescription = record.type.title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.type.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "${record.distanceKm} km",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimaryDark
                    )
                    Text(
                        text = "${durationMins}m ${durationSecs}s",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${record.caloriesBurned} kcal",
                        style = MaterialTheme.typography.labelSmall,
                        color = CalorieOrange
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${record.avgHeartRateBpm} bpm",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = HeartRateRed
                )
                Text(
                    text = "Pace: ${record.avgPacePerKm}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun getWorkoutIcon(type: WorkoutType): ImageVector {
    return when (type) {
        WorkoutType.OUTDOOR_RUN -> Icons.Default.DirectionsRun
        WorkoutType.OUTDOOR_CYCLING -> Icons.Default.DirectionsBike
        WorkoutType.OUTDOOR_WALK -> Icons.Default.DirectionsWalk
        WorkoutType.POOL_SWIMMING -> Icons.Default.Pool
        WorkoutType.TREADMILL -> Icons.Default.FitnessCenter
        WorkoutType.FREESTYLE -> Icons.Default.SportsGymnastics
    }
}
