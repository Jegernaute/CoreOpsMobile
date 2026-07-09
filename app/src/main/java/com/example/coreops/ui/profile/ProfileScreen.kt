package com.example.coreops.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coreops.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onLogout: () -> Unit
) {
    val state by profileViewModel.state.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Профіль", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { /* TODO: Редагування профілю */ }) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Редагувати", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFF3F4F6))
            )
        },
        containerColor = Color(0xFFF3F4F6)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                is ProfileState.Loading -> {
                    CircularProgressIndicator(color = Color(0xFF2563EB))
                }
                is ProfileState.Error -> {
                    val errorMessage = (state as ProfileState.Error).message
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            modifier = Modifier.padding(16.dp)
                        )
                        Button(
                            onClick = { profileViewModel.fetchProfile() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                        ) {
                            Text("Спробувати ще раз")
                        }
                    }
                }
                is ProfileState.Success -> {
                    val user = (state as ProfileState.Success).profile

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))

                        // --- 1. Аватар та Основна інформація ---
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(Color.LightGray, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            // TODO: Замінити на AsyncImage при підключенні Coil/API
                            Icon(Icons.Outlined.Person, contentDescription = "Аватар", tint = Color.Gray, modifier = Modifier.size(40.dp))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = user.fullName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = user.jobTitle ?: "Посада не вказана",
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .background(Color(0xFFEFF6FF), RoundedCornerShape(50))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = user.globalRole.replaceFirstChar { it.uppercase() },
                                color = Color(0xFF2563EB),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // --- 2. Контактні дані ---
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column {
                                ProfileInfoRow(icon = Icons.Outlined.Email, label = "Email", value = user.email)
                                HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                                ProfileInfoRow(icon = Icons.Outlined.Phone, label = "Телефон", value = user.phone ?: "Не вказано")
                                HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                                ProfileInfoRow(icon = Icons.Outlined.Send, label = "Telegram", value = user.telegram ?: "Не вказано")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // --- 3. Налаштування ---
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Outlined.Notifications, contentDescription = null, tint = Color(0xFF6B7280))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Налаштування сповіщень", fontSize = 15.sp, color = Color.Black, fontWeight = FontWeight.Medium)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Color(0xFF9CA3AF))
                                }
                                HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Outlined.DarkMode, contentDescription = null, tint = Color(0xFF6B7280))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Темна тема", fontSize = 15.sp, color = Color.Black, fontWeight = FontWeight.Medium)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Switch(
                                        checked = false,
                                        onCheckedChange = { /* TODO: Логіка зміни теми */ }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // --- 4. Кнопка виходу ---
                        TextButton(
                            onClick = {
                                authViewModel.logout()
                                onLogout()
                            },
                            modifier = Modifier.padding(bottom = 24.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = "Вихід", tint = Color(0xFFDC2626))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Вийти з акаунту", color = Color(0xFFDC2626), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, fontSize = 15.sp, color = Color(0xFF6B7280))
        Spacer(modifier = Modifier.weight(1f))
        Text(value, fontSize = 15.sp, color = Color.Black, fontWeight = FontWeight.Medium)
    }
}