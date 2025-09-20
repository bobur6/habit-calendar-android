package com.bobur.habitcalendarchecker.data.repository

import com.bobur.habitcalendarchecker.data.database.HabitListDao
import com.bobur.habitcalendarchecker.data.model.HabitList
import kotlinx.coroutines.flow.Flow

class HabitListRepository(private val habitListDao: HabitListDao) {
    
    suspend fun createHabitList(userId: String, name: String): Long {
        val habitList = HabitList(
            userId = userId,
            name = name
        )
        return habitListDao.insertHabitList(habitList)
    }
    
    suspend fun updateHabitList(habitList: HabitList) {
        val updatedList = habitList.copy(updatedAt = System.currentTimeMillis())
        habitListDao.updateHabitList(updatedList)
    }
    
    suspend fun deleteHabitList(habitList: HabitList) {
        habitListDao.deleteHabitList(habitList)
    }
    
    fun getHabitListById(listId: Long): Flow<HabitList?> {
        return habitListDao.getHabitListById(listId)
    }
    
    fun getHabitListsByUserId(userId: String): Flow<List<HabitList>> {
        return habitListDao.getHabitListsByUserId(userId)
    }
}
