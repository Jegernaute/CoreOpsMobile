package com.example.coreops.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import com.example.coreops.R

// --- Кольорові палітри ---
val CoreOpsBg = Color(0xFFF3F4F6)
val CoreOpsSurface = Color(0xFFFFFFFF)
val CoreOpsPrimary = Color(0xFF2563EB)
val CoreOpsTextPrimary = Color(0xFF111827)
val CoreOpsTextSecondary = Color(0xFF6B7280)
val CoreOpsBorder = Color(0xFFE5E7EB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()

    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    val rememberMe by viewModel.rememberMe.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CoreOpsBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- 1. Логотип ---
        Surface(
            modifier = Modifier
                .size(64.dp)
                .shadow(elevation = 10.dp, shape = RoundedCornerShape(20.dp), clip = false),
            color = CoreOpsSurface,
            shape = RoundedCornerShape(20.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lucide_layers),
                    contentDescription = "Logo",
                    modifier = Modifier.size(32.dp),
                    tint = CoreOpsPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 2. Блок привітання ---
        Text(
            text = "CoreOps",
            fontSize = 32.sp, // 32px
            fontWeight = FontWeight.Bold,
            color = CoreOpsTextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Увійдіть до робочого простору",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = CoreOpsTextSecondary
        )

        Spacer(modifier = Modifier.height(40.dp))

        // --- 3. Головна картка форми ---

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 15.dp, shape = RoundedCornerShape(24.dp), clip = false),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CoreOpsSurface),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {

                // --- Поле Email ---
                Text(
                    text = "Email",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = CoreOpsTextPrimary,
                    modifier = Modifier.padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { viewModel.onEmailChange(it) },
                    placeholder = { Text("vash@email.com", color = CoreOpsTextSecondary, fontSize = 16.sp) },
                    leadingIcon = {
                        Icon(Icons.Outlined.Email, contentDescription = "Email Icon", tint = CoreOpsTextSecondary, modifier = Modifier.size(20.dp))
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CoreOpsPrimary,
                        unfocusedBorderColor = CoreOpsBorder,
                        focusedTextColor = CoreOpsTextPrimary,
                        unfocusedTextColor = CoreOpsTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                // --- Поле Пароль ---
                Text(
                    text = "Пароль",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = CoreOpsTextPrimary,
                    modifier = Modifier.padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { viewModel.onPasswordChange(it) },
                    placeholder = { Text("Введіть пароль", color = CoreOpsTextSecondary, fontSize = 16.sp) },
                    leadingIcon = {
                        Icon(Icons.Outlined.Lock, contentDescription = "Lock Icon", tint = CoreOpsTextSecondary, modifier = Modifier.size(20.dp))
                    },
                    trailingIcon = {

                        val iconId = if (passwordVisible)
                            android.R.drawable.ic_menu_view
                        else
                            android.R.drawable.ic_secure

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = if (passwordVisible) "Сховати пароль" else "Показати пароль",
                                tint = CoreOpsTextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CoreOpsPrimary,
                        unfocusedBorderColor = CoreOpsBorder,
                        focusedTextColor = CoreOpsTextPrimary,
                        unfocusedTextColor = CoreOpsTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                // --- Рядок: "Запам'ятати мене" та "Забули пароль?" ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Кастомний чекбокс
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { viewModel.onRememberMeChange(it) },
                            modifier = Modifier.size(20.dp),
                            colors = CheckboxDefaults.colors(
                                checkedColor = CoreOpsPrimary,
                                uncheckedColor = CoreOpsBorder,
                                checkmarkColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "Запам'ятати мене", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = CoreOpsTextSecondary)
                    }

                    Text(
                        text = "Забули пароль?",
                        fontSize = 13.sp,
                        color = CoreOpsPrimary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable {}
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Помилка
                if (authState is AuthState.Error) {
                    Text(
                        text = (authState as AuthState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                    )
                }

                // --- 4. Головна кнопка ---
                Button(
                    onClick = { viewModel.login() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = authState !is AuthState.Loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CoreOpsPrimary,
                        contentColor = Color.White
                    )
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text(text = "Увійти", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // --- 5. Футтер ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Немає акаунту?", fontSize = 14.sp, color = CoreOpsTextSecondary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Зверніться до адміністратора",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = CoreOpsPrimary,
                modifier = Modifier.clickable {}
            )
        }
    }
}