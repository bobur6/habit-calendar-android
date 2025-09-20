package com.bobur.habitcalendarchecker.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.bobur.habitcalendarchecker.data.model.HabitCheck
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface HabitCheckDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitCheck(habitCheck: HabitCheck): Long
    
    @Update
    suspend fun updateHabitCheck(habitCheck: HabitCheck): Int
    
    @Delete
    suspend fun deleteHabitCheck(habitCheck: HabitCheck): Int
    
    @Query("DELETE FROM habit_checks WHERE habitId = :habitId")
    suspend fun deleteAllHabitChecks(habitId: Long): Int
    
    @Transaction
    @Query("SELECT * FROM habit_checks WHERE id = :checkId")
    fun getHabitCheckById(checkId: Long): Flow<HabitCheck?>
    
    @Transaction
    @Query("SELECT * FROM habit_checks WHERE habitId = :habitId ORDER BY date DESC")
    fun getHabitChecksByHabitId(habitId: Long): Flow<List<HabitCheck>>
    
    @Transaction
    @Query("SELECT * FROM habit_checks WHERE habitId = :habitId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getHabitChecksByDateRange(habitId: Long, startDate: Long, endDate: Long): Flow<List<HabitCheck>>
    
    @Transaction
    @Query("SELECT * FROM habit_checks WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getHabitCheckByDate(habitId: Long, date: Long): HabitCheck?
}
