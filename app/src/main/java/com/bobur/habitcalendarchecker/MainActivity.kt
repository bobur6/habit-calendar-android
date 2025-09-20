package com.bobur.habitcalendarchecker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.bobur.habitcalendarchecker.data.database.HabitCheckDao
import com.bobur.habitcalendarchecker.data.database.HabitDao
import com.bobur.habitcalendarchecker.data.database.HabitListDao
import com.bobur.habitcalendarchecker.data.database.UserDao
import com.bobur.habitcalendarchecker.data.repository.HabitCheckRepository
import com.bobur.habitcalendarchecker.data.repository.HabitListRepository
import com.bobur.habitcalendarchecker.data.repository.HabitRepository
import com.bobur.habitcalendarchecker.ui.navigation.AppNavHost
import com.bobur.habitcalendarchecker.ui.theme.HabitCalendarCheckerTheme
import com.bobur.habitcalendarchecker.ui.viewmodels.AuthViewModel

// Composition locals for dependency injection
val LocalUserDao = compositionLocalOf<UserDao> { error("No UserDao provided") }
val LocalHabitListDao = compositionLocalOf<HabitListDao> { error("No HabitListDao provided") }
val LocalHabitDao = compositionLocalOf<HabitDao> { error("No HabitDao provided") }
val LocalHabitCheckDao = compositionLocalOf<HabitCheckDao> { error("No HabitCheckDao provided") }
val LocalHabitListRepository = compositionLocalOf<HabitListRepository> { error("No HabitListRepository provided") }
val LocalHabitRepository = compositionLocalOf<HabitRepository> { error("No HabitRepository provided") }
val LocalHabitCheckRepository = compositionLocalOf<HabitCheckRepository> { error("No HabitCheckRepository provided") }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the HabitApplication instance
        val app = applicationContext as HabitApplication

        setContent {
            HabitCalendarCheckerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Get AuthService from HabitApplication
                    val authService = app.authService

                    // Provide dependencies through composition locals using instances from HabitApplication
                    CompositionLocalProvider(
                        LocalUserDao provides app.userDao,
                        LocalHabitListDao provides app.habitListDao,
                        LocalHabitDao provides app.habitDao,
                        LocalHabitCheckDao provides app.habitCheckDao,
                        LocalHabitListRepository provides app.habitListRepository,
                        LocalHabitRepository provides app.habitRepository,
                        LocalHabitCheckRepository provides app.habitCheckRepository
                    ) {
                        // Initialize view models
                        val authViewModel: AuthViewModel = viewModel(
                            factory = AuthViewModel.Factory(authService)
                        )

                        // Set up navigation
                        val navController = rememberNavController()

                        AppNavHost(
                            navController = navController,
                            authViewModel = authViewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}