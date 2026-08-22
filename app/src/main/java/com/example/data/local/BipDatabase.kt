package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AppNotificationRule
import com.example.data.model.BipDevice
import com.example.data.model.HealthMetricRecord
import com.example.data.model.VibrationPattern
import com.example.data.model.WatchFace
import com.example.data.model.WorkoutRecord
import com.example.data.model.WorkoutType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        BipDevice::class,
        HealthMetricRecord::class,
        WorkoutRecord::class,
        WatchFace::class,
        AppNotificationRule::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BipDatabase : RoomDatabase() {
    abstract fun bipDao(): BipDao

    companion object {
        @Volatile
        private var INSTANCE: BipDatabase? = null

        fun getInstance(context: Context): BipDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BipDatabase::class.java,
                    "bip_max_companion.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            populateInitialData(getInstance(context).bipDao())
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(dao: BipDao) {
            // Initial Paired Bip Max Device
            val defaultDevice = BipDevice(
                macAddress = "D4:F5:13:B9:A8:4C",
                name = "Amazfit Bip Max",
                model = "Bip Max (A2170)",
                firmwareVersion = "v1.4.2.18",
                hardwareRevision = "V2.1 - Nordic nRF52840",
                batteryPercent = 88,
                isCharging = false,
                rssi = -58,
                isBonded = true,
                isConnected = true,
                authKeyHex = "30743632303437346162386438343831",
                lastSyncTimestamp = System.currentTimeMillis()
            )
            dao.insertOrUpdateDevice(defaultDevice)

            // Initial Health Telemetry
            val defaultHealth = HealthMetricRecord(
                dateString = "Today",
                timestamp = System.currentTimeMillis(),
                steps = 7432,
                stepGoal = 10000,
                caloriesBurned = 412,
                calorieGoal = 550,
                distanceMeters = 5420,
                distanceGoalMeters = 7000,
                currentHeartRateBpm = 72,
                restingHeartRateBpm = 61,
                maxHeartRateBpm = 138,
                minHeartRateBpm = 54,
                spO2Percentage = 98,
                stressLevel = 28, // Mild/Relaxed
                paiScore = 84,
                totalSleepMinutes = 462, // 7h 42m
                deepSleepMinutes = 112,
                lightSleepMinutes = 240,
                remSleepMinutes = 88,
                awakeMinutes = 22
            )
            dao.insertHealthMetric(defaultHealth)

            // Initial Workouts
            val workouts = listOf(
                WorkoutRecord(
                    type = WorkoutType.OUTDOOR_RUN,
                    durationSeconds = 1845, // 30m 45s
                    distanceKm = 5.24f,
                    caloriesBurned = 360,
                    avgHeartRateBpm = 146,
                    maxHeartRateBpm = 168,
                    avgPacePerKm = "5'52\"",
                    timestamp = System.currentTimeMillis() - 86400000L
                ),
                WorkoutRecord(
                    type = WorkoutType.OUTDOOR_CYCLING,
                    durationSeconds = 2700, // 45m
                    distanceKm = 14.8f,
                    caloriesBurned = 425,
                    avgHeartRateBpm = 132,
                    maxHeartRateBpm = 154,
                    avgPacePerKm = "3'02\"",
                    timestamp = System.currentTimeMillis() - 172800000L
                ),
                WorkoutRecord(
                    type = WorkoutType.OUTDOOR_WALK,
                    durationSeconds = 2400, // 40m
                    distanceKm = 3.1f,
                    caloriesBurned = 190,
                    avgHeartRateBpm = 105,
                    maxHeartRateBpm = 120,
                    avgPacePerKm = "12'54\"",
                    timestamp = System.currentTimeMillis() - 259200000L
                )
            )
            workouts.forEach { dao.insertWorkout(it) }

            // Initial Curated Watch Faces for Bip Max
            val watchFaces = listOf(
                WatchFace(
                    id = "wf_cyber_matrix",
                    title = "Cyber Matrix HUD",
                    style = "Futuristic Digital",
                    author = "Amazfit Studio",
                    fileSizeKb = 342,
                    isInstalled = true,
                    isDefault = true,
                    primaryColorHex = "#38BDF8",
                    accentColorHex = "#34D399",
                    description = "High-contrast cybernetic digital HUD with live pulse animation, step arc gauge, and weather forecast icon."
                ),
                WatchFace(
                    id = "wf_sport_tri_ring",
                    title = "Bip Max Tri-Ring Pro",
                    style = "Fitness Activity",
                    author = "Zepp OS Labs",
                    fileSizeKb = 286,
                    isInstalled = false,
                    isDefault = false,
                    primaryColorHex = "#F87171",
                    accentColorHex = "#FBBF24",
                    description = "Three concentric activity rings displaying steps, calories, and active standing hours with bold numeric clock."
                ),
                WatchFace(
                    id = "wf_chrono_lux",
                    title = "Chrono Minimalist Dark",
                    style = "Classic Luxury",
                    author = "WatchCraft",
                    fileSizeKb = 412,
                    isInstalled = false,
                    isDefault = false,
                    primaryColorHex = "#F59E0B",
                    accentColorHex = "#E2E8F0",
                    description = "Elegant analog dial with textured guilloché sub-dials, date aperture at 3 o'clock, and battery indicator."
                ),
                WatchFace(
                    id = "wf_neon_aurora",
                    title = "Aurora AMOLED Flow",
                    style = "Gradient Modern",
                    author = "Zepp Creative",
                    fileSizeKb = 310,
                    isInstalled = false,
                    isDefault = false,
                    primaryColorHex = "#A855F7",
                    accentColorHex = "#06B6D4",
                    description = "Fluid animated northern lights wave background paired with ultra-clean bold typography and SpO2 readout."
                ),
                WatchFace(
                    id = "wf_pixel_retro",
                    title = "Retro 8-Bit Arcade",
                    style = "Pixel Art",
                    author = "GameBoyDev",
                    fileSizeKb = 195,
                    isInstalled = false,
                    isDefault = false,
                    primaryColorHex = "#10B981",
                    accentColorHex = "#EF4444",
                    description = "Charming 8-bit companion character who runs faster as your daily step count approaches the 10,000 goal."
                ),
                WatchFace(
                    id = "wf_tactical_military",
                    title = "Zepp Tactical Orange",
                    style = "Rugged Military",
                    author = "OutdoorTactics",
                    fileSizeKb = 378,
                    isInstalled = false,
                    isDefault = false,
                    primaryColorHex = "#F97316",
                    accentColorHex = "#EAB308",
                    description = "Military grade layout with high-visibility amber accents, barometric trend, compass heading, and sunrise/sunset."
                )
            )
            dao.insertWatchFaces(watchFaces)

            // Notification App Rules
            val notificationRules = listOf(
                AppNotificationRule("com.google.android.apps.messaging", "Messages / SMS", true, VibrationPattern.STANDARD),
                AppNotificationRule("com.whatsapp", "WhatsApp", true, VibrationPattern.DOUBLE),
                AppNotificationRule("org.telegram.messenger", "Telegram", true, VibrationPattern.SHORT),
                AppNotificationRule("com.google.android.gm", "Gmail", true, VibrationPattern.SHORT),
                AppNotificationRule("com.google.android.dialer", "Phone Calls", true, VibrationPattern.LONG),
                AppNotificationRule("com.slack", "Slack", false, VibrationPattern.STANDARD),
                AppNotificationRule("com.discord", "Discord", false, VibrationPattern.RAPID),
                AppNotificationRule("com.google.android.calendar", "Google Calendar", true, VibrationPattern.DOUBLE)
            )
            dao.insertNotificationRules(notificationRules)
        }
    }
}
