package com.bobur.habitcalendarchecker.ui.screens.habitlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bobur.habitcalendarchecker.data.model.Habit
import com.bobur.habitcalendarchecker.data.model.HabitCheck
import com.bobur.habitcalendarchecker.data.model.HabitList
import com.bobur.habitcalendarchecker.ui.components.HabitDetailDialog
import com.bobur.habitcalendarchecker.ui.viewmodels.HabitViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.Divider
import androidx.compose.material3.TextButton
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HabitListDetailScreen(
    @Suppress("UNUSED_PARAMETER") listId: Long, // Не используется напрямую, но нужен для совместимости
    habitViewModel: HabitViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val habits by habitViewModel.habits.collectAsState(initial = emptyList())
    val habitList by habitViewModel.habitList.collectAsState(initial = null)
    val createHabitState by habitViewModel.createHabitState.collectAsState()
    val updateHabitListState by habitViewModel.updateHabitListState.collectAsState()

    var showCreateHabitDialog by remember { mutableStateOf(false) }
    var newHabitName by remember { mutableStateOf("") }
    var showEditListDialog by remember { mutableStateOf(false) }
    var editedListName by remember { mutableStateOf("") }

    var selectedHabitForDetailDialog by remember { mutableStateOf<Habit?>(null) }

    var currentWeekStart by remember { mutableStateOf(LocalDate.now().with(DayOfWeek.MONDAY)) }
    val today = LocalDate.now()
    val dateRange = remember(currentWeekStart) { (0..6).map { currentWeekStart.plusDays(it.toLong()) } }
    val currentYearMonth = remember(currentWeekStart) { YearMonth.from(currentWeekStart.plusDays(3)) }

    val habitsWithChecks by habitViewModel.habitsWithChecks.collectAsState()

    LaunchedEffect(habits, currentWeekStart) {
        habitViewModel.loadHabitChecksForWeek(currentWeekStart)
    }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(habitList) {
        habitList?.let {
            editedListName = it.name
        }
    }

    LaunchedEffect(createHabitState) {
        when (val state = createHabitState) {
            is HabitViewModel.CreateHabitState.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Habit '${state.habitName}' created!")
                }
                showCreateHabitDialog = false
                newHabitName = ""
                habitViewModel.resetCreateHabitState()
            }
            is HabitViewModel.CreateHabitState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Error: ${state.message}")
                }
                habitViewModel.resetCreateHabitState()
            }
            else -> {}
        }
    }

    LaunchedEffect(updateHabitListState) {
        when (val state = updateHabitListState) {
            is HabitViewModel.UpdateHabitListState.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar("List name updated!")
                }
                showEditListDialog = false
                habitViewModel.resetUpdateHabitListState()
            }
            is HabitViewModel.UpdateHabitListState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Error: ${state.message}")
                }
                habitViewModel.resetUpdateHabitListState()
            }
            else -> {}
        }
    }

    val swipeHandler = Modifier.pointerInput(Unit) {
        var distance = 0f

        detectHorizontalDragGestures(
            onDragStart = { /* Начало свайпа */ },
            onDragEnd = {
                if (abs(distance) > 50) { // Уменьшаем порог для более легкого свайпа
                    if (distance < 0) { // Свайп влево - следующая неделя
                        currentWeekStart = currentWeekStart.plusWeeks(1)
                    } else { // Свайп вправо - предыдущая неделя
                        currentWeekStart = currentWeekStart.minusWeeks(1)
                    }
                }
                distance = 0f
            },
            onDragCancel = { distance = 0f },
            onHorizontalDrag = { _, dragAmount ->
                distance += dragAmount
            }
        )
    }

    if (habitList == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(habitList?.name ?: "Habit List") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        habitList?.let { editedListName = it.name }
                        showEditListDialog = true
                    }) {
                        Icon(Icons.Default.Edit, "Edit List Name")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateHabitDialog = true }) {
                Icon(Icons.Default.Add, "Add Habit")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .then(swipeHandler)
        ) {
            MonthHeader(currentYearMonth, currentWeekStart, onWeekChange = { newStartOfWeek -> currentWeekStart = newStartOfWeek })
            WeekDayHeaders(dateRange, today)

            if (habits.isEmpty() && createHabitState !is HabitViewModel.CreateHabitState.Loading) {
                EmptyHabitListState(onAddHabitClicked = { showCreateHabitDialog = true })
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(habits, key = { it.id }) { habit ->
                        HabitRow(
                            modifier = Modifier.animateItemPlacement(),
                            habit = habit,
                            dateRange = dateRange,
                            today = today,
                            habitChecks = habitsWithChecks[habit.id] ?: emptyList(),
                            habitViewModel = habitViewModel,
                            onHabitLongPress = { longPressedHabit ->
                                selectedHabitForDetailDialog = longPressedHabit
                            }
                        )
                        Divider()
                    }
                }
            }
        }
    }

    if (showCreateHabitDialog) {
        CreateHabitDialog(
            onDismiss = { showCreateHabitDialog = false; newHabitName = "" },
            onConfirm = {
                if (newHabitName.isNotBlank()) {
                    habitViewModel.createHabit(newHabitName)
                }
            },
            habitName = newHabitName,
            onHabitNameChange = { newHabitName = it },
            isLoading = createHabitState is HabitViewModel.CreateHabitState.Loading
        )
    }

    if (showEditListDialog && habitList != null) {
        EditHabitListDialog(
            initialListName = habitList?.name ?: "",
            onDismiss = { showEditListDialog = false },
            onConfirm = { newName ->
                if (newName.isNotBlank()) {
                    habitViewModel.updateHabitList(newName)
                }
            },
            isLoading = updateHabitListState is HabitViewModel.UpdateHabitListState.Loading
        )
    }

    selectedHabitForDetailDialog?.let { habit ->
        HabitDetailDialog(
            habit = habit,
            habitChecks = habitsWithChecks[habit.id] ?: emptyList(), 
            onDismiss = { selectedHabitForDetailDialog = null },
            onEditHabit = { habitId, newName ->
                habitViewModel.updateHabitName(habitId, newName)
                selectedHabitForDetailDialog = null
                scope.launch { snackbarHostState.showSnackbar("Habit updated!") }
            },
            onDeleteHabit = { habitId ->
                habitViewModel.deleteHabit(habitId)
                selectedHabitForDetailDialog = null
                scope.launch { snackbarHostState.showSnackbar("Habit deleted.") }
            },
            onClearHabitChecks = { habitId -> 
                habitViewModel.clearAllHabitChecks(habitId)
                scope.launch { snackbarHostState.showSnackbar("All checks for '${habit.name}' cleared.") }
            }
        )
    }
}

@Composable
fun EmptyHabitListState(onAddHabitClicked: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("No habits in this list yet.", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onAddHabitClicked) {
            Text("Add your first habit")
        }
    }
}

@Composable
fun EditHabitListDialog(
    initialListName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    isLoading: Boolean
) {
    var listName by remember { mutableStateOf(initialListName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit List Name") },
        text = {
            OutlinedTextField(
                value = listName,
                onValueChange = { listName = it },
                label = { Text("List Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(listName) },
                enabled = listName.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(Modifier.size(ButtonDefaults.IconSize))
                } else {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun MonthHeader(
    currentYearMonth: YearMonth,
    currentWeekStartDate: LocalDate,
    onWeekChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = { onWeekChange(currentWeekStartDate.minusWeeks(1)) }) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowLeft,
                contentDescription = "Previous week",
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = "${currentYearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentYearMonth.year}",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        IconButton(onClick = { onWeekChange(currentWeekStartDate.plusWeeks(1)) }) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = "Next week",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun WeekDayHeaders(
    dateRange: List<LocalDate>,
    today: LocalDate,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier.weight(2.5f),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            dateRange.forEach { date ->
                DateHeader(
                    date = date,
                    isToday = date.isEqual(today),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DateHeader(
    date: LocalDate,
    isToday: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            shape = CircleShape,
            modifier = Modifier.size(32.dp),
            color = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
            contentColor = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun HabitRow(
    modifier: Modifier,
    habit: Habit,
    dateRange: List<LocalDate>,
    today: LocalDate,
    habitChecks: List<HabitCheck>,
    habitViewModel: HabitViewModel,
    onHabitLongPress: (Habit) -> Unit
) {
    var showCheckDialog by remember { mutableStateOf(false) }
    var selectedDateForDialog by remember { mutableStateOf<LocalDate?>(null) }
    var emojiForDialog by remember { mutableStateOf("✅") }
    var noteForDialog by remember { mutableStateOf("") }

    val checksMap = remember(habitChecks) {
        habitChecks.associateBy { it.date }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = habit.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
                .clickable(onClick = { onHabitLongPress(habit) }),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Row(
            modifier = Modifier.weight(2.5f),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            dateRange.forEach { date ->
                val epochDay = HabitCheck.fromLocalDate(date)
                val check = checksMap[epochDay]
                
                HabitCell(
                    modifier = Modifier.weight(1f),
                    check = check,
                    isToday = date.isEqual(today),
                    onToggle = {
                        if (check != null) {
                            habitViewModel.deleteHabitCheck(habit.id, date)
                        } else {
                            habitViewModel.createOrUpdateHabitCheck(habit.id, date, "✅", null)
                        }
                    },
                    onLongClick = {
                        selectedDateForDialog = date
                        if (check != null) {
                            emojiForDialog = check.emoji
                            noteForDialog = check.note ?: ""
                        } else {
                            emojiForDialog = "✅"
                            noteForDialog = ""
                        }
                        showCheckDialog = true
                    }
                )
            }
        }
    }

    if (showCheckDialog) {
        selectedDateForDialog?.let { currentDate ->
            HabitCheckDialog(
                date = currentDate,
                initialEmoji = emojiForDialog,
                initialNote = noteForDialog,
                onDismiss = { showCheckDialog = false },
                onConfirm = { confirmedEmoji, confirmedNote ->
                    habitViewModel.createOrUpdateHabitCheck(habit.id, currentDate, confirmedEmoji, confirmedNote)
                    showCheckDialog = false
                }
            )
        }
    }
}

@Composable
fun HabitCell(
    check: HabitCheck?,
    isToday: Boolean,
    onToggle: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val  backgroundColor = when {
        check != null && isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
        check != null -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        isToday -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.surface
    }
    val contentColor = when {
        check != null -> MaterialTheme.colorScheme.onPrimaryContainer
        isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val border = if (isToday && check == null) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null

    Surface(
        modifier = modifier 
            .aspectRatio(1f) 
            .padding(2.dp) 
            .combinedClickable(
                onClick = onToggle,
                onLongClick = onLongClick,
                interactionSource = interactionSource,
                indication = null
            ),
        shape = MaterialTheme.shapes.medium,
        color = backgroundColor,
        border = border,
        tonalElevation = if (check != null) 4.dp else 1.dp,
        shadowElevation = if (check != null) 2.dp else 0.5.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (check != null) {
                Text(
                    text = check.emoji,
                    fontSize = 20.sp,
                    color = contentColor
                )
            } else {
                // Empty else block to show nothing for an empty cell
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitCheckDialog(
    date: LocalDate,
    initialEmoji: String,
    initialNote: String,
    onDismiss: () -> Unit,
    onConfirm: (emoji: String, note: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var emoji by remember { mutableStateOf(initialEmoji) }
    var note by remember { mutableStateOf(initialNote) }

    val emojis = remember {
        listOf("✅", "❌", "👍", "👎", "⭐", "🔥", "😊", "🤔", "💪", "🏃", "🍎", "💤", "💧", "📚", "🎉", "💡")
    }

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Text("Check-in for ${date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}")
        },
        text = {
            Column {
                Text("Select Emoji:", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 48.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(emojis) { currentEmoji ->
                        Surface(
                            modifier = Modifier
                                .aspectRatio(1f),
                            shape = MaterialTheme.shapes.medium,
                            onClick = { emoji = currentEmoji },
                            color = if (emoji == currentEmoji) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = if (emoji == currentEmoji) 4.dp else 1.dp
                        ) {
                            Box(
                                contentAlignment = Alignment.Center, 
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp)
                            ) {
                                Text(currentEmoji, fontSize = 20.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Note (optional):", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Add a comment...") },
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(emoji, note) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHabitDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    habitName: String,
    onHabitNameChange: (String) -> Unit,
    isLoading: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Habit") },
        text = {
            Column {
                OutlinedTextField(
                    value = habitName,
                    onValueChange = onHabitNameChange,
                    label = { Text("Habit Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = habitName.isNotBlank() && !isLoading
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