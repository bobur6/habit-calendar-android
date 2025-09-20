package com.bobur.habitcalendarchecker.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bobur.habitcalendarchecker.data.auth.AuthState
import com.bobur.habitcalendarchecker.ui.viewmodels.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit, // Changed from onLogout for clarity after delete
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val authState by authViewModel.authState.collectAsState()
    val updateProfileState by authViewModel.updateProfileState.collectAsState()
    val deleteAccountState by authViewModel.deleteAccountState.collectAsState()

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current // For Toasts or other context needs

    // Initialize fields when authState is authenticated
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            val user = authState as AuthState.Authenticated
            username = user.username
            email = user.email
            isEditing = false // Reset editing mode if auth state changes
        } else if (authState is AuthState.Unauthenticated) {
            // If user becomes unauthenticated (e.g., after deletion), navigate to login
            onNavigateToLogin()
        }
    }

    // Handle Update Profile State changes
    LaunchedEffect(updateProfileState) {
        when (val state = updateProfileState) {
            is AuthViewModel.UpdateProfileState.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Profile updated successfully!")
                }
                isEditing = false
                username = state.updatedUser.username // Ensure UI reflects the very latest
                email = state.updatedUser.email
                authViewModel.resetUpdateProfileState()
            }
            is AuthViewModel.UpdateProfileState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Error: ${state.message}")
                }
                authViewModel.resetUpdateProfileState()
            }
            else -> { /* Idle or Loading */ }
        }
    }

    // Handle Delete Account State changes
    LaunchedEffect(deleteAccountState) {
        when (val state = deleteAccountState) {
            is AuthViewModel.DeleteAccountState.Success -> {
                // authState will become Unauthenticated, triggering navigation via LaunchedEffect(authState)
                // Snackbar might be shown briefly before navigation
                scope.launch {
                    snackbarHostState.showSnackbar("Account deleted successfully.")
                }
                authViewModel.resetDeleteAccountState() 
                // Navigation to login is handled by observing authState
            }
            is AuthViewModel.DeleteAccountState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Error: ${state.message}")
                }
                authViewModel.resetDeleteAccountState()
            }
            else -> { /* Idle or Loading */ }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isEditing) isEditing = false else onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (authState is AuthState.Authenticated) {
                        if (isEditing) {
                            IconButton(onClick = {
                                (authState as? AuthState.Authenticated)?.let {
                                    authViewModel.updateUserProfile(it.userId, username, email)
                                }
                            }, enabled = updateProfileState !is AuthViewModel.UpdateProfileState.Loading) {
                                if (updateProfileState is AuthViewModel.UpdateProfileState.Loading) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                } else {
                                    Icon(Icons.Default.Save, contentDescription = "Save Changes")
                                }
                            }
                        } else {
                            IconButton(onClick = { isEditing = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                            }
                        }
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (authState is AuthState.Authenticated) {
            val user = authState as AuthState.Authenticated
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .padding(24.dp)
                            .size(72.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

                if (isEditing) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done)
                    )
                } else {
                    Text(text = username, style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = email, style = MaterialTheme.typography.bodyLarge)
                }

                Spacer(modifier = Modifier.height(32.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                ProfileInfoItem(
                    title = "Account ID",
                    value = user.userId
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        authViewModel.logout()
                        // onNavigateToLogin() will be called due to authState change
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Logout")
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { showDeleteConfirmDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    if (deleteAccountState is AuthViewModel.DeleteAccountState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.error)
                    } else {
                        Text("Delete Account")
                    }
                }
            }
        } else if (authState is AuthState.Unauthenticated && authViewModel.loginState.value !is AuthViewModel.LoginState.Loading) {
            // Show a loading indicator or placeholder if unauthenticated and not actively logging in,
            // as the LaunchedEffect should handle navigation.
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                // CircularProgressIndicator() // Or some other placeholder
                 Text("Not authenticated. Redirecting...") // Placeholder, should be quick
            }
        }

        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text("Delete Account?") },
                text = { Text("Are you sure you want to delete your account? This action cannot be undone and all your data will be lost.") },
                confirmButton = {
                    Button(
                        onClick = {
                            (authState as? AuthState.Authenticated)?.let {
                                authViewModel.deleteUserAccount(it.userId)
                            }
                            showDeleteConfirmDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun ProfileInfoItem(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge, // Adjusted style for better hierarchy
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
