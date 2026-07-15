package com.example.coreops.ui.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coreops.data.remote.models.ProjectDto
import com.example.coreops.ui.projects.components.ProjectCard
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

private val CoreOpsBg = Color(0xFFF3F4F6)
private val CoreOpsTextSecondary = Color(0xFF6B7280)

@Composable
fun ProjectsScreen(
    viewModel: ProjectsViewModel = hiltViewModel(),
    onProjectClick: (Int) -> Unit
) {
    val currentState by viewModel.state.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val ordering by viewModel.ordering.collectAsState()
    val showArchived by viewModel.showArchived.collectAsState()

    // Нові стейти
    val currentStatus by viewModel.status.collectAsState()
    val hasActiveTasks by viewModel.hasActiveTasks.collectAsState()
    val isCompleted by viewModel.isCompleted.collectAsState()

    val avatarUrl by viewModel.avatarUrl.collectAsState()

    ProjectsContent(
        state = currentState,
        searchQuery = searchQuery,
        ordering = ordering,
        showArchived = showArchived,
        currentStatus = currentStatus,
        hasActiveTasks = hasActiveTasks,
        isCompleted = isCompleted,
        avatarUrl = avatarUrl,
        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
        onStatusChange = { viewModel.updateStatus(it) },
        onApplyFilters = { newOrdering, includeArchived, activeTasksOnly, completedOnly ->
            viewModel.updateFilters(newOrdering, includeArchived, activeTasksOnly, completedOnly)
        },
        onProjectClick = onProjectClick,
        onTryAgain = { viewModel.loadProjects() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsContent(
    state: ProjectsState,
    searchQuery: String,
    ordering: String?,
    showArchived: Boolean,
    currentStatus: String?,
    hasActiveTasks: Boolean?,
    isCompleted: Boolean?,
    avatarUrl: String?,
    onSearchQueryChange: (String) -> Unit,
    onStatusChange: (String?) -> Unit,
    onApplyFilters: (String?, Boolean, Boolean?, Boolean?) -> Unit,
    onProjectClick: (Int) -> Unit,
    onTryAgain: () -> Unit = {}
) {
    // Стан для перемикання між заголовком та рядком пошуку
    var isSearchActive by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CoreOpsBg)
    ) {
        // --- 1. Кастомна шапка (TopBar) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSearchActive) {
                // Відображення рядка пошуку
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Пошук проєктів...") },
                    leadingIcon = {
                        IconButton(onClick = {
                            isSearchActive = false
                            onSearchQueryChange("") // Очищає пошук при закритті
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                        }
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Filled.Close, contentDescription = "Очистити")
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(50),
                    singleLine = true
                )
            } else {
                // Стандартне відображення заголовку
                Text(
                    text = "Проєкти",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { isSearchActive = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Пошук",
                            tint = Color.Black
                        )
                    }

                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = "Фільтри",
                            tint = if (ordering != null || showArchived || hasActiveTasks != null || isCompleted != null) Color(0xFF2563EB) else Color.Black
                        )
                    }

                    IconButton(onClick = { /* TODO */ }) {
                        if (!avatarUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = "Профіль",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = "Профіль",
                                tint = Color(0xFF2563EB),
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }
                }
            }
        }

        // --- 1.5 Чіпи для фільтрації за статусом ---
        val statuses = listOf(
            null to "Усі",
            "backlog" to "В планах",
            "in_progress" to "В роботі",
            "on_hold" to "На паузі",
            "completed" to "Завершено"
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(statuses) { (statusValue, label) ->
                val isSelected = currentStatus == statusValue
                Surface(
                    onClick = { onStatusChange(statusValue) },
                    shape = RoundedCornerShape(50),
                    color = if (isSelected) Color(0xFF2563EB) else Color.White,
                    border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)) else null,
                    modifier = Modifier.defaultMinSize(minHeight = 36.dp)
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        color = if (isSelected) Color.White else Color(0xFF4B5563),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // --- 2. Основний контент (Стани) ---
        Box(modifier = Modifier.fillMaxSize()) {
            when (state) {
                is ProjectsState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF2563EB)
                    )
                }

                is ProjectsState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            color = Color.Red,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onTryAgain,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                        ) {
                            Text("Спробувати знову", color = Color.White)
                        }
                    }
                }

                is ProjectsState.Success -> {
                    if (state.projects.isEmpty()) {
                        Text(
                            text = "У вас ще немає доступних проєктів.",
                            fontSize = 16.sp,
                            color = CoreOpsTextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 100.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.projects) { project ->
                                ProjectCard(
                                    project = project,
                                    onClick = { onProjectClick(project.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    // --- 3. BottomSheet для фільтрів ---
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = sheetState
        ) {
            // Локальний стейт для керування вибором до натискання "Застосувати"
            var localOrdering by remember { mutableStateOf(ordering) }
            var localShowArchived by remember { mutableStateOf(showArchived) }
            var localHasActiveTasks by remember { mutableStateOf(hasActiveTasks ?: false) }
            var localIsCompleted by remember { mutableStateOf(isCompleted ?: false) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text("Фільтри та сортування", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Показувати архівовані", fontSize = 16.sp)
                    Switch(
                        checked = localShowArchived,
                        onCheckedChange = { localShowArchived = it }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Тільки з активними задачами", fontSize = 16.sp)
                    Switch(
                        checked = localHasActiveTasks,
                        onCheckedChange = { localHasActiveTasks = it }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Тільки завершені", fontSize = 16.sp)
                    Switch(
                        checked = localIsCompleted,
                        onCheckedChange = { localIsCompleted = it }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFE5E7EB))
                Spacer(modifier = Modifier.height(16.dp))

                Text("Сортування", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(12.dp))

                val sortOptions = listOf(
                    null to "За замовчуванням",
                    "name" to "За назвою",
                    "-start_date" to "Спочатку нові",
                    "start_date" to "Спочатку старі"
                )

                sortOptions.forEach { (value, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { localOrdering = value }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = localOrdering == value,
                            onClick = { localOrdering = value },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2563EB))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(label, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        // Перетворення Switch Boolean у Boolean? (null якщо вимкнено)
                        onApplyFilters(
                            localOrdering,
                            localShowArchived,
                            localHasActiveTasks.takeIf { it },
                            localIsCompleted.takeIf { it }
                        )
                        showFilterSheet = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("Застосувати", color = Color.White)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// --- Preview для тестування дизайну ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProjectsScreenPreview() {
    val fakeProjects = listOf(
        ProjectDto(
            id = 1, key = "CRM", name = "Внутрішня CRM", description = null,
            status = "in_progress", activeTasksCount = 12, progress = 0.65f
        ),
        ProjectDto(
            id = 2, key = "MVP", name = "Мобільний додаток MVP", description = null,
            status = "on_hold", activeTasksCount = 3, progress = 0.0f
        ),
        ProjectDto(
            id = 3, key = "API", name = "Розробка API", description = null,
            status = "backlog", activeTasksCount = 24, progress = 0.10f
        )
    )

    ProjectsContent(
        state = ProjectsState.Success(fakeProjects),
        searchQuery = "",
        ordering = null,
        showArchived = false,
        currentStatus = null,
        hasActiveTasks = null,
        isCompleted = null,
        avatarUrl = null,
        onSearchQueryChange = {},
        onStatusChange = {},
        onApplyFilters = { _, _, _, _ -> },
        onProjectClick = {}
    )
}