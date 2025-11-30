package com.tracker.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tracker.app.ui.screen.*
import com.tracker.app.viewmodel.TrackerViewModel
import com.tracker.app.viewmodel.TrackerViewModelFactory

@Composable
fun TrackerApp(viewModelFactory: TrackerViewModelFactory) {
    val navController = rememberNavController()
    val viewModel: TrackerViewModel = viewModel(factory = viewModelFactory)
    
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToDetail = { taskId ->
                    navController.navigate("detail/$taskId")
                },
                onNavigateToTimeline = {
                    navController.navigate("timeline")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                }
            )
        }
        composable("detail/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
            TaskDetailScreen(
                taskId = taskId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("timeline") {
            TimelineScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

