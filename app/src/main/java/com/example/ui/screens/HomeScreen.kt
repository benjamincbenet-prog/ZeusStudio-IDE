package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.BipDevice
import com.example.data.model.ConnectionState
import com.example.data.model.HealthMetricRecord
import com.example.ui.components.BipActivityRings
import com.example.ui.components.BipHeroDeviceCard
import com.example.ui.components.HealthMetricTile
import com.example.ui.components.HeartRatePulseCard
import com.example.ui.components.SleepStagesBar
import com.example.ui.theme.CalorieOrange
import com.example.ui.theme.CyanPrimaryDark
import com.example.ui.theme.PaiYellow
import com.example.ui.theme.SleepPurple
import com.example.ui.theme.SpO2Blue
import com.example.ui.theme.StepsGreen

@Composable
fun HomeScreen(
    activeDevice: BipDevice?,
    connectionState: ConnectionState,
    batteryPercent: Int,
    isCharging: Boolean,
    liveSteps: Int,
    liveHeartRate: Int,
    health: HealthMetricRecord?,
    onFindWatch: () -> Unit,
    onSyncNow: () -> Unit,
    onPairClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val steps = liveSteps
    val stepGoal = health?.stepGoal ?: 10000
    val stepsProgress = (steps.toFloat() / stepGoal).coerceIn(0f, 1f)

    val calories = (steps * 0.045f).toInt()
    val calorieGoal = health?.calorieGoal ?: 550
    val caloriesProgress = (calories.toFloat() / calorieGoal).coerceIn(0f, 1f)

    val distanceMeters = (steps * 0.75f).toInt()
    val distanceKm = String.format(java.util.Locale.US, "%.2f", distanceMeters / 1000f)
    val distanceProgress = (distanceMeters.toFloat() / (health?.distanceGoalMeters ?: 7000)).coerceIn(0f, 1f)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("home_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            BipHeroDeviceCard(
                device = activeDevice,
                connectionState = connectionState,
                batteryPercent = batteryPercent,
                isCharging = isCharging,
                onFindWatch = onFindWatch,
                onSyncNow = onSyncNow,
                onPairClick = onPairClick
            )
        }

        // Daily Activity Section
        item {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Daily Activity Target",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BipActivityRings(
                        stepsProgress = stepsProgress,
                        caloriesProgress = caloriesProgress,
                        distanceProgress = distanceProgress
                    )

                    Column(
                        modifier = Modifier.padding(start = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ActivityStatItem(
                            title = "Steps",
                            value = "$steps",
                            goal = "/ $stepGoal",
                            color = StepsGreen
                        )
                        ActivityStatItem(
                            title = "Calories",
                            value = "$calories kcal",
                            goal = "/ $calorieGoal",
                            color = CalorieOrange
                        )
                        ActivityStatItem(
                            title = "Distance",
                            value = "$distanceKm km",
                            goal = "/ 7.0 km",
                            color = CyanPrimaryDark
                        )
                    }
                }
            }
        }

        // Live PPG Heart Rate Pulse
        item {
            HeartRatePulseCard(
                currentBpm = liveHeartRate,
                restingBpm = health?.restingHeartRateBpm ?: 61,
                maxBpm = health?.maxHeartRateBpm ?: 138,
                minBpm = health?.minHeartRateBpm ?: 54
            )
        }

        // Health Telemetry Grid (SpO2, Stress, PAI, Sleep)
        item {
            Text(
                text = "BioTracker™ Telemetry",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HealthMetricTile(
                    title = "Blood Oxygen",
                    value = "${health?.spO2Percentage ?: 98}",
                    unit = "%",
                    subtext = "Optimal Range",
                    accentColor = SpO2Blue,
                    icon = Icons.Default.Air,
                    modifier = Modifier.weight(1f)
                )

                HealthMetricTile(
                    title = "Stress Level",
                    value = "${health?.stressLevel ?: 28}",
                    unit = "/ 100",
                    subtext = "Relaxed Zone",
                    accentColor = CalorieOrange,
                    icon = Icons.Default.Psychology,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HealthMetricTile(
                    title = "PAI Score",
                    value = "${health?.paiScore ?: 84}",
                    unit = "pts",
                    subtext = "7-Day Total",
                    accentColor = PaiYellow,
                    icon = Icons.Default.Star,
                    modifier = Modifier.weight(1f)
                )

                val sleepHrs = (health?.totalSleepMinutes ?: 462) / 60
                val sleepMins = (health?.totalSleepMinutes ?: 462) % 60
                HealthMetricTile(
                    title = "Sleep Score",
                    value = "88",
                    unit = "pts",
                    subtext = "${sleepHrs}h ${sleepMins}m restful",
                    accentColor = SleepPurple,
                    icon = Icons.Default.Bedtime,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Sleep Stages Breakdown
        item {
            SleepStagesBar(
                totalMinutes = health?.totalSleepMinutes ?: 462,
                deepMinutes = health?.deepSleepMinutes ?: 112,
                lightMinutes = health?.lightSleepMinutes ?: 240,
                remMinutes = health?.remSleepMinutes ?: 88,
                awakeMinutes = health?.awakeMinutes ?: 22
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ActivityStatItem(
    title: String,
    value: String,
    goal: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.padding(start = 4.dp))
            Text(
                text = goal,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
