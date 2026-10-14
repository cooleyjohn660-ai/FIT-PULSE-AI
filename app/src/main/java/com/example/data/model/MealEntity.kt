package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val mealType: String, // "Breakfast", "Lunch", "Dinner", "Snack"
    val calories: Int,
    val proteinGrams: Float,
    val carbsGrams: Float,
    val fatGrams: Float,
    val portionDescription: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String // Format: YYYY-MM-DD
)
