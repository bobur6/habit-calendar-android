package com.bobur.habitcalendarchecker.data.repository

import com.bobur.habitcalendarchecker.data.database.HabitCheckDao
import com.bobur.habitcalendarchecker.data.model.HabitCheck
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class HabitCheckRepository(private val habitCheckDao: HabitCheckDao) {
    
    suspend fun createOrUpdateHabitCheck(habitId: Long, date: LocalDate, emoji: String, note: String? = null): Long {
        val epochDay = HabitCheck.fromLocalDate(date)
        val existingCheck = habitCheckDao.getHabitCheckByDate(habitId, epochDay)
        
        return if (existingCheck != null) {
            val updatedCheck = existingCheck.copy(
                emoji = emoji,
                note = note,
                updatedAt = System.currentTimeMillis()
            )
            habitCheckDao.updateHabitCheck(updatedCheck)
            existingCheck.id
        } else {
            val newCheck = HabitCheck(
                habitId = habitId,
                date = epochDay,
                emoji = emoji,
                note = note
            )
            habitCheckDao.insertHabitCheck(newCheck)
        }
    }
    
    suspend fun deleteHabitCheck(habitCheck: HabitCheck) {
        habitCheckDao.deleteHabitCheck(habitCheck)
    }
    
    suspend fun deleteAllHabitChecks(habitId: Long) {
        habitCheckDao.deleteAllHabitChecks(habitId)
    }
    
    fun getHabitCheckById(checkId: Long): Flow<HabitCheck?> {
        return habitCheckDao.getHabitCheckById(checkId)
    }
    
    fun getHabitChecksByHabitId(habitId: Long): Flow<List<HabitCheck>> {
        return habitCheckDao.getHabitChecksByHabitId(habitId)
    }
    
    fun getHabitChecksByDateRange(habitId: Long, startDate: LocalDate, endDate: LocalDate): Flow<List<HabitCheck>> {
        val startEpochDay = HabitCheck.fromLocalDate(startDate)
        val endEpochDay = HabitCheck.fromLocalDate(endDate)
        return habitCheckDao.getHabitChecksByDateRange(habitId, startEpochDay, endEpochDay)
            .map { checks ->
                // Ensure we have valid checks
                checks.filter { it.habitId == habitId }
            }
    }

    suspend fun getHabitCheckByHabitIdAndDate(habitId: Long, date: LocalDate): HabitCheck? {
        val epochDay = HabitCheck.fromLocalDate(date)
        return habitCheckDao.getHabitCheckByDate(habitId, epochDay)
    }
}
