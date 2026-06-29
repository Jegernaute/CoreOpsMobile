package com.example.coreops.ui.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

private val CoreOpsBg = Color(0xFFF3F4F6)
private val CoreOpsTextSecondary = Color(0xFF6B7280)

@Composable
fun ProjectsScreen(
    viewModel: ProjectsViewModel = hiltViewModel(),
    onProjectClick: (Int) -> Unit
) {
    val currentState by viewModel.state.collectAsState()

    ProjectsContent(
        state = currentState,
        onProjectClick = onProjectClick,
        onTryAgain = { viewModel.loadProjects() }
    )
}

@Composable
fun ProjectsContent(
    state: ProjectsState,
    onProjectClick: (Int) -> Unit,
    onTryAgain: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CoreOpsBg)
    ) {
        // --- 1. Кастомна шапка (TopBar) - Відносне центрування ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Заголовок займає весь залишковий простір та центрується всередині нього
            Text(
                text = "Проєкти",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            // Група іконок (автоматично притискається вправо завдяки weight(1f) у текста)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Пошук",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )

                Icon(
                    imageVector = Icons.Outlined.Tune,
                    contentDescription = "Фільтри",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )

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
        onProjectClick = {}
    )
}