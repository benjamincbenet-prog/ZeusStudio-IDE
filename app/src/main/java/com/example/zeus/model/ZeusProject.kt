package com.example.zeus.model

import java.util.UUID

enum class FileType(val extension: String, val displayName: String) {
    JS(".js", "JavaScript"),
    STYLE_JS(".style.js", "Style JS"),
    JSON(".json", "JSON Config"),
    MARKDOWN(".md", "Markdown"),
    ASSET(".png", "Image Asset")
}

data class ZeusFile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val path: String,
    val content: String,
    val fileType: FileType,
    val isModified: Boolean = false,
    val isReadOnly: Boolean = false
)

enum class ZeusTemplate(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val iconName: String,
    val description: String
) {
    BIP_MAX_DIGITAL_PRO(
        id = "bip_max_digital_pro",
        title = "Digital Pro Watch Face",
        subtitle = "Zepp OS 5.0 • 432x514 AMOLED",
        category = "Watch Face",
        iconName = "watch",
        description = "Cyberpunk digital watch face with neon time, live heart rate pulse, step goal arc, battery gauge, and weather complication."
    ),
    BIP_MAX_FITNESS_TRACKER(
        id = "bip_max_fitness_tracker",
        title = "HIIT & Cardio Tracker",
        subtitle = "Zepp OS 5.0 • Mini App",
        category = "Mini App",
        iconName = "fitness",
        description = "Full fitness companion app with heart rate zone monitor, calorie burn calculator, interval timer, and cadence telemetry."
    ),
    BIP_MAX_WEATHER_WIDGET(
        id = "bip_max_weather_widget",
        title = "Atmosphere & Barometer",
        subtitle = "Zepp OS 5.0 • Widget",
        category = "Widget",
        iconName = "weather",
        description = "Live atmospheric widget displaying 5-day forecasts, UV index, air quality rating, barometric pressure, and sunrise/sunset."
    ),
    BIP_MAX_BLE_CONTROLLER(
        id = "bip_max_ble_controller",
        title = "BLE Media & Camera Remote",
        subtitle = "Zepp OS 5.0 • Companion App",
        category = "Companion",
        iconName = "remote",
        description = "Smart remote controller allowing Bip Max to control phone music playback, trigger camera shutter, and send BLE telemetry."
    ),
    BIP_MAX_MINIMAL_ANALOG(
        id = "bip_max_minimal_analog",
        title = "Minimalist Swiss Analog",
        subtitle = "Zepp OS 5.0 • Watch Face",
        category = "Watch Face",
        iconName = "analog",
        description = "High-precision analog watch face with smooth sweeping seconds, luxury indexes, and date window optimized for Bip Max display."
    )
}

data class BipMaxHardwareSpecs(
    val modelName: String = "Amazfit Bip Max (A2286)",
    val displaySizeInches: String = "2.07\" HD AMOLED",
    val resolution: String = "432 x 514 pixels",
    val pixelDensity: String = "302 PPI",
    val peakBrightness: String = "Up to 3,000 nits",
    val screenGlass: String = "2.5D Tempered Glass + Anti-fingerprint coating",
    val batteryCapacity: String = "550 mAh",
    val batteryLifeTypical: String = "Up to 20 days",
    val batteryLifeHeavy: String = "Up to 10 days",
    val batteryLifeGps: String = "Up to 40 hours continuous",
    val osVersion: String = "Zepp OS 5.0 / 6.0",
    val bluetooth: String = "Bluetooth 5.3 BLE",
    val waterResistance: String = "IP68 / 5 ATM (50m)",
    val sensors: List<String> = listOf(
        "BioTracker™ 5.0 PPG Biometric Sensor (Heart Rate & SpO2)",
        "3-Axis Accelerometer Sensor",
        "Geomagnetic Sensor (Compass)",
        "Barometric Altimeter",
        "Ambient Light Sensor",
        "4-Satellite GNSS Positioning (GPS, GLONASS, Galileo, BDS)"
    )
)

data class ZeusProject(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val appType: String = "watchface", // "watchface" or "app" or "widget"
    val template: ZeusTemplate,
    val targetDevice: String = "bip_max",
    val targetResolution: String = "432x514",
    val zeppOsVersion: String = "5.0",
    val appId: String = "1008601",
    val version: String = "1.0.0",
    val files: List<ZeusFile> = emptyList(),
    val activeFileId: String? = null,
    val lastBuiltTimestamp: Long? = null,
    val isDevServerRunning: Boolean = false,
    val lastBuildSuccess: Boolean? = null
)

data class ZabPackage(
    val packageName: String,
    val version: String,
    val targetDevice: String = "Amazfit Bip Max (A2286)",
    val resolution: String = "432x514",
    val fileSizeKb: Double,
    val checksumCrc32: String,
    val builtAtTimestamp: Long = System.currentTimeMillis(),
    val fileCount: Int,
    val appType: String
)

data class SensorSimulationState(
    val heartRateBpm: Int = 78,
    val steps: Int = 6420,
    val stepGoal: Int = 10000,
    val batteryPercent: Int = 84,
    val isCharging: Boolean = false,
    val temperatureCelsius: Int = 24,
    val weatherCondition: String = "Sunny",
    val calorieKcal: Int = 345,
    val distanceKm: Float = 4.8f,
    val spO2Percent: Int = 98,
    val isBleConnected: Boolean = true,
    val workoutRunning: Boolean = false,
    val workoutDurationSec: Int = 1240
)

data class ZeusLogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel,
    val tag: String,
    val message: String
) {
    enum class LogLevel {
        INFO, SUCCESS, WARNING, ERROR, BLE, ZEUS
    }
}
