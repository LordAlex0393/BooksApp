
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

private const val BUTTON_MAX_WIDTH = 0.5f
private val BUTTON_MAX_HEIGHT = 48.dp
private val PADDING = 52.dp

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
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

        if (error.isNotEmpty()) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isEmpty() || password.isEmpty()) {
                    error = "Заполните все поля"
                    return@Button
                }

                scope.launch {
//                    try {
//                        Supabase.client.gotrue.loginWith(Email) {
//                            this.email = email
//                            this.password = password
//                        }
//                        navController.popBackStack()
//                        navController.navigate("home") // Переход на главный экран
//                    } catch (e: Exception) {
//                        error = "Ошибка входа: ${e.message}"
//                    }
                }
            },
            //modifier = Modifier.fillMaxWidth()
            modifier = Modifier
                .fillMaxWidth(BUTTON_MAX_WIDTH)
                .height(BUTTON_MAX_HEIGHT)
        ) {
            Text("Войти")
        }

        Spacer(modifier = Modifier.height(5.dp))

        TextButton(
            onClick = { navController.navigate("signup") },
        ) {
            Text("Нет аккаунта? Зарегистрируйтесь")
        }
    }
}