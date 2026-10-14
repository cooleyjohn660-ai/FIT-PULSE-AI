package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.FitnessRepository
import com.example.data.ai.FoodEstimationResult
import com.example.data.ai.GeminiFitnessService
import com.example.data.ai.SuggestedMeal
import com.example.data.ai.SuggestedWorkout
import com.example.data.model.MealEntity
import com.example.data.model.MacroSummary
import com.example.data.model.UserGoals
import com.example.data.model.WorkoutEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "user" or "coach"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class FitnessViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FitnessRepository
    private val aiService = GeminiFitnessService()

    private val _currentDate = MutableStateFlow("")
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    private val _userGoals = MutableStateFlow(UserGoals())
    val userGoals: StateFlow<UserGoals> = _userGoals.asStateFlow()

    // Reactive lists from Room
    val todayMeals: StateFlow<List<MealEntity>>
    val todayWorkouts: StateFlow<List<WorkoutEntity>>
    val allWorkouts: StateFlow<List<WorkoutEntity>>

    // AI state
    private val _suggestedMealPlan = MutableStateFlow<List<SuggestedMeal>>(emptyList())
    val suggestedMealPlan: StateFlow<List<SuggestedMeal>> = _suggestedMealPlan.asStateFlow()

    private val _isGeneratingMealPlan = MutableStateFlow(false)
    val isGeneratingMealPlan: StateFlow<Boolean> = _isGeneratingMealPlan.asStateFlow()

    private val _suggestedWorkout = MutableStateFlow<SuggestedWorkout?>(null)
    val suggestedWorkout: StateFlow<SuggestedWorkout?> = _suggestedWorkout.asStateFlow()

    private val _isGeneratingWorkout = MutableStateFlow(false)
    val isGeneratingWorkout: StateFlow<Boolean> = _isGeneratingWorkout.asStateFlow()

    private val _foodEstimate = MutableStateFlow<FoodEstimationResult?>(null)
    val foodEstimate: StateFlow<FoodEstimationResult?> = _foodEstimate.asStateFlow()

    private val _isEstimatingFood = MutableStateFlow(false)
    val isEstimatingFood: StateFlow<Boolean> = _isEstimatingFood.asStateFlow()

    private val _aiChatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "coach",
                text = "Hey there! I'm your FitPulse AI Coach. I'm here to analyze your workouts, balance your macros in real-time, and suggest customized meal strategies. What's on your mind today?"
            )
        )
    )
    val aiChatMessages: StateFlow<List<ChatMessage>> = _aiChatMessages.asStateFlow()

    private val _isCoachThinking = MutableStateFlow(false)
    val isCoachThinking: StateFlow<Boolean> = _isCoachThinking.asStateFlow()

    // Real-time macro summary reactively computed from meals, workouts, and goals
    val macroSummary: StateFlow<MacroSummary>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = FitnessRepository(db.mealDao(), db.workoutDao())
        val today = repository.getTodayDateString()
        _currentDate.value = today

        todayMeals = repository.getMealsForDate(today)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        todayWorkouts = repository.getWorkoutsForDate(today)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allWorkouts = repository.getAllWorkouts()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        macroSummary = combine(todayMeals, todayWorkouts, _userGoals) { meals, workouts, goals ->
            val totalCalories = meals.sumOf { it.calories }
            val totalProtein = meals.sumOf { it.proteinGrams.toDouble() }.toFloat()
            val totalCarbs = meals.sumOf { it.carbsGrams.toDouble() }.toFloat()
            val totalFat = meals.sumOf { it.fatGrams.toDouble() }.toFloat()
            val totalBurned = workouts.sumOf { it.caloriesBurned }

            MacroSummary(
                consumedCalories = totalCalories,
                targetCalories = goals.calorieTarget,
                consumedProtein = totalProtein,
                targetProtein = goals.proteinTargetGrams,
                consumedCarbs = totalCarbs,
                targetCarbs = goals.carbsTargetGrams,
                consumedFat = totalFat,
                targetFat = goals.fatTargetGrams,
                burnedCalories = totalBurned
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MacroSummary())

        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
            // Auto generate initial meal plan
            generateMealPlan(goals = _userGoals.value.primaryGoal, diet = _userGoals.value.dietType, targetCalories = _userGoals.value.calorieTarget)
        }
    }

    fun logMeal(
        name: String,
        mealType: String,
        calories: Int,
        protein: Float,
        carbs: Float,
        fat: Float,
        portion: String = ""
    ) {
        viewModelScope.launch {
            val meal = MealEntity(
                name = name,
                mealType = mealType,
                calories = calories,
                proteinGrams = protein,
                carbsGrams = carbs,
                fatGrams = fat,
                portionDescription = portion,
                dateString = _currentDate.value
            )
            repository.insertMeal(meal)
        }
    }

    fun deleteMeal(meal: MealEntity) {
        viewModelScope.launch {
            repository.deleteMeal(meal)
        }
    }

    fun logWorkout(
        title: String,
        category: String,
        durationMinutes: Int,
        caloriesBurned: Int,
        exercises: String = "",
        intensity: String = "Moderate",
        notes: String = ""
    ) {
        viewModelScope.launch {
            val workout = WorkoutEntity(
                title = title,
                category = category,
                durationMinutes = durationMinutes,
                caloriesBurned = caloriesBurned,
                exercisesSummary = exercises,
                intensity = intensity,
                notes = notes,
                dateString = _currentDate.value
            )
            repository.insertWorkout(workout)
        }
    }

    fun deleteWorkout(workout: WorkoutEntity) {
        viewModelScope.launch {
            repository.deleteWorkout(workout)
        }
    }

    fun generateMealPlan(goals: String, diet: String, targetCalories: Int) {
        viewModelScope.launch {
            _isGeneratingMealPlan.value = true
            val plan = aiService.generatePersonalizedMealPlan(goals, diet, targetCalories)
            _suggestedMealPlan.value = plan
            _isGeneratingMealPlan.value = false
        }
    }

    fun logSuggestedMeal(suggested: SuggestedMeal) {
        logMeal(
            name = suggested.name,
            mealType = suggested.mealType,
            calories = suggested.calories,
            protein = suggested.proteinGrams,
            carbs = suggested.carbsGrams,
            fat = suggested.fatGrams,
            portion = suggested.portion
        )
    }

    fun generateWorkout(goal: String, category: String, durationMinutes: Int) {
        viewModelScope.launch {
            _isGeneratingWorkout.value = true
            val workout = aiService.generateWorkoutRoutine(goal, category, durationMinutes)
            _suggestedWorkout.value = workout
            _isGeneratingWorkout.value = false
        }
    }

    fun logSuggestedWorkout(suggested: SuggestedWorkout) {
        logWorkout(
            title = suggested.title,
            category = suggested.category,
            durationMinutes = suggested.durationMinutes,
            caloriesBurned = suggested.estimatedCalories,
            exercises = suggested.exercises.joinToString(", "),
            intensity = suggested.intensity,
            notes = suggested.tips
        )
        _suggestedWorkout.value = null
    }

    fun estimateFood(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _isEstimatingFood.value = true
            val res = aiService.estimateFoodMacros(query)
            _foodEstimate.value = res
            _isEstimatingFood.value = false
        }
    }

    fun clearFoodEstimate() {
        _foodEstimate.value = null
    }

    fun sendCoachMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(sender = "user", text = text)
        _aiChatMessages.value = _aiChatMessages.value + userMsg

        viewModelScope.launch {
            _isCoachThinking.value = true
            val currentSummary = macroSummary.value
            val contextInfo = """
                Goal: ${_userGoals.value.primaryGoal} (${_userGoals.value.dietType})
                Calories: ${currentSummary.consumedCalories} / ${currentSummary.targetCalories} kcal (Remaining: ${currentSummary.remainingCalories})
                Protein: ${currentSummary.consumedProtein.toInt()}g / ${currentSummary.targetProtein.toInt()}g
                Carbs: ${currentSummary.consumedCarbs.toInt()}g / ${currentSummary.targetCarbs.toInt()}g
                Fat: ${currentSummary.consumedFat.toInt()}g / ${currentSummary.targetFat.toInt()}g
                Calories Burned: ${currentSummary.burnedCalories} kcal
            """.trimIndent()

            val reply = aiService.askFitnessCoach(text, contextInfo)
            val coachMsg = ChatMessage(sender = "coach", text = reply)
            _aiChatMessages.value = _aiChatMessages.value + coachMsg
            _isCoachThinking.value = false
        }
    }

    fun updateGoals(goals: UserGoals) {
        _userGoals.value = goals
    }
}
