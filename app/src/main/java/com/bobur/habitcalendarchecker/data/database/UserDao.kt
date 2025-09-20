package com.bobur.habitcalendarchecker.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Transaction
import com.bobur.habitcalendarchecker.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long
    
    @Update
    suspend fun updateUser(user: User): Int
    
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String): Int
    
    @Transaction
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: String): Flow<User?>
    
    @Transaction
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    fun getUserByEmail(email: String): Flow<User?>
    
    @Transaction
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmailSuspend(email: String): User?
    
    @Transaction
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserByIdSuspend(userId: String): User?
    
    @Transaction
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?
}
