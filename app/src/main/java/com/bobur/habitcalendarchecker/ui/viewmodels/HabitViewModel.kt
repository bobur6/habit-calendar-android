package com.bobur.habitcalendarchecker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bobur.habitcalendarchecker.data.model.Habit
import com.bobur.habitcalendarchecker.data.model.HabitCheck
import com.bobur.habitcalendarchecker.data.model.HabitList
import com.bobur.habitcalendarchecker.data.repository.HabitCheckRepository
import com.bobur.habitcalendarchecker.data.repository.HabitListRepository
import com.bobur.habitcalendarchecker.data.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate

class HabitViewModel(
    private val habitRepository: HabitRepository,
    private val habitCheckRepository: HabitCheckRepository,
    private val habitListRepository: HabitListRepository,
    private val listId: Long
    // habitId parameter removed as it's not generally used for the whole ViewModel lifecycle
) : ViewModel() {
    
    // --- States for UI feedback ---
    private val _createHabitState = MutableStateFlow<CreateHabitState>(CreateHabitState.Idle)
    val createHabitState: StateFlow<CreateHabitState> = _createHabitState.asStateFlow()
    
    private val _updateHabitListState = MutableStateFlow<UpdateHabitListState>(UpdateHabitListState.Idle)
    val updateHabitListState: StateFlow<UpdateHabitListState> = _updateHabitListState.asStateFlow()
    
    // --- Core Data Flows ---
    val habitList: Flow<HabitList?> = habitListRepository.getHabitListById(listId)
    val habits: Flow<List<Habit>> = habitRepository.getHabitsByListId(listId)

    // Combined flow for habits and their checks for the current week
    private val _habitsWithChecks = MutableStateFlow<Map<Long, List<HabitCheck>>>(emptyMap())
    val habitsWithChecks: StateFlow<Map<Long, List<HabitCheck>>> = _habitsWithChecks.asStateFlow()

    private var currentLoadedWeekStart: LocalDate? = null // Keeps track of the *center* of the displayed week for UI logic if needed
    private var loadedChecksDateRange: ClosedRange<LocalDate>? = null // Tracks the actual full range of loaded check data

    fun loadHabitChecksForWeek(displayWeekStartDate: LocalDate) {
        val newDesiredLoadStart = displayWeekStartDate.minusWeeks(2)
        // Ensure the end of the range covers 2 full weeks *after* the start of the display week.
        // So, displayWeekStartDate + 2 weeks gets us to the start of the 2nd week ahead.
        // Then, .plusDays(6) covers that entire week.
        val newDesiredLoadEnd = displayWeekStartDate.plusWeeks(2).plusDays(6)

        val needsReload = loadedChecksDateRange == null ||
                newDesiredLoadStart < loadedChecksDateRange!!.start ||
                newDesiredLoadEnd > loadedChecksDateRange!!.endInclusive

        if (!needsReload && displayWeekStartDate == currentLoadedWeekStart) { // Also check if display week hasn't changed
            // Data for the broader range is already loaded, and the focused display week is the same.
            return
        }

        currentLoadedWeekStart = displayWeekStartDate // Update the focus week for UI

        viewModelScope.launch {
            // If needsReload is true, we fetch the entire new broad range.
            // If needsReload is false but displayWeekStartDate changed, we don't refetch, 
            // as the data is assumed to be there from a previous broader load.
            // This means the `if (!needsReload)` check above effectively handles this.
            // The only time we proceed here is if a genuine broader fetch is required.

            if (needsReload) {
                habits.firstOrNull()?.let { currentHabits ->
                    if (currentHabits.isEmpty()) {
                        _habitsWithChecks.value = emptyMap()
                        loadedChecksDateRange = newDesiredLoadStart..newDesiredLoadEnd
                        return@launch
                    }
                    val checksMap = mutableMapOf<Long, List<HabitCheck>>()
                    currentHabits.forEach { habit ->
                        try {
                            val checks = habitCheckRepository.getHabitChecksByDateRange(habit.id, newDesiredLoadStart, newDesiredLoadEnd)
                                .firstOrNull() ?: emptyList()
                            checksMap[habit.id] = checks
                        } catch (e: Exception) {
                            checksMap[habit.id] = emptyList()
                            // Log error
                        }
                    }
                    _habitsWithChecks.value = checksMap
                    loadedChecksDateRange = newDesiredLoadStart..newDesiredLoadEnd
                } ?: run {
                    _habitsWithChecks.value = emptyMap()
                    loadedChecksDateRange = newDesiredLoadStart..newDesiredLoadEnd
                }
            } // else: data for the new displayWeekStartDate should already be within the loadedChecksDateRange
        }
    }


    // --- CRUD Operations ---
    
    fun createHabit(name: String) {
        if (name.isBlank()) {
            _createHabitState.value = CreateHabitState.Error("Habit name cannot be empty")
            return
        }
        _createHabitState.value = CreateHabitState.Loading
        viewModelScope.launch {
            try {
                habitRepository.createHabit(listId, name)
                _createHabitState.value = CreateHabitState.Success(name) // Pass name for snackbar
            } catch (e: Exception) {
                _createHabitState.value = CreateHabitState.Error(e.message ?: "Failed to create habit")
            }
        }
    }
    
    fun updateHabitName(habitId: Long, newName: String) {
        if (newName.isBlank()) return // Or provide error feedback
        viewModelScope.launch {
            try {
                val habit = habitRepository.getHabitById(habitId).firstOrNull()
                habit?.let {
                    habitRepository.updateHabit(it.copy(name = newName))
                    // Optionally, provide success feedback via a StateFlow if needed elsewhere
                }
            } catch (e: Exception) {
                // Handle error, e.g., Log.e("HabitViewModel", "Failed to update habit name", e)
            }
        }
    }
    
    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            try {
                val habit = habitRepository.getHabitById(habitId).firstOrNull()
                habit?.let {
                    habitRepository.deleteHabit(it)
                    // Refresh checks map if a habit is deleted
                    currentLoadedWeekStart?.let { weekStart -> loadHabitChecksForWeek(weekStart) }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun updateHabitList(newName: String) {
        if (newName.isBlank()) {
            _updateHabitListState.value = UpdateHabitListState.Error("List name cannot be empty")
            return
        }
        _updateHabitListState.value = UpdateHabitListState.Loading
        viewModelScope.launch {
            try {
                val currentList = habitList.firstOrNull()
                currentList?.let {
                    habitListRepository.updateHabitList(it.copy(name = newName))
                    _updateHabitListState.value = UpdateHabitListState.Success
                } ?: run {
                    _updateHabitListState.value = UpdateHabitListState.Error("Habit list not found.")
                }
            } catch (e: Exception) {
                _updateHabitListState.value = UpdateHabitListState.Error(e.message ?: "Failed to update list name")
            }
        }
    }

    // --- Habit Check Operations ---
    fun createOrUpdateHabitCheck(habitId: Long, date: LocalDate, emoji: String, note: String?) {
        viewModelScope.launch {
            try {
                habitCheckRepository.createOrUpdateHabitCheck(habitId, date, emoji, note)
                // Refresh checks for the affected habit and week
                currentLoadedWeekStart?.let { currentWeekStart ->
                    loadHabitChecksForHabit(habitId, currentWeekStart) // Use helper
                }
            } catch (e: Exception) {
                // Handle error, maybe show a snackbar from the screen
                // Consider emitting an error state
            }
        }
    }
    
    fun deleteHabitCheck(habitId: Long, date: LocalDate) {
        viewModelScope.launch {
            try {
                val checkToDelete = habitCheckRepository.getHabitCheckByHabitIdAndDate(habitId, date)
                checkToDelete?.let {
                    habitCheckRepository.deleteHabitCheck(it)
                    // Refresh checks for the affected habit and week
                    currentLoadedWeekStart?.let { weekStart ->
                       loadHabitChecksForHabit(habitId, weekStart) // Use helper
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun clearAllHabitChecks(habitId: Long) {
        viewModelScope.launch {
            try {
                habitCheckRepository.deleteAllHabitChecks(habitId)
                // Refresh checks for the affected habit
                 currentLoadedWeekStart?.let { _ ->
                    _habitsWithChecks.value = _habitsWithChecks.value.toMutableMap().apply {
                        this[habitId] = emptyList() // Directly set to empty after clearing all
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    // Helper function to reload checks for a specific habit for the current week
    private fun loadHabitChecksForHabit(habitId: Long, weekStartDate: LocalDate) {
         viewModelScope.launch {
            val updatedChecksForHabit = habitCheckRepository.getHabitChecksByDateRange(habitId, weekStartDate, weekStartDate.plusDays(6)).firstOrNull() ?: emptyList()
            _habitsWithChecks.value = _habitsWithChecks.value.toMutableMap().apply {
                this[habitId] = updatedChecksForHabit
            }
        }
    }
    
    // --- State Resets ---
    fun resetCreateHabitState() {
        _createHabitState.value = CreateHabitState.Idle
    }
    
    fun resetUpdateHabitListState() {
        _updateHabitListState.value = UpdateHabitListState.Idle
    }

    // --- Sealed Classes for States ---
    sealed class CreateHabitState {
        object Idle : CreateHabitState()
        object Loading : CreateHabitState()
        data class Success(val habitName: String) : CreateHabitState() // Include name for Snackbar
        data class Error(val message: String) : CreateHabitState()
    }
    
    sealed class UpdateHabitListState {
        object Idle : UpdateHabitListState()
        object Loading : UpdateHabitListState()
        object Success : UpdateHabitListState()
        data class Error(val message: String) : UpdateHabitListState()
    }

    // --- Factory ---
    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val habitRepository: HabitRepository,
        private val habitCheckRepository: HabitCheckRepository,
        private val habitListRepository: HabitListRepository,
        private val listId: Long
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HabitViewModel::class.java)) {
                return HabitViewModel(habitRepository, habitCheckRepository, habitListRepository, listId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
