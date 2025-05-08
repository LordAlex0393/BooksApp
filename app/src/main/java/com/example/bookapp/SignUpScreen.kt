
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.bookapp.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.net.UnknownHostException

// ============= КОНСТАНТЫ И КОНФИГУРАЦИЯ =============
private const val SUCCESS_COLOR_HEX = 0xFF2E7D32
private const val BCRYPT_COST = 12
private const val BUTTON_MAX_WIDTH = 0.6f
private const val TRANSITION_DELAY = 1500L
private val BUTTON_MAX_HEIGHT = 48.dp
private val PADDING = 52.dp

// ================ ЭКРАН РЕГИСТРАЦИИ ================
@Composable
fun SignUpScreen(navController: NavController) {
    // --------- Состояния формы ---------
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var registrationSuccess by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // --------- UI Компоненты ---------
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(PADDING),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Заголовок
        Text("Регистрация", style = MaterialTheme.typography.headlineMedium)


        Spacer(modifier = Modifier.height(20.dp))


        // Поле имени пользователя
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Имя пользователя") },
            modifier = Modifier.fillMaxWidth()
        )


        Spacer(modifier = Modifier.height(8.dp))


        // Поле email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )


        Spacer(modifier = Modifier.height(8.dp))


        // Поле пароля
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )


        Spacer(modifier = Modifier.height(8.dp))


        // Подтверждение пароля
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Повторите пароль") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))
        // --------- Сообщения об ошибках/успехе ---------
        if (error.isNotEmpty()) {
            Text(error, color = MaterialTheme.colorScheme.error)
        }

        if (registrationSuccess) {
            Text("Вы успешно зарегистрировались", color = Color(SUCCESS_COLOR_HEX))
            LaunchedEffect(Unit) {
                delay(TRANSITION_DELAY) // Задержка
                navController.navigate("login") // Переход на главный экран
            }
        }

        Spacer(modifier = Modifier.height(16.dp)) //24


        // --------- Кнопка регистрации ---------
        Button(
            onClick = {
                scope.launch {
                    registerUser(
                        username = username,
                        email = email,
                        password = password,
                        confirmPassword = confirmPassword,
                        onError = { errorMessage -> error = errorMessage
                        },
                        onSuccess = {
                            registrationSuccess = true
                            error = ""
                            username = ""
                            email = ""
                            password = ""
                            confirmPassword = ""
                        }
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth(BUTTON_MAX_WIDTH)
                .height(BUTTON_MAX_HEIGHT)
        ) {
            Text("Зарегистрироваться")
        }


        Spacer(modifier = Modifier.height(5.dp))


        TextButton(
            onClick = { navController.navigate("login") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Уже есть аккаунт? Войдите")
        }
    }
}

// --------- Регистрация пользователя ---------
private suspend fun registerUser(
    username: String,
    email: String,
    password: String,
    confirmPassword: String,
    onError: (String) -> Unit,
    onSuccess: () -> Unit
) {
    try {
        withContext(Dispatchers.IO) {
            // Проверка интернет-соединения
            if (!checkInternetConnection()) {
                onError("Нет подключения к интернету")
                return@withContext
            }

            // Проверка совпадения паролей
            if (password != confirmPassword) {
                onError("Пароли не совпадают")
                return@withContext
            }

            // Хеширование пароля
            val hashedPassword = hashPassword(password)

            // Отправка данных в Supabase
            SupabaseClient.client.from("users").insert(mapOf(
                "username" to username,
                "email" to email,
                "password_hash" to hashedPassword
            )) {
                select()
            }

            // Успешная регистрация
            onSuccess()
        }
    } catch (e: Exception) {
        val errorMessage = when (e) {
            is UnknownHostException -> "Ошибка подключения. Проверьте интернет"
            else -> "Ошибка регистрации: ${e.localizedMessage}"
        }
        onError(errorMessage)
        Log.e("SignUp", "Registration error", e)
    }
}

// --------- Хеширование пароля ---------
fun hashPassword(password: String): String {
    return BCrypt.withDefaults().hashToString(BCRYPT_COST, password.toCharArray())
}

// ----- Проверка интернет-соединения -----
private fun checkInternetConnection(): Boolean {
    return try {
        URL("https://google.com").openConnection().apply {
            connectTimeout = 3000
            readTimeout = 3000
        }.connect()
        true
    } catch (e: Exception) {
        Log.w("Network", "No internet connection: ${e.localizedMessage}")
        false
    }
}