package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.VibrationPattern
import com.example.data.model.WorkoutType

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return value?.joinToString(separator = "||") ?: ""
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        return value.split("||")
    }

    @TypeConverter
    fun fromWorkoutType(value: WorkoutType?): String {
        return value?.name ?: WorkoutType.OUTDOOR_RUN.name
    }

    @TypeConverter
    fun toWorkoutType(value: String?): WorkoutType {
        return try {
            if (value != null) WorkoutType.valueOf(value) else WorkoutType.OUTDOOR_RUN
        } catch (e: Exception) {
            WorkoutType.OUTDOOR_RUN
        }
    }

    @TypeConverter
    fun fromVibrationPattern(value: VibrationPattern?): String {
        return value?.name ?: VibrationPattern.STANDARD.name
    }

    @TypeConverter
    fun toVibrationPattern(value: String?): VibrationPattern {
        return try {
            if (value != null) VibrationPattern.valueOf(value) else VibrationPattern.STANDARD
        } catch (e: Exception) {
            VibrationPattern.STANDARD
        }
    }
}
