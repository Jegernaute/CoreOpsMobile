package com.example.coreops.ui.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coreops.ui.projects.components.ProjectCard

// Фоновий колір з дизайн-системи
private val CoreOpsBg = Color(0xFFF3F4F6)
private val CoreOpsPrimary = Color(0xFF2563EB)
private val CoreOpsTextPrimary = Color(0xFF111827)
private val CoreOpsTextSecondary = Color(0xFF6B7280)

@Composable
fun ProjectsScreen(
    viewModel: ProjectsViewModel = hiltViewModel(),
    onProjectClick: (Int) -> Unit // Передає ID для майбутнього переходу на екран деталей
) {
    // Підписує на стан з ViewModel
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CoreOpsBg)
    ) {
        // --- Заголовок Екрану ---
        Text(
            text = "Проєкти",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = CoreOpsTextPrimary,
            modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 16.dp)
        )

        // --- Контейнер для різних станів ---
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (val currentState = state) {
                // 1. Стан завантаження
                is ProjectsState.Loading -> {
                    CircularProgressIndicator(color = CoreOpsPrimary)
                }

                // 2. Стан помилки (з кнопкою оновлення)
                is ProjectsState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = currentState.message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadProjects() },
                            colors = ButtonDefaults.buttonColors(containerColor = CoreOpsPrimary)
                        ) {
                            Text("Спробувати знову", color = Color.White)
                        }
                    }
                }

                // 3. Стан успіху (Список проєктів)
                is ProjectsState.Success -> {
                    if (currentState.projects.isEmpty()) {
                        // Якщо масив порожній
                        Text(
                            text = "У вас ще немає доступних проєктів.",
                            fontSize = 16.sp,
                            color = CoreOpsTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        // Якщо є дані — малює список
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(currentState.projects) { project ->
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
}