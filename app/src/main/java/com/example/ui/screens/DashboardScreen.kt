package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.MealEntity
import com.example.data.model.WorkoutEntity
import com.example.ui.FitnessViewModel
import com.example.ui.components.AddMealDialog
import com.example.ui.components.AddWorkoutDialog
import com.example.ui.components.MealItemCard
import com.example.ui.components.RealTimeMacroDashboardCard
import com.example.ui.components.WorkoutItemCard
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.CoralSecondary

@Composable
fun DashboardScreen(
    viewModel: FitnessViewModel,
    onNavigateToCoach: () -> Unit,
    modifier: Modifier = Modifier
) {
    val macroSummary by viewModel.macroSummary.collectAsStateWithLifecycle()
    val todayMeals by viewModel.todayMeals.collectAsStateWithLifecycle()
    val todayWorkouts by viewModel.todayWorkouts.collectAsStateWithLifecycle()
    val foodEstimate by viewModel.foodEstimate.collectAsStateWithLifecycle()
    val isEstimatingFood by viewModel.isEstimatingFood.collectAsStateWithLifecycle()

    var showAddMealDialog by remember { mutableStateOf(false) }
    var showAddWorkoutDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Hero Visual Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_banner_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_fitness),
                        contentDescription = "Fitness hero illustration",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(
                                        androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.3f),
                                        androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.85f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "FitPulse AI",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                        Text(
                            text = "Smart workouts & real-time macro balance",
                            fontSize = 12.sp,
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        // Real-Time Macro Dashboard
        item {
            RealTimeMacroDashboardCard(summary = macroSummary)
        }

        // Quick Action Chips / Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showAddMealDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_log_meal_button")
                ) {
                    Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log Meal", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { showAddWorkoutDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralSecondary),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_log_workout_button")
                ) {
                    Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log Workout", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // AI Coach Spotlight Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_coach_spotlight_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AI Fitness Assistant",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Need meal ideas or workout tweaks? Your coach is ready.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    FilledTonalButton(
                        onClick = onNavigateToCoach,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("ask_coach_button")
                    ) {
                        Text("Chat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Today's Workouts Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Workouts (${todayWorkouts.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (todayWorkouts.isNotEmpty()) {
                    Text(
                        text = "${todayWorkouts.sumOf { it.caloriesBurned }} kcal burned",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CoralSecondary
                    )
                }
            }
        }

        if (todayWorkouts.isEmpty()) {
            item {
                EmptyStateItem(
                    icon = Icons.AutoMirrored.Filled.DirectionsRun,
                    title = "No workouts logged today",
                    description = "Crush a session and log it to track burned calories!"
                )
            }
        } else {
            items(todayWorkouts, key = { "workout_${it.id}" }) { workout ->
                WorkoutItemCard(
                    workout = workout,
                    onDelete = { viewModel.deleteWorkout(workout) }
                )
            }
        }

        // Today's Meals Section Header
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Meals (${todayMeals.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (todayMeals.isNotEmpty()) {
                    Text(
                        text = "${todayMeals.sumOf { it.calories }} kcal total",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        if (todayMeals.isEmpty()) {
            item {
                EmptyStateItem(
                    icon = Icons.Default.Restaurant,
                    title = "No meals logged today",
                    description = "Log your breakfast, lunch, or snack to monitor macros."
                )
            }
        } else {
            items(todayMeals, key = { "meal_${it.id}" }) { meal ->
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

    if (showAddWorkoutDialog) {
        AddWorkoutDialog(
            onDismiss = { showAddWorkoutDialog = false },
            onSaveWorkout = { title, category, duration, calories, exercises, intensity, notes ->
                viewModel.logWorkout(title, category, duration, calories, exercises, intensity, notes)
            }
        )
    }
}

@Composable
fun EmptyStateItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
