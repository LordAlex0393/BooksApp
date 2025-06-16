
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bookapp.repositories.AuthRepository
import com.example.bookapp.viewModel.SignUpViewModel
import com.example.bookapp.viewModelFactory.SignUpViewModelFactory
import kotlinx.coroutines.delay

// ============= КОНСТАНТЫ И КОНФИГУРАЦИЯ =============
private const val SUCCESS_COLOR_HEX = 0xFF2E7D32
private const val BCRYPT_COST = 12
private const val BUTTON_MAX_WIDTH = 0.6f
private const val TRANSITION_DELAY = 1500L
private val BUTTON_MAX_HEIGHT = 48.dp
private val PADDING = 52.dp

// ================ ЭКРАН РЕГИСТРАЦИИ ================
@Composable
fun SignUpScreen(
    navController: NavController,
    viewModel: SignUpViewModel = viewModel(factory = SignUpViewModelFactory(AuthRepository()))
) {
    // Автоматический переход после регистрации
    if (viewModel.registrationSuccess) {
        LaunchedEffect(Unit) {
            delay(1500L)
            navController.navigate("login") {
                popUpTo("signup") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(52.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Регистрация", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(20.dp))

        // Поля формы
        OutlinedTextField(
            value = viewModel.username,
            onValueChange = { viewModel.username = it },
            label = { Text("Имя пользователя") },
            modifier = Modifier.fillMaxWidth()
        )


        Spacer(modifier = Modifier.height(8.dp))


        // Поле email
        OutlinedTextField(
            value = viewModel.email,
            onValueChange = { viewModel.email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )


        Spacer(modifier = Modifier.height(8.dp))


        // Поле пароля
        OutlinedTextField(
            value = viewModel.password,
            onValueChange = { viewModel.password = it },
            label = { Text("Пароль") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )


        Spacer(modifier = Modifier.height(8.dp))


        // Подтверждение пароля
        OutlinedTextField(
            value = viewModel.confirmPassword,
            onValueChange = { viewModel.confirmPassword = it },
            label = { Text("Повторите пароль") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Сообщения об ошибках
        viewModel.error?.let { error ->
            Text(error, color = MaterialTheme.colorScheme.error)
        }

        if (viewModel.registrationSuccess) {
            Text("Успешная регистрация!", color = Color(0xFF2E7D32))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.register() },
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(48.dp),
            enabled = !viewModel.isLoading
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Зарегистрироваться")
            }
        }

        TextButton(
            onClick = { navController.navigate("login") }
        ) {
            Text("Уже есть аккаунт? Войдите")
        }
    }
}