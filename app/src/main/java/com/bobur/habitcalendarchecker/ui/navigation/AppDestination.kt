package com.bobur.habitcalendarchecker.ui.navigation

sealed class AppDestination(val route: String) {
    // Auth screens
    object Login : AppDestination("login")
    object Register : AppDestination("register")
    
    // Main app screens
    object Home : AppDestination("home")
    object Profile : AppDestination("profile")
    
    // Habit list screens
    object HabitListDetail : AppDestination("habitList/{listId}") {
        fun createRoute(listId: Long): String = "habitList/$listId"
    }
}
