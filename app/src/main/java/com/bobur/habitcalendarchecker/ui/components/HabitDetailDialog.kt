package com.bobur.habitcalendarchecker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.bobur.habitcalendarchecker.R
import com.bobur.habitcalendarchecker.data.model.Habit
import com.bobur.habitcalendarchecker.data.model.HabitCheck
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailDialog(
    habit: Habit,
    habitChecks: List<HabitCheck>,
    onDismiss: () -> Unit,
    onEditHabit: (Long, String) -> Unit = { _, _ -> },
    onDeleteHabit: (Long) -> Unit = { _ -> },
    onClearHabitChecks: (Long) -> Unit = { _ -> }
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showClearChecksDialog by remember { mutableStateOf(false) }
    var editedHabitName by remember { mutableStateOf(habit.name) }
    
    val today = LocalDate.now()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                // Action Buttons (Edit, Clear, Delete)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = { showEditDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.habit_detail_edit_description),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.habit_detail_edit_button))
                    }
                    TextButton(onClick = { showClearChecksDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = stringResource(R.string.habit_detail_clear_checks_description),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.habit_detail_clear_checks_button))
                    }
                    TextButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.habit_detail_delete_description),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.habit_detail_delete_button))
                    }
                }
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                // Статистика
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.habit_detail_statistics_title),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(
                                title = stringResource(R.string.habit_detail_stats_total),
                                value = habitChecks.size.toString()
                            )
                            
                            StatItem(
                                title = stringResource(R.string.habit_detail_stats_this_month),
                                value = habitChecks.count { 
                                    val date = HabitCheck.toLocalDate(it.date)
                                    date.month == today.month && date.year == today.year 
                                }.toString()
                            )
                            
                            StatItem(
                                title = stringResource(R.string.habit_detail_stats_streak),
                                value = calculateStreak(habitChecks, today).toString()
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Информация о привычке
                Text(
                    text = stringResource(R.string.habit_detail_created_date, habit.formattedCreatedDate),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(   
                    text = stringResource(R.string.habit_detail_updated_date, habit.formattedUpdatedDate),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.dialog_close_button))
            }
        },
        dismissButton = { }
    )
    
    // Диалог редактирования привычки
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(stringResource(R.string.edit_habit_dialog_title)) },
            text = {
                OutlinedTextField(
                    value = editedHabitName,
                    onValueChange = { editedHabitName = it },
                    label = { Text(stringResource(R.string.edit_habit_name_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editedHabitName.isNotBlank()) {
                            onEditHabit(habit.id, editedHabitName)
                            showEditDialog = false
                        }
                    }
                ) {
                    Text(stringResource(R.string.dialog_save_button))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditDialog = false }
                ) {
                    Text(stringResource(R.string.dialog_cancel_button))
                }
            }
        )
    }
    
    // Диалог подтверждения удаления привычки
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_habit_dialog_title)) },
            text = { Text(stringResource(R.string.delete_habit_confirmation_message, habit.name)) },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteHabit(habit.id)
                        showDeleteDialog = false
                        onDismiss()
                    }
                ) {
                    Text(stringResource(R.string.dialog_delete_button))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text(stringResource(R.string.dialog_cancel_button))
                }
            }
        )
    }
    
    // Диалог подтверждения очистки отметок
    if (showClearChecksDialog) {
        AlertDialog(
            onDismissRequest = { showClearChecksDialog = false },
            title = { Text(stringResource(R.string.clear_all_checks_dialog_title)) },
            text = { Text(stringResource(R.string.clear_all_checks_confirmation_message, habit.name)) },
            confirmButton = {
                Button(
                    onClick = {
                        onClearHabitChecks(habit.id)
                        showClearChecksDialog = false
                    }
                ) {
                    Text(stringResource(R.string.dialog_clear_button))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearChecksDialog = false }
                ) {
                    Text(stringResource(R.string.dialog_cancel_button))
                }
            }
        )
    }
}

@Composable
fun StatItem(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

// Расширение для форматирования даты создания
private val Habit.formattedCreatedDate: String
    get() {
        val date = java.time.Instant.ofEpochMilli(createdAt)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
        return date.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    }

// Расширение для форматирования даты обновления
private val Habit.formattedUpdatedDate: String
    get() {
        val date = java.time.Instant.ofEpochMilli(updatedAt)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
        return date.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    }

// Функция для расчета текущей серии отметок
private fun calculateStreak(habitChecks: List<HabitCheck>, today: LocalDate): Int {
    if (habitChecks.isEmpty()) return 0
    
    // Сортируем отметки по дате (от новых к старым)
    val sortedChecks = habitChecks
        .map { HabitCheck.toLocalDate(it.date) }
        .sortedDescending()
    
    // Проверяем, есть ли отметка за сегодня
    val startDate = if (sortedChecks.first() == today) today else today.minusDays(1)
    
    var currentDate = startDate
    var streak = 0
    
    while (sortedChecks.contains(currentDate)) {
        streak++
        currentDate = currentDate.minusDays(1)
    }
    
    return streak
}
