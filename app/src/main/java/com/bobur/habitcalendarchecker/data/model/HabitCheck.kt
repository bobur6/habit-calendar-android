package com.bobur.habitcalendarchecker.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.time.LocalDate

@Entity(
    tableName = "habit_checks",
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("habitId"), Index("date")]
)
@Serializable
data class HabitCheck(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val date: Long, // Stored as epoch day
    val emoji: String = "✅",
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        // Вспомогательные методы для конвертации LocalDate в Long и обратно
        fun fromLocalDate(date: LocalDate): Long = date.toEpochDay()
        
        fun toLocalDate(epochDay: Long): LocalDate = LocalDate.ofEpochDay(epochDay)
    }
}
