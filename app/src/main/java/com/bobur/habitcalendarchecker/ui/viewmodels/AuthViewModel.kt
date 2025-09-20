package com.bobur.habitcalendarchecker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bobur.habitcalendarchecker.data.auth.AuthCredentials
import com.bobur.habitcalendarchecker.data.auth.AuthService
import com.bobur.habitcalendarchecker.data.auth.AuthState
import com.bobur.habitcalendarchecker.data.auth.RegisterRequest
import com.bobur.habitcalendarchecker.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(private val authService: AuthService) : ViewModel() {
    
    val authState: StateFlow<AuthState> = authService.authState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AuthState.Unauthenticated
        )
    
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState
    
    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState
    
    // State for profile update operation
    private val _updateProfileState = MutableStateFlow<UpdateProfileState>(UpdateProfileState.Idle)
    val updateProfileState: StateFlow<UpdateProfileState> = _updateProfileState
    
    // State for account deletion operation
    private val _deleteAccountState = MutableStateFlow<DeleteAccountState>(DeleteAccountState.Idle)
    val deleteAccountState: StateFlow<DeleteAccountState> = _deleteAccountState
    
    fun login(email: String, password: String) {
        _loginState.value = LoginState.Loading
        
        viewModelScope.launch {
            val result = authService.login(AuthCredentials(email, password))
            _loginState.value = result.fold(
                onSuccess = { LoginState.Success },
                onFailure = { LoginState.Error(it.message ?: "Unknown error") }
            )
        }
    }
    
    fun register(username: String, email: String, password: String) {
        _registerState.value = RegisterState.Loading
        
        viewModelScope.launch {
            val result = authService.register(RegisterRequest(username, email, password))
            _registerState.value = result.fold(
                onSuccess = { RegisterState.Success },
                onFailure = { RegisterState.Error(it.message ?: "Unknown error") }
            )
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            authService.logout()
            // Reset other states if necessary upon logout
            _updateProfileState.value = UpdateProfileState.Idle
            _deleteAccountState.value = DeleteAccountState.Idle
        }
    }
    
    fun updateUserProfile(userId: String, newUsername: String, newEmail: String) {
        _updateProfileState.value = UpdateProfileState.Loading
        viewModelScope.launch {
            val result = authService.updateUserProfile(userId, newUsername, newEmail)
            _updateProfileState.value = result.fold(
                onSuccess = { UpdateProfileState.Success(it) },
                onFailure = { UpdateProfileState.Error(it.message ?: "Failed to update profile") }
            )
        }
    }
    
    fun deleteUserAccount(userId: String) {
        _deleteAccountState.value = DeleteAccountState.Loading
        viewModelScope.launch {
            val result = authService.deleteUserAccount(userId)
            _deleteAccountState.value = result.fold(
                onSuccess = { DeleteAccountState.Success }, // AuthState will update to Unauthenticated via authService.logout()
                onFailure = { DeleteAccountState.Error(it.message ?: "Failed to delete account") }
            )
        }
    }
    
    fun resetLoginState() {
        _loginState.value = LoginState.Idle
    }
    
    fun resetRegisterState() {
        _registerState.value = RegisterState.Idle
    }
    
    fun resetUpdateProfileState() {
        _updateProfileState.value = UpdateProfileState.Idle
    }
    
    fun resetDeleteAccountState() {
        _deleteAccountState.value = DeleteAccountState.Idle
    }
    
    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        object Success : LoginState()
        data class Error(val message: String) : LoginState()
    }
    
    sealed class RegisterState {
        object Idle : RegisterState()
        object Loading : RegisterState()
        object Success : RegisterState()
        data class Error(val message: String) : RegisterState()
    }
    
    // Sealed class for Update Profile State
    sealed class UpdateProfileState {
        object Idle : UpdateProfileState()
        object Loading : UpdateProfileState()
        data class Success(val updatedUser: User) : UpdateProfileState()
        data class Error(val message: String) : UpdateProfileState()
    }
    
    // Sealed class for Delete Account State
    sealed class DeleteAccountState {
        object Idle : DeleteAccountState()
        object Loading : DeleteAccountState()
        object Success : DeleteAccountState() // User is logged out, Nav handled by AuthState change
        data class Error(val message: String) : DeleteAccountState()
    }
    
    class Factory(private val authService: AuthService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                return AuthViewModel(authService) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
