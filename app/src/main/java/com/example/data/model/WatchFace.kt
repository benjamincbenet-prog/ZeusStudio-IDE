package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_faces")
data class WatchFace(
    @PrimaryKey
    val id: String,
    val title: String,
    val style: String,
    val author: String,
    val fileSizeKb: Int,
    val isInstalled: Boolean = false,
    val isDefault: Boolean = false,
    val primaryColorHex: String = "#38BDF8",
    val accentColorHex: String = "#F87171",
    val description: String,
    val features: List<String> = listOf("Steps", "Heart Rate", "Battery", "Weather", "Date")
)

data class CustomWatchFaceConfig(
    val themeColorHex: String = "#38BDF8",
    val isDigital: Boolean = true,
    val showSteps: Boolean = true,
    val showHeartRate: Boolean = true,
    val showBattery: Boolean = true,
    val showWeather: Boolean = true,
    val showSeconds: Boolean = true,
    val backgroundStyle: String = "Neon Dark"
)
