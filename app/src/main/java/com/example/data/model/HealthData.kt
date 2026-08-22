package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_metrics")
data class HealthMetricRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateString: String, // e.g. "2026-08-22"
    val timestamp: Long = System.currentTimeMillis(),
    val steps: Int,
    val stepGoal: Int = 10000,
    val caloriesBurned: Int,
    val calorieGoal: Int = 500,
    val distanceMeters: Int,
    val distanceGoalMeters: Int = 6000,
    val currentHeartRateBpm: Int,
    val restingHeartRateBpm: Int,
    val maxHeartRateBpm: Int,
    val minHeartRateBpm: Int,
    val spO2Percentage: Int,
    val stressLevel: Int, // 0-100 (0-25 Relaxed, 26-50 Mild, 51-75 Medium, 76-100 High)
    val paiScore: Int,
    val totalSleepMinutes: Int,
    val deepSleepMinutes: Int,
    val lightSleepMinutes: Int,
    val remSleepMinutes: Int,
    val awakeMinutes: Int
)

data class HeartRatePoint(
    val hourMinute: String,
    val bpm: Int
)
