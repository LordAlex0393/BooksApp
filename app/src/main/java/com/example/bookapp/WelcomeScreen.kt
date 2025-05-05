import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

private const val BUTTON_MAX_WIDTH = 0.6f
private val BUTTON_MAX_HEIGHT = 44.dp
private val PADDING = 16.dp

@Composable
fun WelcomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(PADDING),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Добро пожаловать!",
            style = MaterialTheme.typography.headlineMedium
        )


        Spacer(modifier = Modifier.height(36.dp))


        Button(
            onClick = { navController.navigate("login") },
            modifier = Modifier
                .fillMaxWidth(BUTTON_MAX_WIDTH)
                .height(BUTTON_MAX_HEIGHT)
        ) {
            Text("Войти")
        }


        Spacer(modifier = Modifier.height(20.dp))


        Button(
            onClick = { navController.navigate("signup") },
            modifier = Modifier
                .fillMaxWidth(BUTTON_MAX_WIDTH)
                .height(BUTTON_MAX_HEIGHT)
        ) {
            Text("Создать аккаунт")
        }
    }
}