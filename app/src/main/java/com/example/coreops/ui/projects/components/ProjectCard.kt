package com.example.coreops.ui.projects.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coreops.data.remote.models.ProjectDto

@Composable
fun ProjectCard(
    project: ProjectDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // --- 1. Верхній рядок: Назва та Статус ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = project.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Мапінг системних статусів у локалізований текст та кольори
                val (statusText, statusBg, statusColor) = when (project.status.lowercase()) {
                    "backlog" -> Triple("В планах", Color(0xFFE0F2FE), Color(0xFF0284C7))
                    "in_progress" -> Triple("В роботі", Color(0xFFFEF3C7), Color(0xFFD97706))
                    "on_hold" -> Triple("На паузі", Color(0xFFF3E8FF), Color(0xFF7E22CE))
                    "completed" -> Triple("Завершено", Color(0xFFD1FAE5), Color(0xFF059669))
                    "archived" -> Triple("Архів", Color(0xFFF3F4F6), Color(0xFF4B5563))
                    else -> Triple("Невідомо", Color(0xFFF3F4F6), Color(0xFF4B5563))
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBg
                ) {
                    Text(
                        text = statusText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. Середній рядок: Задачі та Відсоток ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${project.activeTasksCount} активних задач",
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                Text(
                    text = "${(project.progress * 100).toInt()}%",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- 3. Нижній рядок: Смуга прогресу ---
            LinearProgressIndicator(
                progress = { project.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFF2563EB),
                trackColor = Color(0xFFE5E7EB),
                strokeCap = StrokeCap.Round
            )
        }
    }
}