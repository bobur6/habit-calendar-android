package com.bobur.habitcalendarchecker.data.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bobur.habitcalendarchecker.data.database.UserDao
import com.bobur.habitcalendarchecker.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.UUID

// For a real app, this would be replaced with actual API calls
// This is a simplified mock implementation for the exam project
class AuthService(
    private val context: Context,
    private val userDao: UserDao
) {
    
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val EMAIL_KEY = stringPreferencesKey("email")
        private const val CREDENTIALS_PREFS = "auth_credentials_prefs"
        private const val CREDENTIALS_MAP_KEY = "credentials_map"
    }
    
    // Persistent storage for credentials (email -> password hash)
    private val credentialsSharedPreferences: SharedPreferences = context.getSharedPreferences(CREDENTIALS_PREFS, Context.MODE_PRIVATE)
    
    // Load credentials from SharedPreferences
    private fun loadCredentials(): MutableMap<String, String> {
        val credentialsJson = credentialsSharedPreferences.getString(CREDENTIALS_MAP_KEY, null) ?: return mutableMapOf()
        return try {
            Json.decodeFromString<Map<String, String>>(credentialsJson).toMutableMap()
        } catch (e: Exception) {
            mutableMapOf()
        }
    }
    
    // Save credentials to SharedPreferences
    private fun saveCredentials(credentials: Map<String, String>) {
        val credentialsJson = Json.encodeToString(credentials)
        credentialsSharedPreferences.edit().putString(CREDENTIALS_MAP_KEY, credentialsJson).apply()
    }
    
    suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        val credentials = loadCredentials()
        // Check if email is already used for credentials or in UserDao
        if (credentials.containsKey(request.email) || userDao.getUserByEmailSuspend(request.email) != null) {
            return Result.failure(Exception("Email already registered"))
        }
        
        // For a real app, hash the password before storing
        // For this mock, we store it as is, but acknowledge this is not secure.
        val hashedPassword = request.password // In a real app: hashPassword(request.password)
        
        val userId = UUID.randomUUID().toString()
        val newUser = User(
            id = userId,
            username = request.username,
            email = request.email
            // profilePictureUrl can be added later if needed
        )
        
        try {
            userDao.insertUser(newUser) // Save user to Room
            val newCredentials = credentials.toMutableMap()
            newCredentials[newUser.email] = hashedPassword
            saveCredentials(newCredentials)
        
            // Generate token and save auth data
            val token = UUID.randomUUID().toString()
            saveAuthData(token, newUser)
        
            return Result.success(
                AuthResponse(
                    token = token,
                    userId = userId,
                    username = request.username,
                    email = request.email
                )
            )
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
    
    suspend fun login(credentials: AuthCredentials): Result<AuthResponse> {
        val allCredentials = loadCredentials() // Load from SharedPreferences
        // Check if email exists and password matches
        val storedPassword = allCredentials[credentials.email]
        if (storedPassword == null || storedPassword != credentials.password) {
            return Result.failure(Exception("Invalid email or password"))
        }
        
        // Find user by email from UserDao
        val user = userDao.getUserByEmailSuspend(credentials.email)
            ?: return Result.failure(Exception("User not found for email: ${credentials.email}"))
        
        // Generate token and save auth data
        val token = UUID.randomUUID().toString()
        saveAuthData(token, user) // Use user from DAO
        
        return Result.success(
            AuthResponse(
                token = token,
                userId = user.id,
                username = user.username,
                email = user.email
            )
        )
    }
    
    suspend fun logout() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(USER_ID_KEY)
            preferences.remove(USERNAME_KEY)
            preferences.remove(EMAIL_KEY)
        }
    }
    
    val authState: Flow<AuthState> = context.dataStore.data.map { preferences ->
        val token = preferences[TOKEN_KEY]
        val userId = preferences[USER_ID_KEY]
        val username = preferences[USERNAME_KEY]
        val email = preferences[EMAIL_KEY]
        
        if (token != null && userId != null && username != null && email != null) {
            AuthState.Authenticated(
                token = token,
                userId = userId,
                username = username,
                email = email
            )
        } else {
            AuthState.Unauthenticated
        }
    }
    
    private suspend fun saveAuthData(token: String, user: User) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[USER_ID_KEY] = user.id
            preferences[USERNAME_KEY] = user.username
            preferences[EMAIL_KEY] = user.email
        }
    }
    
    suspend fun updateUserProfile(userId: String, newUsername: String, newEmail: String): Result<User> {
        return try {
            val currentUser = userDao.getUserByIdSuspend(userId)
                ?: return Result.failure(Exception("User not found to update"))

            val updatedUser = currentUser.copy(username = newUsername, email = newEmail)

            if (currentUser.email != newEmail) {
                // Email changed, update credentials map
                val credentials = loadCredentials()
                val password = credentials.remove(currentUser.email) // Remove old email entry
                if (password != null) {
                    credentials[newEmail] = password // Add new email entry with old password
                    saveCredentials(credentials)
                }
            }

            userDao.updateUser(updatedUser)

            // If this is the currently logged-in user, update DataStore session
            val currentSessionUserId = context.dataStore.data.firstOrNull()?.get(USER_ID_KEY)
            if (currentSessionUserId == userId) {
                context.dataStore.edit {
                    it[USERNAME_KEY] = newUsername
                    it[EMAIL_KEY] = newEmail
                }
            }
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update profile: ${e.message}"))
        }
    }

    suspend fun deleteUserAccount(userId: String): Result<Unit> {
        return try {
            val userToDelete = userDao.getUserByIdSuspend(userId)
                ?: return Result.failure(Exception("User not found to delete"))

            // Proceed to delete from Room (cascade will handle habits, etc.)
            val deletedRows = userDao.deleteUserById(userId)
            if (deletedRows == 0) {
                return Result.failure(Exception("Failed to delete user from database"))
            }

            // Remove credentials from SharedPreferences
            val credentials = loadCredentials()
            credentials.remove(userToDelete.email)
            saveCredentials(credentials)

            // Log out the user by clearing DataStore
            logout()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to delete account: ${e.message}"))
        }
    }
}
