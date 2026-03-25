package com.example.coreops.ui.tasks

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    projectId: Int,
    viewModel: CreateTaskViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onTaskCreated: () -> Unit
) {
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val taskType by viewModel.taskType.collectAsState()
    val priority by viewModel.priority.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is CreateTaskState.Success) {
            onTaskCreated()
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Нова задача", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- ПОЛЕ: НАЗВА ---
            OutlinedTextField(
                value = title,
                onValueChange = { viewModel.setTitle(it) },
                label = { Text("Назва задачі *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState is CreateTaskState.Error && title.isBlank()
            )

            // --- ПОЛЕ: ОПИС ---
            OutlinedTextField(
                value = description,
                onValueChange = { viewModel.setDescription(it) },
                label = { Text("Опис (опціонально)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )

            // --- ВИБІР: ТИП ЗАДАЧІ ---
            Text("Тип задачі", fontWeight = FontWeight.Medium, fontSize = 14.sp)
            val taskTypes = listOf(
                "task" to "Задача",
                "bug" to "Баг",
                "feature" to "Фіча"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                taskTypes.forEach { (backendValue, displayLabel) ->
                    FilterChip(
                        selected = taskType == backendValue,
                        onClick = { viewModel.setTaskType(backendValue) },
                        label = { Text(displayLabel) }
                    )
                }
            }

            // --- ВИБІР: ПРІОРИТЕТ ---
            Text("Пріоритет", fontWeight = FontWeight.Medium, fontSize = 14.sp)
            val priorities = listOf(
                "low" to "Низький",
                "medium" to "Середній",
                "high" to "Високий",
                "critical" to "Критичний"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                priorities.forEach { (backendValue, displayLabel) ->
                    FilterChip(
                        selected = priority == backendValue,
                        onClick = { viewModel.setPriority(backendValue) },
                        label = { Text(displayLabel) }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- ВІДОБРАЖЕННЯ ПОМИЛКИ ---
            if (uiState is CreateTaskState.Error) {
                Text(
                    text = (uiState as CreateTaskState.Error).message,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            // --- КНОПКА СТВОРЕННЯ ---
            Button(
                onClick = { viewModel.submitTask() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = uiState !is CreateTaskState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                if (uiState is CreateTaskState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Створити задачу", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}