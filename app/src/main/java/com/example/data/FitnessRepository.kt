package com.example.data

import com.example.data.dao.MealDao
import com.example.data.dao.WorkoutDao
import com.example.data.model.MealEntity
import com.example.data.model.UserGoals
import com.example.data.model.WorkoutEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FitnessRepository(
    private val mealDao: MealDao,
    private val workoutDao: WorkoutDao
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getTodayDateString(): String = dateFormat.format(Date())

    fun getMealsForDate(dateString: String): Flow<List<MealEntity>> {
        return mealDao.getMealsForDate(dateString)
    }

    fun getAllMeals(): Flow<List<MealEntity>> = mealDao.getAllMeals()

    fun getWorkoutsForDate(dateString: String): Flow<List<WorkoutEntity>> {
        return workoutDao.getWorkoutsForDate(dateString)
    }

    fun getAllWorkouts(): Flow<List<WorkoutEntity>> = workoutDao.getAllWorkouts()

    suspend fun insertMeal(meal: MealEntity): Long {
        return mealDao.insertMeal(meal)
    }

    suspend fun deleteMeal(meal: MealEntity) {
        mealDao.deleteMeal(meal)
    }

    suspend fun deleteMealById(id: Long) {
        mealDao.deleteMealById(id)
    }

    suspend fun insertWorkout(workout: WorkoutEntity): Long {
        return workoutDao.insertWorkout(workout)
    }

    suspend fun deleteWorkout(workout: WorkoutEntity) {
        workoutDao.deleteWorkout(workout)
    }

    suspend fun deleteWorkoutById(id: Long) {
        workoutDao.deleteWorkoutById(id)
    }

    suspend fun seedInitialDataIfEmpty() {
        val today = getTodayDateString()
        val currentMeals = mealDao.getAllMeals().first()
        if (currentMeals.isEmpty()) {
            // Seed a clean initial day
            mealDao.insertMeal(
                MealEntity(
                    name = "Protein Oatmeal with Blueberries & Chia",
                    mealType = "Breakfast",
                    calories = 420,
                    proteinGrams = 32f,
                    carbsGrams = 52f,
                    fatGrams = 9f,
                    portionDescription = "1 cup rolled oats, 1 scoop whey, 50g berries",
                    dateString = today
                )
            )
            mealDao.insertMeal(
                MealEntity(
                    name = "Grilled Chicken Quinoa Power Bowl",
                    mealType = "Lunch",
                    calories = 580,
                    proteinGrams = 48f,
                    carbsGrams = 54f,
                    fatGrams = 16f,
                    portionDescription = "180g chicken breast, 1 cup cooked quinoa, mixed greens & olive oil",
                    dateString = today
                )
            )
        }

        val currentWorkouts = workoutDao.getAllWorkouts().first()
        if (currentWorkouts.isEmpty()) {
            workoutDao.insertWorkout(
                WorkoutEntity(
                    title = "Upper Body Push & Core",
                    category = "Strength",
                    durationMinutes = 45,
                    caloriesBurned = 340,
                    exercisesSummary = "Barbell Bench Press (4x8), Overhead Dumbbell Press (3x10), Cable Flys (3x12), Plank (3x60s)",
                    intensity = "High",
                    notes = "Felt great, increased bench press weight on set 3.",
                    dateString = today
                )
            )
        }
    }
}
