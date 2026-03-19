package com.example.coreops.ui.projects.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coreops.data.remote.models.ProjectDto

// Кольори з нашої дизайн-системи
private val CoreOpsSurface = Color(0xFFFFFFFF)
private val CoreOpsTextPrimary = Color(0xFF111827)
private val CoreOpsTextSecondary = Color(0xFF6B7280)
private val CoreOpsBorder = Color(0xFFE5E7EB)
private val CoreOpsPrimary = Color(0xFF2563EB)

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
        colors = CardDefaults.cardColors(containerColor = CoreOpsSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CoreOpsBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // --- 1. Верхній рядок: Ключ проєкту та Статус ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Бейдж із ключем (наприклад, "ALF")
                Text(
                    text = project.key.uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CoreOpsPrimary,
                    modifier = Modifier
                        .background(CoreOpsPrimary.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )

                // Динамічний колір статусу
                val statusColor = when (project.status.lowercase()) {
                    "active", "активний" -> Color(0xFF10B981)
                    "completed", "завершено" -> Color(0xFF6B7280)
                    "on hold", "на паузі" -> Color(0xFFF59E0B)
                    else -> CoreOpsPrimary
                }

                // Бейдж статусу
                Text(
                    text = project.status,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = statusColor,
                    modifier = Modifier
                        .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- 2. Головна назва проєкту ---
            Text(
                text = project.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = CoreOpsTextPrimary
            )

            // --- 3. Опис (показує, тільки якщо він є) ---
            if (!project.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = project.description,
                    fontSize = 14.sp,
                    color = CoreOpsTextSecondary,
                    maxLines = 2, // Обмежуємо 2 рядками, щоб картка не була гігантською
                    overflow = TextOverflow.Ellipsis // Додає "..." в кінці, якщо текст не влазить
                )
            }
        }
    }
}