package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AppNotificationRule
import com.example.data.model.BipDevice
import com.example.data.model.HealthMetricRecord
import com.example.data.model.WatchFace
import com.example.data.model.WorkoutRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface BipDao {

    // Device operations
    @Query("SELECT * FROM bip_devices LIMIT 1")
    fun getActiveDeviceFlow(): Flow<BipDevice?>

    @Query("SELECT * FROM bip_devices LIMIT 1")
    suspend fun getActiveDevice(): BipDevice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDevice(device: BipDevice)

    @Query("UPDATE bip_devices SET batteryPercent = :battery, isCharging = :isCharging, lastSyncTimestamp = :timestamp WHERE macAddress = :macAddress")
    suspend fun updateBattery(macAddress: String, battery: Int, isCharging: Boolean, timestamp: Long)

    @Query("UPDATE bip_devices SET isConnected = :isConnected WHERE macAddress = :macAddress")
    suspend fun updateConnectionStatus(macAddress: String, isConnected: Boolean)

    @Query("DELETE FROM bip_devices")
    suspend fun clearDevices()

    // Health metrics operations
    @Query("SELECT * FROM health_metrics ORDER BY timestamp DESC LIMIT 7")
    fun getRecentHealthMetricsFlow(): Flow<List<HealthMetricRecord>>

    @Query("SELECT * FROM health_metrics ORDER BY timestamp DESC LIMIT 1")
    fun getLatestHealthMetricFlow(): Flow<HealthMetricRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthMetric(metric: HealthMetricRecord)

    // Workout operations
    @Query("SELECT * FROM workouts ORDER BY timestamp DESC")
    fun getAllWorkoutsFlow(): Flow<List<WorkoutRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutRecord)

    // Watch faces operations
    @Query("SELECT * FROM watch_faces")
    fun getAllWatchFacesFlow(): Flow<List<WatchFace>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchFaces(watchFaces: List<WatchFace>)

    @Query("UPDATE watch_faces SET isInstalled = (id = :watchFaceId)")
    suspend fun setInstalledWatchFace(watchFaceId: String)

    // Notifications operations
    @Query("SELECT * FROM notification_configs")
    fun getAllNotificationRulesFlow(): Flow<List<AppNotificationRule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationRules(rules: List<AppNotificationRule>)

    @Update
    suspend fun updateNotificationRule(rule: AppNotificationRule)
}
