package com.bobur.habitcalendarchecker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bobur.habitcalendarchecker.data.model.HabitList
import com.bobur.habitcalendarchecker.data.repository.HabitListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HabitListViewModel(
    private val habitListRepository: HabitListRepository,
    private val userId: String
) : ViewModel() {
    
    private val _createListState = MutableStateFlow<CreateListState>(CreateListState.Idle)
    val createListState: StateFlow<CreateListState> = _createListState
    
    val habitLists: Flow<List<HabitList>> = habitListRepository.getHabitListsByUserId(userId)
    
    fun createHabitList(name: String) {
        if (name.isBlank()) {
            _createListState.value = CreateListState.Error("List name cannot be empty")
            return
        }
        
        _createListState.value = CreateListState.Loading
        
        viewModelScope.launch {
            try {
                val listId = habitListRepository.createHabitList(userId, name)
                _createListState.value = CreateListState.Success(listId)
            } catch (e: Exception) {
                _createListState.value = CreateListState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun updateHabitList(habitList: HabitList, newName: String) {
        if (newName.isBlank()) {
            return
        }
        
        viewModelScope.launch {
            try {
                val updatedList = habitList.copy(name = newName)
                habitListRepository.updateHabitList(updatedList)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun deleteHabitList(habitList: HabitList) {
        viewModelScope.launch {
            try {
                habitListRepository.deleteHabitList(habitList)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun resetCreateListState() {
        _createListState.value = CreateListState.Idle
    }
    
    sealed class CreateListState {
        object Idle : CreateListState()
        object Loading : CreateListState()
        data class Success(val listId: Long) : CreateListState()
        data class Error(val message: String) : CreateListState()
    }
    
    class Factory(
        private val habitListRepository: HabitListRepository,
        private val userId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HabitListViewModel::class.java)) {
                return HabitListViewModel(habitListRepository, userId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
