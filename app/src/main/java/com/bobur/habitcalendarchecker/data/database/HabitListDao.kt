package com.bobur.habitcalendarchecker.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.bobur.habitcalendarchecker.data.model.HabitList
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitList(habitList: HabitList): Long
    
    @Update
    suspend fun updateHabitList(habitList: HabitList): Int
    
    @Delete
    suspend fun deleteHabitList(habitList: HabitList): Int
    
    @Transaction
    @Query("SELECT * FROM habit_lists WHERE id = :listId")
    fun getHabitListById(listId: Long): Flow<HabitList?>
    
    @Transaction
    @Query("SELECT * FROM habit_lists WHERE userId = :userId ORDER BY updatedAt DESC")
    fun getHabitListsByUserId(userId: String): Flow<List<HabitList>>
}
