package com.bobur.habitcalendarchecker.ui.screens.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bobur.habitcalendarchecker.data.auth.AuthState
import com.bobur.habitcalendarchecker.data.model.HabitList
import com.bobur.habitcalendarchecker.ui.viewmodels.HabitListViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    habitListViewModel: HabitListViewModel,
    onNavigateToHabitList: (Long) -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
        
        val habitLists by habitListViewModel.habitLists.collectAsState(initial = emptyList())
        val createListState by habitListViewModel.createListState.collectAsState()
        
        var showCreateListDialog by remember { mutableStateOf(false) }
        var newListName by remember { mutableStateOf("") }
        
        // State for delete confirmation dialog
        var showDeleteListConfirmDialog by remember { mutableStateOf(false) }
        var listToDelete by remember { mutableStateOf<HabitList?>(null) }
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Habit Calendar Checker") },
                    actions = {
                        IconButton(onClick = onNavigateToProfile) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile"
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showCreateListDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Habit List"
                    )
                }
            },
            modifier = modifier
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (habitLists.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No habit lists yet",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showCreateListDialog = true }
                        ) {
                            Text("Create your first habit list")
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(habitLists, key = { it.id }) { habitList ->
                            HabitListItem(
                                modifier = Modifier.animateItemPlacement(),
                                habitList = habitList,
                                onItemClick = { onNavigateToHabitList(habitList.id) },
                                onDeleteClick = {
                                    listToDelete = habitList
                                    showDeleteListConfirmDialog = true
                                }
                            )
                        }
                    }
                }
                
                if (showCreateListDialog) {
                    CreateHabitListDialog(
                        onDismiss = {
                            showCreateListDialog = false
                            newListName = ""
                        },
                        onConfirm = {
                            habitListViewModel.createHabitList(newListName)
                            showCreateListDialog = false
                            newListName = ""
                        },
                        listName = newListName,
                        onListNameChange = { newListName = it },
                        isLoading = createListState is HabitListViewModel.CreateListState.Loading
                    )
                }
                
                // Dialog for confirming habit list deletion
                if (showDeleteListConfirmDialog && listToDelete != null) {
                    AlertDialog(
                        onDismissRequest = {
                            showDeleteListConfirmDialog = false
                            listToDelete = null
                        },
                        title = { Text("Delete Habit List?") },
                        text = { Text("Are you sure you want to delete the list \"${listToDelete?.name}\"? All habits and progress within this list will also be deleted. This action cannot be undone.") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    listToDelete?.let { habitListViewModel.deleteHabitList(it) }
                                    showDeleteListConfirmDialog = false
                                    listToDelete = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Delete")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showDeleteListConfirmDialog = false
                                listToDelete = null
                            }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
                
                LaunchedEffect(createListState) {
                    if (createListState is HabitListViewModel.CreateListState.Success) {
                        val listId = (createListState as HabitListViewModel.CreateListState.Success).listId
                        onNavigateToHabitList(listId)
                        habitListViewModel.resetCreateListState()
                    }
                }
            }
        }
    }

@Composable
fun HabitListItem(
    habitList: HabitList,
    onItemClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = habitList.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Created: ${formatDate(habitList.createdAt)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHabitListDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    listName: String,
    onListNameChange: (String) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Habit List") },
        text = {
            Column {
                OutlinedTextField(
                    value = listName,
                    onValueChange = onListNameChange,
                    label = { Text("List Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = listName.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Create")
                }
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

// Helper function to format date
private fun formatDate(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}
