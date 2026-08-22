package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class WorkoutRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: WorkoutType,
    val durationSeconds: Long,
    val distanceKm: Float,
    val caloriesBurned: Int,
    val avgHeartRateBpm: Int,
    val maxHeartRateBpm: Int,
    val avgPacePerKm: String, // e.g. "5'24\""
    val timestamp: Long = System.currentTimeMillis()
)

enum class WorkoutType(val title: String, val iconName: String) {
    OUTDOOR_RUN("Outdoor Run", "directions_run"),
    OUTDOOR_WALK("Outdoor Walk", "directions_walk"),
    OUTDOOR_CYCLING("Outdoor Cycling", "directions_bike"),
    TREADMILL("Treadmill", "fitness_center"),
    POOL_SWIMMING("Pool Swimming", "pool"),
    FREESTYLE("Freestyle Fitness", "sports_gymnastics")
}
