package com.example.data.model

data class UserGoals(
    val calorieTarget: Int = 2300,
    val proteinTargetGrams: Float = 160f,
    val carbsTargetGrams: Float = 240f,
    val fatTargetGrams: Float = 70f,
    val primaryGoal: String = "Lean Muscle", // "Lean Muscle", "Fat Loss", "Strength", "Endurance", "Maintenance"
    val dietType: String = "High Protein" // "High Protein", "Balanced", "Keto", "Plant-Based", "Mediterranean"
)

data class MacroSummary(
    val consumedCalories: Int = 0,
    val targetCalories: Int = 2300,
    val consumedProtein: Float = 0f,
    val targetProtein: Float = 160f,
    val consumedCarbs: Float = 0f,
    val targetCarbs: Float = 240f,
    val consumedFat: Float = 0f,
    val targetFat: Float = 70f,
    val burnedCalories: Int = 0
) {
    val remainingCalories: Int get() = (targetCalories - consumedCalories + burnedCalories).coerceAtLeast(0)
    val proteinProgress: Float get() = if (targetProtein > 0) (consumedProtein / targetProtein).coerceIn(0f, 1f) else 0f
    val carbsProgress: Float get() = if (targetCarbs > 0) (consumedCarbs / targetCarbs).coerceIn(0f, 1f) else 0f
    val fatProgress: Float get() = if (targetFat > 0) (consumedFat / targetFat).coerceIn(0f, 1f) else 0f
    val caloriesProgress: Float get() = if (targetCalories > 0) (consumedCalories.toFloat() / targetCalories).coerceIn(0f, 1.2f) else 0f
}
