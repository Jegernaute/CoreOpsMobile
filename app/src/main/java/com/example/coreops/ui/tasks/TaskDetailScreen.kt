package com.example.coreops.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coreops.data.remote.models.ChecklistDto
import com.example.coreops.data.remote.models.CommentDto
import com.example.coreops.data.remote.models.HistoryEventDto
import com.example.coreops.data.remote.models.TaskDto
import com.example.coreops.data.remote.models.ResourceDto
import com.example.coreops.data.remote.models.UserDetailsDto
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
@Composable
fun TaskDetailScreen(
    viewModel: TaskDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val isSending by viewModel.isSendingComment.collectAsState()

    TaskDetailContent(
        state = state,
        isSending = isSending,
        onNavigateBack = onNavigateBack,
        onStatusChange = { newStatus ->
            if (state is TaskDetailState.Success) {
                viewModel.updateTaskStatus(
                    taskId = (state as TaskDetailState.Success).task.id,
                    newStatus = newStatus
                )
            }
        },
        onSendComment = { text -> viewModel.sendComment(text) },
        onToggleChecklist = { checklistId, isCompleted ->
            viewModel.toggleChecklistItem(checklistId, isCompleted)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailContent(
    state: TaskDetailState,
    isSending: Boolean,
    onNavigateBack: () -> Unit,
    onStatusChange: (String) -> Unit,
    onSendComment: (String) -> Unit,
    onToggleChecklist: (Int, Boolean) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    Scaffold(
        containerColor = Color(0xFFF3F4F6),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF3F4F6))
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = Color.Black
                    )
                }

                val titleText = if (state is TaskDetailState.Success) {
                    state.task.taskKey ?: "Деталі задачі"
                } else "Завантаження..."

                Text(
                    text = titleText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4B5563),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                IconButton(onClick = { /* TODO: Опції */ }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Опції",
                        tint = Color.Black
                    )
                }
            }
        },
        bottomBar = {
            if (state is TaskDetailState.Success && selectedTabIndex == 0) {
                ChatInputBar(
                    isSending = isSending,
                    onSendComment = onSendComment,
                    onAttachFile = { uri ->
                        Toast.makeText(context, "Вибрано файл: $uri", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
        ) {
            when (state) {
                is TaskDetailState.Loading -> {
                    CircularProgressIndicator(
                        color = Color(0xFF2563EB),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is TaskDetailState.Error -> {
                    Text(
                        text = state.message,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                is TaskDetailState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 24.dp, bottom = 16.dp)
                    ) {
                        item {
                            TaskHeader(
                                task = state.task,
                                onStatusChange = onStatusChange
                            )
                        }

                        if (state.task.checklist.isNotEmpty()) {
                            item {
                                ChecklistSection(
                                    checklist = state.task.checklist,
                                    onToggle = onToggleChecklist
                                )
                            }
                        }

                        item {
                            AttachmentsSection(resources = state.task.resources)
                        }

                        item {
                            TabsSection(
                                selectedTabIndex = selectedTabIndex,
                                commentsCount = state.comments.size,
                                onTabSelected = { selectedTabIndex = it }
                            )
                        }

                        if (selectedTabIndex == 0) {
                            if (state.comments.isEmpty()) {
                                item {
                                    Text(
                                        text = "Немає коментарів",
                                        color = Color.Gray,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            } else {
                                items(state.comments) { comment ->
                                    CommentBubble(comment = comment)
                                }
                            }
                        } else {
                            if (state.history.isEmpty()) {
                                item {
                                    Text(
                                        text = "Історія порожня",
                                        color = Color.Gray,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            } else {
                                items(state.history) { event ->
                                    HistoryItem(event = event)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskHeader(
    task: TaskDto,
    onStatusChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Заголовок
        Text(
            text = task.title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827),
            lineHeight = 28.sp
        )

        // Теги (Статус, Пріоритет, Тип)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusPillDropdown(
                currentStatus = task.status,
                onStatusChange = onStatusChange
            )
            TagPill(
                text = task.priority.replaceFirstChar { it.uppercase() },
                containerColor = Color(0xFFFEE2E2),
                textColor = Color(0xFFDC2626)
            )
            TagPill(
                text = task.taskType.replaceFirstChar { it.uppercase() },
                containerColor = Color(0xFFEEF2FF),
                textColor = Color(0xFF4F46E5)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Сітка: Виконавець / Автор
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Виконавець", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                UserRowItem(user = task.assigneeDetails, fallbackName = task.assigneeName)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Автор", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                UserRowItem(user = task.reporterDetails, fallbackName = task.reporterName)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Сітка: Дедлайн / Спринт
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Дедлайн", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = task.dueDate?.take(10) ?: "Не вказано",
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Спринт", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray) // ВИПРАВЛЕНО
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = task.sprintName ?: task.sprint?.let { "Sprint $it" } ?: "Немає",
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Опис
        Column {
            Text("Опис", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = task.description ?: "Опис відсутній",
                fontSize = 14.sp,
                color = Color(0xFF4B5563),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun UserRowItem(user: UserDetailsDto?, fallbackName: String?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val avatarUrl = user?.avatar

        if (!avatarUrl.isNullOrEmpty()) {
            coil.compose.AsyncImage(
                model = avatarUrl,
                contentDescription = "Аватар користувача",
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        } else {
            // Плейсхолдер для аватарки
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE5E7EB)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = user?.name ?: fallbackName ?: "Не призначено",
            fontSize = 14.sp,
            color = Color.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ChecklistSection(
    checklist: List<ChecklistDto>,
    onToggle: (Int, Boolean) -> Unit
) {
    val completedCount = checklist.count { it.isCompleted }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Підзадачі", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text("$completedCount/${checklist.size}", fontSize = 14.sp, color = Color.Gray)
        }
        Spacer(modifier = Modifier.height(12.dp))

        checklist.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle(item.id, item.isCompleted) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.isCompleted) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Done", tint = Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
                } else {
                    Icon(Icons.Outlined.Circle, contentDescription = "Todo", tint = Color(0xFFD1D5DB), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = item.content,
                    fontSize = 14.sp,
                    color = if (item.isCompleted) Color.Gray else Color.Black,
                    textDecoration = if (item.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                )
            }
        }
    }
}

@Composable
fun AttachmentsSection(resources: List<ResourceDto>) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text("Вкладення та лінки", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(12.dp))

        if (resources.isEmpty()) {
            Text(
                text = "Наразі до завдання не додано жодних файлів чи посилань",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(resources) { resource ->
                    // Визначає правильне посилання за логікою бекенду
                    val targetLink = resource.file ?: resource.url

                    if (targetLink.isNullOrBlank()) return@items // Якщо і те, і те порожнє - пропускає

                    // Визначає чи це файл за resourceType або розширенням
                    val isFile = resource.resourceType == "file" || resource.fileExtension != null

                    val icon = if (isFile) Icons.Default.InsertDriveFile else Icons.Default.Link
                    val iconBg = if (isFile) Color(0xFFE0F2FE) else Color(0xFFEEF2FF)
                    val iconTint = if (isFile) Color(0xFF0284C7) else Color(0xFF4F46E5)

                    val title = if (!resource.name.isNullOrBlank()) {
                        resource.name
                    } else {
                        if (isFile) "Вкладений файл" else "Зовнішнє посилання"
                    }

                    val subtitle = if (isFile) {
                        val sizeKb = (resource.fileSize ?: 0L) / 1024
                        val sizeText = if (sizeKb > 1024) "${sizeKb / 1024} MB" else "$sizeKb KB"
                        val ext = resource.fileExtension?.uppercase() ?: "FILE"
                        "$ext • $sizeText"
                    } else {
                        if (targetLink.isNotBlank()) {
                            try {
                                java.net.URI(targetLink).host?.removePrefix("www.") ?: "веб-посилання"
                            } catch (e: Exception) {
                                "веб-посилання"
                            }
                        } else {
                            "веб-посилання"
                        }
                    }

                    AttachmentCard(
                        icon = icon,
                        title = title,
                        subtitle = subtitle,
                        iconBg = iconBg,
                        iconTint = iconTint,
                        onClick = {
                            try {
                                // Підміна localhost для емулятора
                                val fixedUrl = targetLink
                                    .replace("127.0.0.1", "10.0.2.2")
                                    .replace("localhost", "10.0.2.2")

                                val validUrl = if (!fixedUrl.startsWith("http://") && !fixedUrl.startsWith("https://")) {
                                    "http://$fixedUrl"
                                } else {
                                    fixedUrl
                                }

                                Log.d("AttachmentClick", "Відкриваємо: $validUrl")

                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(validUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Log.e("AttachmentClick", "Помилка Intent", e)
                                Toast.makeText(context, "Помилка: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                }
            }
        }
    }
}
@Composable
fun AttachmentCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    iconBg: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp)
            .widthIn(min = 160.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.Black)
            Text(subtitle, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
fun TabsSection(
    selectedTabIndex: Int,
    commentsCount: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        TabItem(
            title = "Коментарі ($commentsCount)",
            isSelected = selectedTabIndex == 0,
            onClick = { onTabSelected(0) }
        )
        TabItem(
            title = "Історія",
            isSelected = selectedTabIndex == 1,
            onClick = { onTabSelected(1) }
        )
    }
    HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)
}

@Composable
fun TabItem(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            color = if (isSelected) Color(0xFF2563EB) else Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (isSelected) {
            Box(
                modifier = Modifier
                    .height(2.dp)
                    .fillMaxWidth(0.8f)
                    .background(Color(0xFF2563EB))
            )
        } else {
            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}

@Composable
fun CommentBubble(comment: CommentDto) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Аватар
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFFD1D5DB)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Ім'я та час
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comment.authorName ?: "Невідомий",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Text(
                    text = comment.createdAt.takeLast(8).take(5),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            // Бабл коментаря
            Box(
                modifier = Modifier
                    .background(
                        color = Color(0xFFF3F4F6),
                        shape = RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 12.dp,
                            bottomEnd = 12.dp,
                            bottomStart = 12.dp
                        )
                    )
                    .padding(12.dp)
            ) {
                Text(text = comment.content, fontSize = 14.sp, color = Color.Black, lineHeight = 20.sp)
            }
        }
    }
}

@Composable
fun HistoryItem(event: HistoryEventDto) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "${event.actor.name} - ${event.actionType}",
            fontSize = 14.sp,
            color = Color.Black
        )
        Text(
            text = event.timestamp.take(16).replace("T", " "),
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun ChatInputBar(
    isSending: Boolean,
    onSendComment: (String) -> Unit,
    onAttachFile: (Uri) -> Unit
) {
    var text by remember { mutableStateOf("") }

    // Лаунчер для відкриття системного файлового менеджера
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // Якщо користувач вибрав файл (uri != null) передає його далі
        uri?.let { onAttachFile(it) }
    }

    Surface(
        color = Color.White,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(24.dp))
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "Прикріпити",
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            // Запускає вибір файлу. "*/*" означає будь-який формат.
                            filePickerLauncher.launch("*/*")
                        }
                )

                TextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Написати коментар...", color = Color.Gray, fontSize = 14.sp) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        if (text.isNotBlank()) {
                            onSendComment(text)
                            text = ""
                        }
                    },
                    enabled = !isSending && text.isNotBlank()
                ) {
                    if (isSending) {
                        CircularProgressIndicator(color = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Надіслати",
                            tint = if (text.isNotBlank()) Color(0xFF2563EB) else Color.Gray
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun TagPill(text: String, containerColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .background(color = containerColor, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = text, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusPillDropdown(
    currentStatus: String,
    onStatusChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val statuses = listOf(
        "to_do" to "До виконання",
        "in_progress" to "В роботі",
        "review" to "На перевірці",
        "done" to "Готово"
    )
    val currentLabel = statuses.find { it.first == currentStatus }?.second ?: currentStatus

    val bgColor = if (currentStatus == "in_progress") Color(0xFFFEF3C7) else Color(0xFFF3F4F6)
    val textColor = if (currentStatus == "in_progress") Color(0xFFD97706) else Color(0xFF4B5563)

    Box {
        Row(
            modifier = Modifier
                .background(color = bgColor, shape = RoundedCornerShape(8.dp))
                .clickable { expanded = true }
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = currentLabel, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Змінити статус",
                tint = textColor,
                modifier = Modifier.size(18.dp).padding(start = 2.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            statuses.forEach { (backendValue, displayLabel) ->
                DropdownMenuItem(
                    text = { Text(displayLabel, color = Color.Black) },
                    onClick = {
                        onStatusChange(backendValue)
                        expanded = false
                    }
                )
            }
        }
    }
}