package com.bobur.habitcalendarchecker.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.bobur.habitcalendarchecker.data.model.Habit
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long
    
    @Update
    suspend fun updateHabit(habit: Habit): Int
    
    @Delete
    suspend fun deleteHabit(habit: Habit): Int
    
    @Transaction
    @Query("SELECT * FROM habits WHERE id = :habitId")
    fun getHabitById(habitId: Long): Flow<Habit?>
    
    @Transaction
    @Query("SELECT * FROM habits WHERE listId = :listId ORDER BY createdAt ASC")
    fun getHabitsByListId(listId: Long): Flow<List<Habit>>
}
