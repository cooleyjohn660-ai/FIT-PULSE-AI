package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class SuggestedMeal(
    val mealType: String,
    val name: String,
    val portion: String,
    val calories: Int,
    val proteinGrams: Float,
    val carbsGrams: Float,
    val fatGrams: Float,
    val benefits: String
)

data class SuggestedWorkout(
    val title: String,
    val category: String,
    val durationMinutes: Int,
    val estimatedCalories: Int,
    val intensity: String,
    val exercises: List<String>,
    val tips: String
)

data class FoodEstimationResult(
    val foodName: String,
    val calories: Int,
    val proteinGrams: Float,
    val carbsGrams: Float,
    val fatGrams: Float,
    val suggestedMealType: String
)

class GeminiFitnessService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generatePersonalizedMealPlan(
        goal: String,
        dietType: String,
        calorieTarget: Int
    ): List<SuggestedMeal> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getFallbackMealPlan(goal, dietType, calorieTarget)
        }

        val prompt = """
            You are an elite sports nutritionist. Create a personalized 1-day meal plan for a client.
            Client Goal: $goal
            Dietary Style: $dietType
            Daily Calorie Target: $calorieTarget kcal
            
            Return STRICTLY a JSON array of 4 meals (Breakfast, Lunch, Dinner, Snack). Do not output markdown codeblocks, only valid JSON.
            Each object in the array must have:
            - "mealType": "Breakfast" / "Lunch" / "Dinner" / "Snack"
            - "name": string (dish name)
            - "portion": string (e.g. "1 bowl with 150g chicken...")
            - "calories": integer
            - "proteinGrams": number (grams)
            - "carbsGrams": number (grams)
            - "fatGrams": number (grams)
            - "benefits": string (1 short sentence explaining why it supports $goal)
        """.trimIndent()

        try {
            val responseText = executeGeminiPrompt(apiKey, prompt)
            parseMealPlanResponse(responseText, goal, dietType, calorieTarget)
        } catch (e: Exception) {
            Log.e("GeminiFitnessService", "Error calling Gemini API for meal plan", e)
            getFallbackMealPlan(goal, dietType, calorieTarget)
        }
    }

    suspend fun estimateFoodMacros(foodQuery: String): FoodEstimationResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getFallbackFoodEstimate(foodQuery)
        }

        val prompt = """
            You are a nutrition database. The user logged the following food in plain text:
            "$foodQuery"
            
            Estimate accurate calories and macronutrients for this food/meal.
            Return STRICTLY a JSON object. Do not output markdown codeblocks, only valid JSON:
            {
              "foodName": "Concise clean food name",
              "calories": integer,
              "proteinGrams": number,
              "carbsGrams": number,
              "fatGrams": number,
              "suggestedMealType": "Breakfast" | "Lunch" | "Dinner" | "Snack"
            }
        """.trimIndent()

        try {
            val responseText = executeGeminiPrompt(apiKey, prompt)
            parseFoodEstimationResponse(responseText, foodQuery)
        } catch (e: Exception) {
            Log.e("GeminiFitnessService", "Error estimating food macros", e)
            getFallbackFoodEstimate(foodQuery)
        }
    }

    suspend fun generateWorkoutRoutine(
        fitnessGoal: String,
        splitOrCategory: String,
        durationMinutes: Int
    ): SuggestedWorkout = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getFallbackWorkout(fitnessGoal, splitOrCategory, durationMinutes)
        }

        val prompt = """
            You are a certified strength and conditioning coach.
            Design a structured workout routine for:
            Goal: $fitnessGoal
            Focus/Category: $splitOrCategory
            Time: $durationMinutes minutes
            
            Return STRICTLY a JSON object without markdown code fences:
            {
              "title": "Inspiring routine title",
              "category": "$splitOrCategory",
              "durationMinutes": $durationMinutes,
              "estimatedCalories": integer (e.g. 250 - 500),
              "intensity": "Moderate" or "High",
              "exercises": ["Exercise 1 (Sets x Reps)", "Exercise 2 (Sets x Reps)", "Exercise 3 (Sets x Reps)", "Exercise 4 (Sets x Reps)"],
              "tips": "Brief key coaching cue or warm-up tip"
            }
        """.trimIndent()

        try {
            val responseText = executeGeminiPrompt(apiKey, prompt)
            parseWorkoutResponse(responseText, fitnessGoal, splitOrCategory, durationMinutes)
        } catch (e: Exception) {
            Log.e("GeminiFitnessService", "Error generating workout routine", e)
            getFallbackWorkout(fitnessGoal, splitOrCategory, durationMinutes)
        }
    }

    suspend fun askFitnessCoach(userMessage: String, contextInfo: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getFallbackCoachReply(userMessage)
        }

        val prompt = """
            You are FitPulse AI, an empathetic, energizing, and scientifically grounded fitness & nutrition coach.
            Current user state today:
            $contextInfo
            
            User message: "$userMessage"
            
            Guidelines:
            - Give direct, highly actionable, concise advice (2 to 3 paragraphs max).
            - Highlight practical workout or nutrition adjustments.
            - Keep a supportive, positive, and motivating tone.
        """.trimIndent()

        try {
            executeGeminiPrompt(apiKey, prompt)
        } catch (e: Exception) {
            Log.e("GeminiFitnessService", "Error in fitness coach chat", e)
            getFallbackCoachReply(userMessage)
        }
    }

    private fun executeGeminiPrompt(apiKey: String, promptText: String): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val requestBodyJson = JSONObject().apply {
            val contents = JSONArray().apply {
                val content = JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().apply { put("text", promptText) })
                    }
                    put("parts", parts)
                }
                put(content)
            }
            put("contents", contents)
            val generationConfig = JSONObject().apply {
                put("temperature", 0.6)
            }
            put("generationConfig", generationConfig)
        }

        val request = Request.Builder()
            .url(url)
            .post(requestBodyJson.toString().toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                throw RuntimeException("Gemini API HTTP ${response.code}: $errBody")
            }
            val responseBody = response.body?.string() ?: throw RuntimeException("Empty response body")
            val root = JSONObject(responseBody)
            val candidates = root.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val contentObj = firstCandidate.optJSONObject("content")
                val parts = contentObj?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    return parts.getJSONObject(0).optString("text", "")
                }
            }
            return ""
        }
    }

    private fun cleanJsonText(raw: String): String {
        var text = raw.trim()
        if (text.startsWith("```json")) {
            text = text.removePrefix("```json").trim()
        } else if (text.startsWith("```")) {
            text = text.removePrefix("```").trim()
        }
        if (text.endsWith("```")) {
            text = text.removeSuffix("```").trim()
        }
        return text
    }

    private fun parseMealPlanResponse(
        rawText: String,
        goal: String,
        dietType: String,
        calorieTarget: Int
    ): List<SuggestedMeal> {
        return try {
            val cleaned = cleanJsonText(rawText)
            val jsonArray = JSONArray(cleaned)
            val list = mutableListOf<SuggestedMeal>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    SuggestedMeal(
                        mealType = obj.optString("mealType", "Meal"),
                        name = obj.optString("name", "Nutritious Meal"),
                        portion = obj.optString("portion", "1 balanced serving"),
                        calories = obj.optInt("calories", 400),
                        proteinGrams = obj.optDouble("proteinGrams", 30.0).toFloat(),
                        carbsGrams = obj.optDouble("carbsGrams", 40.0).toFloat(),
                        fatGrams = obj.optDouble("fatGrams", 12.0).toFloat(),
                        benefits = obj.optString("benefits", "Optimized for your fitness goal.")
                    )
                )
            }
            if (list.isNotEmpty()) list else getFallbackMealPlan(goal, dietType, calorieTarget)
        } catch (e: Exception) {
            Log.e("GeminiFitnessService", "Failed to parse meal plan JSON: $rawText", e)
            getFallbackMealPlan(goal, dietType, calorieTarget)
        }
    }

    private fun parseFoodEstimationResponse(rawText: String, originalQuery: String): FoodEstimationResult {
        return try {
            val cleaned = cleanJsonText(rawText)
            val obj = JSONObject(cleaned)
            FoodEstimationResult(
                foodName = obj.optString("foodName", originalQuery.replaceFirstChar { it.uppercase() }),
                calories = obj.optInt("calories", 250),
                proteinGrams = obj.optDouble("proteinGrams", 15.0).toFloat(),
                carbsGrams = obj.optDouble("carbsGrams", 25.0).toFloat(),
                fatGrams = obj.optDouble("fatGrams", 8.0).toFloat(),
                suggestedMealType = obj.optString("suggestedMealType", "Lunch")
            )
        } catch (e: Exception) {
            getFallbackFoodEstimate(originalQuery)
        }
    }

    private fun parseWorkoutResponse(
        rawText: String,
        fitnessGoal: String,
        splitOrCategory: String,
        durationMinutes: Int
    ): SuggestedWorkout {
        return try {
            val cleaned = cleanJsonText(rawText)
            val obj = JSONObject(cleaned)
            val exercisesArray = obj.optJSONArray("exercises")
            val exercises = mutableListOf<String>()
            if (exercisesArray != null) {
                for (i in 0 until exercisesArray.length()) {
                    exercises.add(exercisesArray.getString(i))
                }
            }
            SuggestedWorkout(
                title = obj.optString("title", "$splitOrCategory Power Session"),
                category = obj.optString("category", splitOrCategory),
                durationMinutes = obj.optInt("durationMinutes", durationMinutes),
                estimatedCalories = obj.optInt("estimatedCalories", durationMinutes * 7),
                intensity = obj.optString("intensity", "Moderate"),
                exercises = if (exercises.isNotEmpty()) exercises else listOf(
                    "Primary Compound Lift (4x8)",
                    "Secondary Movement (3x10)",
                    "Isolation Accessory (3x12)",
                    "Core Finisher (3x15)"
                ),
                tips = obj.optString("tips", "Focus on controlled eccentrics and steady breathing.")
            )
        } catch (e: Exception) {
            getFallbackWorkout(fitnessGoal, splitOrCategory, durationMinutes)
        }
    }

    // Fallbacks ensuring 100% offline & instantaneous responsiveness
    private fun getFallbackMealPlan(goal: String, dietType: String, calorieTarget: Int): List<SuggestedMeal> {
        val isFatLoss = goal.contains("Fat", ignoreCase = true)
        val isMuscle = goal.contains("Muscle", ignoreCase = true) || goal.contains("Strength", ignoreCase = true)

        return listOf(
            SuggestedMeal(
                mealType = "Breakfast",
                name = if (dietType.contains("Plant", ignoreCase = true)) "High-Protein Chia & Tofu Scramble" else "Egg White Omelet with Spinach & Avocado Toast",
                portion = "3 eggs / scramble, 1 slice sourdough, 1/4 avocado",
                calories = if (isFatLoss) 380 else 480,
                proteinGrams = if (isMuscle) 38f else 30f,
                carbsGrams = 35f,
                fatGrams = 14f,
                benefits = "High bioavailability protein to initiate muscle protein synthesis early in the day."
            ),
            SuggestedMeal(
                mealType = "Lunch",
                name = if (dietType.contains("Plant", ignoreCase = true)) "Edamame & Tempeh Quinoa Bowl" else "Grilled Herb Chicken Breast & Sweet Potato Wedges",
                portion = "180g protein, 150g roasted sweet potato, steamed broccoli",
                calories = if (isFatLoss) 520 else 640,
                proteinGrams = if (isMuscle) 48f else 40f,
                carbsGrams = 52f,
                fatGrams = 15f,
                benefits = "Complex carbohydrates and lean protein for sustained afternoon stamina."
            ),
            SuggestedMeal(
                mealType = "Dinner",
                name = if (dietType.contains("Plant", ignoreCase = true)) "Lentil Dahl with Brown Basmati & Greens" else "Baked Wild Salmon Fillet with Asparagus & Jasmine Rice",
                portion = "170g salmon fillet, 1 cup asparagus, 120g cooked rice",
                calories = if (isFatLoss) 480 else 610,
                proteinGrams = if (isMuscle) 42f else 35f,
                carbsGrams = 42f,
                fatGrams = 18f,
                benefits = "Rich in omega-3 fatty acids and zinc for overnight cellular recovery and joint support."
            ),
            SuggestedMeal(
                mealType = "Snack",
                name = "Greek Yogurt Power Parfait with Almonds & Berries",
                portion = "200g Greek yogurt (0%), 15g raw almonds, 40g fresh berries",
                calories = 220,
                proteinGrams = 22f,
                carbsGrams = 18f,
                fatGrams = 6f,
                benefits = "Slow-digesting casein protein supporting recovery between workout windows."
            )
        )
    }

    private fun getFallbackFoodEstimate(query: String): FoodEstimationResult {
        val lower = query.lowercase()
        return when {
            lower.contains("egg") -> FoodEstimationResult(
                foodName = "Whole Eggs & Toast",
                calories = 310,
                proteinGrams = 18f,
                carbsGrams = 22f,
                fatGrams = 14f,
                suggestedMealType = "Breakfast"
            )
            lower.contains("chicken") || lower.contains("salad") -> FoodEstimationResult(
                foodName = "Grilled Chicken Salad",
                calories = 420,
                proteinGrams = 42f,
                carbsGrams = 15f,
                fatGrams = 18f,
                suggestedMealType = "Lunch"
            )
            lower.contains("shake") || lower.contains("protein") || lower.contains("whey") -> FoodEstimationResult(
                foodName = "Protein Shake & Banana",
                calories = 260,
                proteinGrams = 28f,
                carbsGrams = 30f,
                fatGrams = 3f,
                suggestedMealType = "Snack"
            )
            lower.contains("steak") || lower.contains("beef") -> FoodEstimationResult(
                foodName = "Lean Beef Steak & Potatoes",
                calories = 580,
                proteinGrams = 46f,
                carbsGrams = 40f,
                fatGrams = 22f,
                suggestedMealType = "Dinner"
            )
            lower.contains("oat") || lower.contains("porridge") -> FoodEstimationResult(
                foodName = "Oatmeal with Honey & Nuts",
                calories = 340,
                proteinGrams = 12f,
                carbsGrams = 52f,
                fatGrams = 9f,
                suggestedMealType = "Breakfast"
            )
            else -> FoodEstimationResult(
                foodName = query.trim().replaceFirstChar { it.uppercase() },
                calories = 350,
                proteinGrams = 22f,
                carbsGrams = 35f,
                fatGrams = 11f,
                suggestedMealType = "Lunch"
            )
        }
    }

    private fun getFallbackWorkout(
        goal: String,
        category: String,
        duration: Int
    ): SuggestedWorkout {
        val exercises = when (category) {
            "Strength" -> listOf(
                "Barbell Bench Press (4 sets x 8-10 reps)",
                "Incline Dumbbell Press (3 sets x 10 reps)",
                "Dumbbell Lateral Raises (3 sets x 15 reps)",
                "Triceps Rope Pushdowns (3 sets x 12 reps)",
                "Hanging Leg Raises (3 sets x 15 reps)"
            )
            "Cardio" -> listOf(
                "5-min Dynamic Mobility Warmup",
                "20-min Zone 2 Aerobic Tempo Run/Cycle",
                "5 x 30s High Incline Power Sprints",
                "5-min Cool-down Walk & Calf Stretches"
            )
            "HIIT" -> listOf(
                "Kettlebell Swings (40s on / 20s rest x 4)",
                "Plyometric Jump Squats (40s on / 20s rest x 4)",
                "Burpee to Overhead Press (40s on / 20s rest x 4)",
                "Mountain Climbers & Hollow Hold (4 sets)"
            )
            else -> listOf(
                "Goblet Squats (3 sets x 12 reps)",
                "Push-Ups (3 sets to 2 reps in reserve)",
                "Single-Arm Dumbbell Rows (3 sets x 10 reps each)",
                "Walking Lunges (3 sets x 12 steps)",
                "Plank Hold (3 sets x 45 seconds)"
            )
        }

        return SuggestedWorkout(
            title = "$category Precision Session",
            category = category,
            durationMinutes = duration,
            estimatedCalories = duration * 8,
            intensity = "High",
            exercises = exercises,
            tips = "Prioritize form over load. Keep 1-2 minutes rest between working compound sets."
        )
    }

    private fun getFallbackCoachReply(query: String): String {
        val lower = query.lowercase()
        return when {
            lower.contains("protein") -> """
                For optimal muscle repair and growth, aim for 1.6 to 2.2 grams of protein per kilogram of bodyweight (approx 0.8–1.0g per pound).
                
                Distribute this across 3–4 meals containing at least 25–40g of protein each to trigger muscle protein synthesis regularly throughout the day!
            """.trimIndent()
            lower.contains("macro") || lower.contains("calorie") -> """
                Consistency with your energy balance is key! If your goal is lean muscle, stay in a slight surplus (+200-300 kcal) with high protein. If fat loss is your priority, maintain a moderate 300-500 kcal deficit while keeping protein high to preserve lean mass.
                
                Check your Real-time Macro rings on the dashboard to see exactly what you have left for today.
            """.trimIndent()
            lower.contains("workout") || lower.contains("exercise") -> """
                Focus on progressive overload: tracking weights, reps, and sets each week. Even adding one rep or a 1% weight increase will drive continuous adaptations.
                
                Don't forget adequate sleep—deep sleep is when growth hormone and muscular repair peak!
            """.trimIndent()
            else -> """
                You're making great progress towards your fitness goals! Keep logging your meals and workouts consistently in real-time.
                
                Every session and every balanced meal builds momentum. Let me know if you want a custom meal suggestion or tailored workout split!
            """.trimIndent()
        }
    }
}
