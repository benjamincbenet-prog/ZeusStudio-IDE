package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_configs")
data class AppNotificationRule(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val isEnabled: Boolean = true,
    val vibrationPattern: VibrationPattern = VibrationPattern.STANDARD,
    val onlyWhenScreenOff: Boolean = false
)

enum class VibrationPattern(val label: String, val patternMs: List<Long>) {
    SHORT("Short Pulse (100ms)", listOf(100L)),
    STANDARD("Standard (250ms)", listOf(250L)),
    DOUBLE("Double Tap", listOf(150L, 100L, 150L)),
    LONG("Long Alert (600ms)", listOf(600L)),
    RAPID("Rapid Buzz", listOf(80L, 50L, 80L, 50L, 80L)),
    SOS("SOS Emergency", listOf(100L, 100L, 100L, 100L, 300L, 100L, 300L))
}

data class WatchSettings(
    val liftToWakeEnabled: Boolean = true,
    val dndEnabled: Boolean = false,
    val dndStartHour: Int = 23,
    val dndEndHour: Int = 7,
    val sedentaryAlertEnabled: Boolean = true,
    val heartRateIntervalMinutes: Int = 5, // 1, 5, 10, 30
    val screenBrightnessPercent: Int = 75,
    val screenTimeoutSeconds: Int = 15,
    val distanceUnitKm: Boolean = true,
    val temperatureUnitCelsius: Boolean = true,
    val timeFormat24Hour: Boolean = true
)
