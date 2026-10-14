package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.FitnessViewModel
import com.example.ui.components.AddMealDialog
import com.example.ui.components.MealItemCard
import com.example.ui.components.SuggestedMealCard
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MacroCarbsColor
import com.example.ui.theme.MacroFatColor
import com.example.ui.theme.MacroProteinColor

@Composable
fun MealPlannerScreen(
    viewModel: FitnessViewModel,
    modifier: Modifier = Modifier
) {
    val macroSummary by viewModel.macroSummary.collectAsStateWithLifecycle()
    val todayMeals by viewModel.todayMeals.collectAsStateWithLifecycle()
    val suggestedPlan by viewModel.suggestedMealPlan.collectAsStateWithLifecycle()
    val isGeneratingPlan by viewModel.isGeneratingMealPlan.collectAsStateWithLifecycle()
    val foodEstimate by viewModel.foodEstimate.collectAsStateWithLifecycle()
    val isEstimatingFood by viewModel.isEstimatingFood.collectAsStateWithLifecycle()
    val userGoals by viewModel.userGoals.collectAsStateWithLifecycle()

    var showAddMealDialog by remember { mutableStateOf(false) }

    var selectedGoal by remember { mutableStateOf(userGoals.primaryGoal) }
    var selectedDiet by remember { mutableStateOf(userGoals.dietType) }
    var selectedCalorieTarget by remember { mutableIntStateOf(userGoals.calorieTarget) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Quick Macro Intake Summary Header
            Card(
                modifier = Modifier.fillMaxWidth().testTag("nutrition_status_banner"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Real-Time Nutrition Budget",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${macroSummary.remainingCalories} kcal left for today",
                                style = MaterialTheme.typography.bodySmall,
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Button(
                            onClick = { showAddMealDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            modifier = Modifier.testTag("log_meal_planner_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Log Food", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MacroNutrientMini(
                            label = "Protein",
                            consumed = "${macroSummary.consumedProtein.toInt()}g",
                            target = "${macroSummary.targetProtein.toInt()}g",
                            color = MacroProteinColor
                        )
                        MacroNutrientMini(
                            label = "Carbs",
                            consumed = "${macroSummary.consumedCarbs.toInt()}g",
                            target = "${macroSummary.targetCarbs.toInt()}g",
                            color = MacroCarbsColor
                        )
                        MacroNutrientMini(
                            label = "Fat",
                            consumed = "${macroSummary.consumedFat.toInt()}g",
                            target = "${macroSummary.targetFat.toInt()}g",
                            color = MacroFatColor
                        )
                        MacroNutrientMini(
                            label = "Calories",
                            consumed = "${macroSummary.consumedCalories}",
                            target = "${macroSummary.targetCalories}",
                            color = AmberAccent
                        )
                    }
                }
            }
        }

        // AI Personalized Meal Plan Generator
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_meal_planner_generator_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Personalized AI Meal Plan",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Formulated around your targets & dietary style",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Fitness Objective:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Lean Muscle", "Fat Loss", "Maintenance").forEach { goal ->
                            FilterChip(
                                selected = selectedGoal == goal,
                                onClick = { selectedGoal = goal },
                                label = { Text(goal, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Dietary Preference:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("High Protein", "Balanced", "Keto", "Plant-Based").forEach { diet ->
                            FilterChip(
                                selected = selectedDiet == diet,
                                onClick = { selectedDiet = diet },
                                label = { Text(diet, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Daily Target: $selectedCalorieTarget kcal", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(1800, 2100, 2400, 2800).forEach { cals ->
                            FilterChip(
                                selected = selectedCalorieTarget == cals,
                                onClick = { selectedCalorieTarget = cals },
                                label = { Text("${cals} kcal", fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            viewModel.generateMealPlan(selectedGoal, selectedDiet, selectedCalorieTarget)
                        },
                        enabled = !isGeneratingPlan,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("generate_meal_plan_button")
                    ) {
                        if (isGeneratingPlan) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generating Personalized Meal Plan...")
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Generate Personalized Meal Plan", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Suggested Meals from AI
        if (suggestedPlan.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI Suggested Daily Meals (${suggestedPlan.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "1-Tap to Log",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                }
            }

            items(suggestedPlan, key = { "suggested_${it.mealType}_${it.name}" }) { suggestedMeal ->
                SuggestedMealCard(
                    suggested = suggestedMeal,
                    onLogMeal = {
                        viewModel.logSuggestedMeal(suggestedMeal)
                    }
                )
            }
        }

        // Today's Meals Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Today's Logged Meals (${todayMeals.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (todayMeals.isEmpty()) {
            item {
                EmptyStateItem(
                    icon = Icons.Default.Restaurant,
                    title = "No meals logged yet today",
                    description = "Use the AI generator above or tap '+ Log Food' to track your intake."
                )
            }
        } else {
            items(todayMeals, key = { "today_meal_${it.id}" }) { meal ->
                MealItemCard(
                    meal = meal,
                    onDelete = { viewModel.deleteMeal(meal) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showAddMealDialog) {
        AddMealDialog(
            onDismiss = {
                showAddMealDialog = false
                viewModel.clearFoodEstimate()
            },
            onSaveMeal = { name, mealType, calories, p, c, f, portion ->
                viewModel.logMeal(name, mealType, calories, p, c, f, portion)
            },
            onEstimateAi = { query ->
                viewModel.estimateFood(query)
            },
            aiEstimate = foodEstimate,
            isEstimating = isEstimatingFood
        )
    }
}

@Composable
fun MacroNutrientMini(
    label: String,
    consumed: String,
    target: String,
    color: Color
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(consumed, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
        Text("of $target", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
