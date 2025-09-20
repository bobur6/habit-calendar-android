package com.bobur.habitcalendarchecker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.bobur.habitcalendarchecker.data.model.Habit
import com.bobur.habitcalendarchecker.data.model.HabitCheck
import com.bobur.habitcalendarchecker.data.model.HabitList
import com.bobur.habitcalendarchecker.data.model.User
import java.time.LocalDate

/**
 * Type converters for Room database
 */
class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }

    @TypeConverter
    fun toLocalDate(epochDay: Long?): LocalDate? {
        return epochDay?.let { LocalDate.ofEpochDay(it) }
    }
}

@Database(
    entities = [User::class, HabitList::class, Habit::class, HabitCheck::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun habitListDao(): HabitListDao
    abstract fun habitDao(): HabitDao
    abstract fun habitCheckDao(): HabitCheckDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habit_calendar_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
