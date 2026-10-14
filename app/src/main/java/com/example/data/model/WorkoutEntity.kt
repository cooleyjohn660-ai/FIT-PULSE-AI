package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String, // "Strength", "Cardio", "HIIT", "Flexibility", "Full Body"
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val exercisesSummary: String = "",
    val intensity: String = "Moderate", // "Light", "Moderate", "High"
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String // Format: YYYY-MM-DD
)
