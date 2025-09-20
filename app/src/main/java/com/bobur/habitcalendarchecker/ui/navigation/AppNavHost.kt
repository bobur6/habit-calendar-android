package com.bobur.habitcalendarchecker.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.bobur.habitcalendarchecker.LocalHabitCheckRepository
import com.bobur.habitcalendarchecker.LocalHabitListRepository
import com.bobur.habitcalendarchecker.LocalHabitRepository
import com.bobur.habitcalendarchecker.data.auth.AuthState
import com.bobur.habitcalendarchecker.ui.screens.auth.LoginScreen
import com.bobur.habitcalendarchecker.ui.screens.auth.RegisterScreen
import com.bobur.habitcalendarchecker.ui.screens.habitlist.HabitListDetailScreen
import com.bobur.habitcalendarchecker.ui.screens.home.HomeScreen
import com.bobur.habitcalendarchecker.ui.screens.profile.ProfileScreen
import com.bobur.habitcalendarchecker.ui.viewmodels.AuthViewModel
import com.bobur.habitcalendarchecker.ui.viewmodels.HabitListViewModel
import com.bobur.habitcalendarchecker.ui.viewmodels.HabitViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val authState by authViewModel.authState.collectAsState()
    
    // Get repositories from composition locals
    val habitListRepository = LocalHabitListRepository.current
    val habitRepository = LocalHabitRepository.current
    val habitCheckRepository = LocalHabitCheckRepository.current
    
    NavHost(
        navController = navController,
        startDestination = when (authState) {
            is AuthState.Authenticated -> AppDestination.Home.route
            is AuthState.Unauthenticated -> AppDestination.Login.route
        },
        modifier = modifier
    ) {
        // Auth screens
        composable(
            route = AppDestination.Login.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(AppDestination.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(AppDestination.Home.route) {
                        popUpTo(AppDestination.Login.route) { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }
        
        composable(
            route = AppDestination.Register.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(AppDestination.Login.route) {
                        popUpTo(AppDestination.Register.route) { inclusive = true }
                    }
                },
                onRegisterSuccess = {
                    navController.navigate(AppDestination.Home.route) {
                        popUpTo(AppDestination.Register.route) { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }
        
        // Main app screens
        composable(
            route = AppDestination.Home.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            val userId = (authState as? AuthState.Authenticated)?.userId ?: ""
            val habitListViewModel: HabitListViewModel = viewModel(
                factory = HabitListViewModel.Factory(habitListRepository, userId)
            )
            
            HomeScreen(
                habitListViewModel = habitListViewModel,
                onNavigateToHabitList = { listId ->
                    navController.navigate(AppDestination.HabitListDetail.createRoute(listId))
                },
                onNavigateToProfile = {
                    navController.navigate(AppDestination.Profile.route)
                }
            )
        }
        
        composable(
            route = AppDestination.Profile.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    navController.navigate(AppDestination.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }
        
        // Habit list screens
        composable(
            route = AppDestination.HabitListDetail.route,
            arguments = listOf(
                navArgument("listId") { type = NavType.LongType }
            ),
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) }
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getLong("listId") ?: 0L
            val habitViewModel: HabitViewModel = viewModel(
                factory = HabitViewModel.Factory(habitRepository, habitCheckRepository, habitListRepository, listId)
            )
            
            HabitListDetailScreen(
                listId = listId,
                habitViewModel = habitViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Экран деталей привычки удален, теперь используется диалог
    }
}
