package com.example.coreops.ui.notifications

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coreops.data.remote.models.NotificationDto
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Notifications
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.ui.text.style.TextAlign
@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    NotificationsContent(
        state = state,
        onNotificationClick = { id -> viewModel.markAsRead(id) },
        onMarkAllRead = { viewModel.markAllAsRead() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsContent(
    state: NotificationState,
    onNotificationClick: (Int) -> Unit,
    onMarkAllRead: () -> Unit
) {
    Scaffold(
        containerColor = Color(0xFFF3F4F6),
        topBar = {
            TopAppBar(
                title = {
                    Text("Сповіщення", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                },
                actions = {
                    TextButton(onClick = onMarkAllRead) {
                        Icon(
                            imageVector = Icons.Default.DoneAll, // Подвійна галочка з макету
                            contentDescription = "Прочитати всі",
                            modifier = Modifier.size(20.dp),
                            tint = Color(0xFF2563EB)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Прочитано",
                            color = Color(0xFF2563EB),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF3F4F6))
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state) {
                is NotificationState.Loading -> {
                    CircularProgressIndicator(
                        color = Color(0xFF2563EB),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is NotificationState.Error -> {
                    Text(
                        text = state.message,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                is NotificationState.Success -> {
                    val notifications = state.notifications

                    if (notifications.isEmpty()) {
                        Text(
                            text = "У вас немає нових сповіщень",
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(notifications) { notif ->
                                NotificationItem(
                                    notification = notif,
                                    onClick = { if (!notif.isRead) {
                                        onNotificationClick(notif.id)
                                    } }
                                )
                            }

                            item {
                                Text(
                                    text = "Немає нових сповіщень",
                                    color = Color(0xFF6B7280),
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp, bottom = 32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: NotificationDto,
    onClick: () -> Unit
) {
    // Кольори для непрочитаного (світло-синій) та прочитаного (білий) стану
    val backgroundColor = if (notification.isRead) Color.White else Color(0xFFF0F9FF)
    val titleWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold
    val titleColor = if (notification.isRead) Color(0xFF374151) else Color.Black

    Card(
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Синій індикатор непрочитаного сповіщення
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .size(8.dp)
                    .background(
                        color = if (notification.isRead) Color.Transparent else Color(0xFF3B82F6),
                        shape = CircleShape
                    )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Іконка дзвіночка
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFF3F4F6), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Сповіщення",
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Текстовий контент
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    fontWeight = titleWeight,
                    fontSize = 15.sp,
                    color = titleColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.message,
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280),
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatNotificationTime(notification.createdAt),
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
        }
    }
}
// Утиліта для форматування дати у відносний час
private fun formatNotificationTime(dateString: String?): String {
    if (dateString.isNullOrBlank()) return "Невідомо"
    return try {
        val past = ZonedDateTime.parse(dateString)
        val now = ZonedDateTime.now()

        val minutes = ChronoUnit.MINUTES.between(past, now)
        val hours = ChronoUnit.HOURS.between(past, now)
        val days = ChronoUnit.DAYS.between(past, now)

        when {
            minutes < 1 -> "Щойно"
            minutes < 60 -> "$minutes хв тому"
            hours < 24 -> "$hours год тому"
            days == 1L -> "Вчора"
            else -> {
                val day = past.dayOfMonth
                val month = when (past.monthValue) {
                    1 -> "січ."
                    2 -> "лют."
                    3 -> "бер."
                    4 -> "квіт."
                    5 -> "трав."
                    6 -> "черв."
                    7 -> "лип."
                    8 -> "серп."
                    9 -> "вер."
                    10 -> "жовт."
                    11 -> "лист."
                    12 -> "груд."
                    else -> ""
                }
                "$day $month"
            }
        }
    } catch (e: Exception) {
        dateString.take(10)
    }
}