package com.bobur.habitcalendarchecker

import android.app.Application
import com.bobur.habitcalendarchecker.data.database.AppDatabase
import com.bobur.habitcalendarchecker.data.database.HabitCheckDao
import com.bobur.habitcalendarchecker.data.database.HabitDao
import com.bobur.habitcalendarchecker.data.database.HabitListDao
import com.bobur.habitcalendarchecker.data.database.UserDao
import com.bobur.habitcalendarchecker.data.repository.HabitCheckRepository
import com.bobur.habitcalendarchecker.data.repository.HabitListRepository
import com.bobur.habitcalendarchecker.data.repository.HabitRepository
import com.bobur.habitcalendarchecker.data.auth.AuthService

class HabitApplication : Application() {

    // Database instance
    private val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    // DAO instances
    val userDao: UserDao by lazy { database.userDao() }
    val habitListDao: HabitListDao by lazy { database.habitListDao() }
    val habitDao: HabitDao by lazy { database.habitDao() }
    val habitCheckDao: HabitCheckDao by lazy { database.habitCheckDao() }

    // Repository instances
    val habitListRepository: HabitListRepository by lazy { HabitListRepository(habitListDao) }
    val habitRepository: HabitRepository by lazy { HabitRepository(habitDao) }
    val habitCheckRepository: HabitCheckRepository by lazy { HabitCheckRepository(habitCheckDao) }

    // AuthService instance
    val authService: AuthService by lazy { AuthService(this, userDao) }

    override fun onCreate() {
        super.onCreate()
        // You can add any other global initialization here if needed in the future
    }
} 