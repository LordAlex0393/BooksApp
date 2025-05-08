
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
import kotlinx.serialization.Serializable
import java.net.UnknownHostException

private const val SUCCESS_COLOR_HEX = 0xFF2E7D32
private const val BUTTON_MAX_WIDTH = 0.5f
private val BUTTON_MAX_HEIGHT = 48.dp
private val PADDING = 52.dp

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var loginSuccess by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(PADDING),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Вход",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (error.isNotEmpty()) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (loginSuccess) {
            Text(
                text = "Вы успешно вошли",
                color = Color(SUCCESS_COLOR_HEX),
                modifier = Modifier.padding(top = 8.dp)
            )
            LaunchedEffect(Unit) {
                delay(1500) // Задержка для отображения сообщения
                navController.navigate("welcome") // Переход на главный экран
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.isEmpty() || password.isEmpty()) {
                    error = "Заполните все поля"
                    return@Button
                }

                scope.launch {
                    try {
                        // Получаем пользователя из Supabase
                        val user = SupabaseClient.client
                            .from("users")
                            .select {
                                filter{
                                    eq("email", email)
                                }
                            }
                            .decodeSingleOrNull<User>()

                        if (user == null) {
                            withContext(Dispatchers.Main) {
                                error = "Пользователь не найден"
                            }
                        }

                        // Проверяем пароль
                        val isPasswordValid = BCrypt.verifyer()
                            .verify(password.toCharArray(), user?.password_hash)
                            .verified

                        if (!isPasswordValid) {
                            withContext(Dispatchers.Main) {
                                error = "Неверный пароль"
                            }
                        }

                        // Успешный вход
                        withContext(Dispatchers.Main) {
                            loginSuccess = true
                            error = ""
                            email = ""
                            password = ""
                        }

                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            error = when {
                                e is UnknownHostException -> "Ошибка подключения"
                                e.message?.contains("404") == true -> "Пользователь не найден"
                                else -> "Ошибка входа: ${e.localizedMessage}"
                            }
                        }
                        Log.e("Login", "Login error", e)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth(BUTTON_MAX_WIDTH)
                .height(BUTTON_MAX_HEIGHT)
        ) {
            Text("Войти")
        }

        Spacer(modifier = Modifier.height(3.dp))

        TextButton(
            onClick = { navController.navigate("signup") },
        ) {
            Text("Нет аккаунта? Зарегистрируйтесь")
        }
    }
}

// Модель пользователя (добавьте в общие классы)
@Serializable
data class User(
    val email: String,
    val password_hash: String
)