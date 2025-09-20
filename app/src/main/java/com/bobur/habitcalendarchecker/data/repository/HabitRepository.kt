package com.bobur.habitcalendarchecker.data.repository

import com.bobur.habitcalendarchecker.data.database.HabitDao
import com.bobur.habitcalendarchecker.data.model.Habit
import kotlinx.coroutines.flow.Flow

class HabitRepository(private val habitDao: HabitDao) {
    
    suspend fun createHabit(listId: Long, name: String): Long {
        val habit = Habit(
            listId = listId,
            name = name
        )
        return habitDao.insertHabit(habit)
    }
    
    suspend fun updateHabit(habit: Habit) {
        val updatedHabit = habit.copy(updatedAt = System.currentTimeMillis())
        habitDao.updateHabit(updatedHabit)
    }
    
    suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteHabit(habit)
    }
    
    fun getHabitById(habitId: Long): Flow<Habit?> {
        return habitDao.getHabitById(habitId)
    }
    
    fun getHabitsByListId(listId: Long): Flow<List<Habit>> {
        return habitDao.getHabitsByListId(listId)
    }
}
