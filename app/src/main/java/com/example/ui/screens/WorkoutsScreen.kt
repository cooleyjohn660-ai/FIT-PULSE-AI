package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ads.AdManager
import com.example.ads.findActivity
import com.example.ui.FitnessViewModel
import com.example.ui.components.AddWorkoutDialog
import com.example.ui.components.SuggestedWorkoutCard
import com.example.ui.components.WorkoutItemCard
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.CoralSecondary
import com.example.ui.theme.EmeraldPrimary

data class QuickWorkoutPreset(
    val title: String,
    val category: String,
    val duration: Int,
    val calories: Int,
    val exercises: String,
    val intensity: String
)

@Composable
fun WorkoutsScreen(
    viewModel: FitnessViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context.findActivity()

    val todayWorkouts by viewModel.todayWorkouts.collectAsStateWithLifecycle()
    val allWorkouts by viewModel.allWorkouts.collectAsStateWithLifecycle()
    val suggestedWorkout by viewModel.suggestedWorkout.collectAsStateWithLifecycle()
    val isGeneratingWorkout by viewModel.isGeneratingWorkout.collectAsStateWithLifecycle()
    val userGoals by viewModel.userGoals.collectAsStateWithLifecycle()

    var showAddWorkoutDialog by remember { mutableStateOf(false) }
    var showAiGeneratorSection by remember { mutableStateOf(true) }

    var selectedGoal by remember { mutableStateOf(userGoals.primaryGoal) }
    var selectedCategory by remember { mutableStateOf("Strength") }
    var selectedDuration by remember { mutableIntStateOf(45) }

    val presets = listOf(
        QuickWorkoutPreset(
            title = "Chest & Triceps Push",
            category = "Strength",
            duration = 45,
            calories = 320,
            exercises = "Bench Press (4x8), Incline Dumbbell (3x10), Triceps Pushdown (3x12)",
            intensity = "High"
        ),
        QuickWorkoutPreset(
            title = "Back & Biceps Pull",
            category = "Strength",
            duration = 45,
            calories = 310,
            exercises = "Lat Pulldown (4x10), Barbell Row (3x8), Biceps Curl (3x12)",
            intensity = "High"
        ),
        QuickWorkoutPreset(
            title = "Leg Day Quad & Glute Blast",
            category = "Strength",
            duration = 50,
            calories = 380,
            exercises = "Barbell Squats (4x8), Romanian Deadlift (3x10), Leg Press (3x12)",
            intensity = "High"
        ),
        QuickWorkoutPreset(
            title = "20-Min Tabata Fat Burner",
            category = "HIIT",
            duration = 20,
            calories = 220,
            exercises = "Burpees, High Knees, Mountain Climbers, Jump Squats",
            intensity = "High"
        ),
        QuickWorkoutPreset(
            title = "Zone 2 Outdoor 5K Run",
            category = "Cardio",
            duration = 30,
            calories = 290,
            exercises = "5km steady tempo running, 5min warm-up walk",
            intensity = "Moderate"
        )
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Workouts Stat Banner
            Card(
                modifier = Modifier.fillMaxWidth().testTag("workout_summary_banner"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WorkoutStatColumn(
                        icon = Icons.Default.Timer,
                        value = "${todayWorkouts.sumOf { it.durationMinutes }}m",
                        label = "Trained Today",
                        tint = BlueAccent
                    )
                    Box(modifier = Modifier.width(1.dp).height(40.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)))
                    WorkoutStatColumn(
                        icon = Icons.Default.LocalFireDepartment,
                        value = "${todayWorkouts.sumOf { it.caloriesBurned }}",
                        label = "Kcal Burned",
                        tint = CoralSecondary
                    )
                    Box(modifier = Modifier.width(1.dp).height(40.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)))
                    WorkoutStatColumn(
                        icon = Icons.Default.FitnessCenter,
                        value = "${todayWorkouts.size}",
                        label = "Sessions",
                        tint = EmeraldPrimary
                    )
                }
            }
        }

        // Action Buttons Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Workout Routines",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Button(
                    onClick = { showAddWorkoutDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralSecondary),
                    modifier = Modifier.testTag("log_custom_workout_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log Workout", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // AI Workout Generator Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_workout_generator_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CoralSecondary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = CoralSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "AI Workout Generator",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Tailored to your goal & time",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Goal:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Lean Muscle", "Fat Loss", "Strength").forEach { goal ->
                            FilterChip(
                                selected = selectedGoal == goal,
                                onClick = { selectedGoal = goal },
                                label = { Text(goal, fontSize = 11.sp) },
                                modifier = Modifier.testTag("ai_goal_$goal")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Category / Split:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Strength", "Cardio", "HIIT", "Full Body").forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) },
                                modifier = Modifier.testTag("ai_cat_$cat")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Duration: $selectedDuration minutes", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(20, 30, 45, 60).forEach { mins ->
                            FilterChip(
                                selected = selectedDuration == mins,
                                onClick = { selectedDuration = mins },
                                label = { Text("${mins}m", fontSize = 11.sp) },
                                modifier = Modifier.testTag("ai_duration_$mins")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            viewModel.generateWorkout(selectedGoal, selectedCategory, selectedDuration)
                        },
                        enabled = !isGeneratingWorkout,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CoralSecondary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("generate_workout_button")
                    ) {
                        if (isGeneratingWorkout) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generating Personalized Routine...")
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Generate AI Workout Routine", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Show AI generated workout if present
        suggestedWorkout?.let { routine ->
            item {
                SuggestedWorkoutCard(
                    suggested = routine,
                    onLogWorkout = {
                        viewModel.logSuggestedWorkout(routine)
                    }
                )
            }
        }

        // Rewarded Ad Feature: Elite Pro AI Routine
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rewarded_pro_workout_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AmberAccent.copy(alpha = 0.12f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AmberAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Pro Elite AI Workout Split",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Watch a rewarded video ad to unlock a personalized 60m routine",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            activity?.let { act ->
                                AdManager.showRewardedAd(
                                    activity = act,
                                    onUserEarnedReward = {
                                        viewModel.generateWorkout(
                                            goal = "Hypertrophy & Elite Performance",
                                            category = "Full Body Power",
                                            durationMinutes = 60
                                        )
                                    }
                                )
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                        modifier = Modifier.testTag("btn_watch_rewarded_workout")
                    ) {
                        Text("Watch Ad", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // 1-Tap Quick Presets Row
        item {
            Text(
                text = "Quick-Start Routines (1-Tap)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(presets) { preset ->
                    Card(
                        modifier = Modifier
                            .width(220.dp)
                            .testTag("preset_${preset.title}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Text(
                                text = preset.category,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CoralSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = preset.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${preset.duration}m • ~${preset.calories} kcal",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    viewModel.logWorkout(
                                        title = preset.title,
                                        category = preset.category,
                                        durationMinutes = preset.duration,
                                        caloriesBurned = preset.calories,
                                        exercises = preset.exercises,
                                        intensity = preset.intensity
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                modifier = Modifier.fillMaxWidth().testTag("log_preset_${preset.title}")
                            ) {
                                Text("+ Log Routine", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Workout History
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Workout History (${allWorkouts.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (allWorkouts.isEmpty()) {
            item {
                EmptyStateItem(
                    icon = Icons.AutoMirrored.Filled.DirectionsRun,
                    title = "No workout logs found",
                    description = "Choose a quick-start routine or generate an AI routine above!"
                )
            }
        } else {
            items(allWorkouts, key = { "all_workout_${it.id}" }) { workout ->
                WorkoutItemCard(
                    workout = workout,
                    onDelete = { viewModel.deleteWorkout(workout) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
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
fun WorkoutStatColumn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    tint: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
