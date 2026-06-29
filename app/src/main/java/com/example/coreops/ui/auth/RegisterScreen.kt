package com.example.coreops.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coreops.R
import com.example.coreops.data.remote.models.RegisterRequest

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val registerState by viewModel.registerState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(registerState) {
        if (registerState is AuthState.Success) {
            Toast.makeText(context, "Акаунт успішно створено!", Toast.LENGTH_SHORT).show()
            viewModel.resetRegisterState()
            onRegisterSuccess()
        }
    }

    RegisterContent(
        registerState = registerState,
        onNavigateBack = onNavigateBack,
        onRegisterClick = { firstName, lastName, inviteToken, password ->
            val request = RegisterRequest(
                token = inviteToken,
                password = password,
                firstName = firstName,
                lastName = lastName
            )
            viewModel.register(request)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterContent(
    registerState: AuthState,
    onNavigateBack: () -> Unit,
    onRegisterClick: (firstName: String, lastName: String, inviteToken: String, password: String) -> Unit
) {
    var inviteToken by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var localError by remember { mutableStateOf<String?>(null) }

    // Головний контейнер на весь екран (Тепер він єдиний відповідає за скрол)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F6))
            .verticalScroll(rememberScrollState())
            .imePadding() // Адаптація під клавіатуру
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp)) // Верхній відступ

        // 1. Логотип
        Surface(
            modifier = Modifier
                .size(64.dp)
                .shadow(elevation = 10.dp, shape = RoundedCornerShape(20.dp), clip = false),
            color = Color.White,
            shape = RoundedCornerShape(20.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lucide_layers),
                    contentDescription = "Logo",
                    modifier = Modifier.size(32.dp),
                    tint = Color(0xFF2563EB)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Шапка екрану
        Text(
            text = "Реєстрація",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Створіть акаунт за допомогою коду запрошення",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 3. Картка форми
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Поле: Код запрошення
                Column {
                    Text("Код запрошення", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = inviteToken,
                        onValueChange = { inviteToken = it },
                        placeholder = { Text("Введіть токен", color = Color.LightGray) },
                        leadingIcon = { Icon(Icons.Outlined.Key, contentDescription = "Token") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Поле: Ім'я
                Column {
                    Text("Ім'я", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        placeholder = { Text("Ваше ім'я", color = Color.LightGray) },
                        leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = "First Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Поле: Прізвище
                Column {
                    Text("Прізвище", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        placeholder = { Text("Ваше прізвище", color = Color.LightGray) },
                        leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = "Last Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Поле: Пароль
                Column {
                    Text("Пароль", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Створіть пароль", color = Color.LightGray) },
                        leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = "Password") },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                    contentDescription = "Toggle Password Visibility"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Поле: Підтвердження пароля
                Column {
                    Text("Підтвердження пароля", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = { Text("Повторіть пароль", color = Color.LightGray) },
                        leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = "Confirm Password") },
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                    contentDescription = "Toggle Confirm Password Visibility"
                                )
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val errorMessage = localError ?: (registerState as? AuthState.Error)?.message
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                Button(
                    onClick = {
                        localError = null
                        if (password != confirmPassword) {
                            localError = "Паролі не збігаються"
                        } else if (inviteToken.isBlank() || firstName.isBlank() || lastName.isBlank() || password.isBlank()) {
                            localError = "Будь ласка, заповніть всі поля"
                        } else {
                            onRegisterClick(firstName.trim(), lastName.trim(), inviteToken.trim(), password)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = registerState !is AuthState.Loading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (registerState is AuthState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Створити акаунт", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- 4. Футтер навігації ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Вже маєте акаунт? ", color = Color.Gray, fontSize = 14.sp)
            Text(
                text = "Увійти",
                color = Color(0xFF2563EB),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onNavigateBack() }
            )
        }

        Spacer(modifier = Modifier.height(48.dp)) // Нижній відступ
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    RegisterContent(
        registerState = AuthState.Idle,
        onNavigateBack = {},
        onRegisterClick = { _, _, _, _ -> }
    )
}