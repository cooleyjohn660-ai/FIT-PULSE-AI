package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.UserGoals
import com.example.ui.components.AdMobHubDialog
import com.example.ui.screens.AiCoachScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MealPlannerScreen
import com.example.ui.screens.WorkoutsScreen
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldPrimary

enum class FitnessNavTab(
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    DASHBOARD("Today", Icons.Default.LocalFireDepartment, "tab_dashboard"),
    WORKOUTS("Workouts", Icons.Default.FitnessCenter, "tab_workouts"),
    MEALS("Nutrition", Icons.Default.Restaurant, "tab_meals"),
    COACH("AI Coach", Icons.Default.AutoAwesome, "tab_coach")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainFitnessScreen(
    viewModel: FitnessViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(FitnessNavTab.DASHBOARD) }
    var showGoalsDialog by remember { mutableStateOf(false) }
    var showAdMobDialog by remember { mutableStateOf(false) }
    val userGoals by viewModel.userGoals.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "FitPulse",
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = " AI",
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAdMobDialog = true },
                        modifier = Modifier.testTag("open_admob_hub_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = "Rewarded Ads",
                            tint = AmberAccent
                        )
                    }
                    IconButton(
                        onClick = { showGoalsDialog = true },
                        modifier = Modifier.testTag("open_goals_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Adjust Goals",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                FitnessNavTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            indicatorColor = EmeraldPrimary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedTextColor = EmeraldPrimary,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                FitnessNavTab.DASHBOARD -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToCoach = { currentTab = FitnessNavTab.COACH }
                    )
                }
                FitnessNavTab.WORKOUTS -> {
                    WorkoutsScreen(viewModel = viewModel)
                }
                FitnessNavTab.MEALS -> {
                    MealPlannerScreen(viewModel = viewModel)
                }
                FitnessNavTab.COACH -> {
                    AiCoachScreen(viewModel = viewModel)
                }
            }
        }
    }

    if (showGoalsDialog) {
        GoalsSettingsDialog(
            currentGoals = userGoals,
            onDismiss = { showGoalsDialog = false },
            onSave = { updated ->
                viewModel.updateGoals(updated)
                showGoalsDialog = false
            }
        )
    }

    if (showAdMobDialog) {
        AdMobHubDialog(
            onDismiss = { showAdMobDialog = false }
        )
    }
}

@Composable
fun GoalsSettingsDialog(
    currentGoals: UserGoals,
    onDismiss: () -> Unit,
    onSave: (UserGoals) -> Unit
) {
    var calories by remember { mutableStateOf(currentGoals.calorieTarget.toString()) }
    var protein by remember { mutableStateOf(currentGoals.proteinTargetGrams.toInt().toString()) }
    var carbs by remember { mutableStateOf(currentGoals.carbsTargetGrams.toInt().toString()) }
    var fat by remember { mutableStateOf(currentGoals.fatTargetGrams.toInt().toString()) }
    var selectedGoal by remember { mutableStateOf(currentGoals.primaryGoal) }
    var selectedDiet by remember { mutableStateOf(currentGoals.dietType) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Personal Fitness Goals", fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Primary Goal:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Lean Muscle", "Fat Loss", "Strength").forEach { g ->
                        FilterChip(
                            selected = selectedGoal == g,
                            onClick = { selectedGoal = g },
                            label = { Text(g, fontSize = 11.sp) }
                        )
                    }
                }

                Text("Diet Style:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("High Protein", "Balanced", "Keto").forEach { d ->
                        FilterChip(
                            selected = selectedDiet == d,
                            onClick = { selectedDiet = d },
                            label = { Text(d, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text("Daily Calorie Target (kcal)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = protein,
                        onValueChange = { protein = it },
                        label = { Text("Protein (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = carbs,
                        onValueChange = { carbs = it },
                        label = { Text("Carbs (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = fat,
                        onValueChange = { fat = it },
                        label = { Text("Fat (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = UserGoals(
                        calorieTarget = calories.toIntOrNull() ?: currentGoals.calorieTarget,
                        proteinTargetGrams = protein.toFloatOrNull() ?: currentGoals.proteinTargetGrams,
                        carbsTargetGrams = carbs.toFloatOrNull() ?: currentGoals.carbsTargetGrams,
                        fatTargetGrams = fat.toFloatOrNull() ?: currentGoals.fatTargetGrams,
                        primaryGoal = selectedGoal,
                        dietType = selectedDiet
                    )
                    onSave(updated)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("Save Targets", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
