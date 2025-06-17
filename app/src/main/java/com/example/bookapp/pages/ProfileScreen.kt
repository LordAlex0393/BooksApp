
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.bookapp.models.Book
import com.example.bookapp.models.BookList
import com.example.bookapp.models.UserSession
import com.example.bookapp.ui.theme.LoadingIndicator
import com.example.bookapp.viewModel.ProfileViewModel
import com.example.bookapp.viewModelFactory.ProfileViewModelFactory

private const val LIST_TITLE_MAX_WIDTH = 0.8f
private val PADDING = 24.dp

private val BOOKLIST_TITLE_PADDING = 8.dp
private val BOOKLIST_SPASE = 28.dp

private val BOOK_ITEM_HEIGHT = 190.dp
private val BOOK_ITEM_WIDTH = 120.dp
private val LIST_SPACING = 14.dp

private val CREATE_LIST_BUTTON_HEIGHT = 56.dp
private val CREATE_LIST_BUTTON_WIDTH = 0.8f
private val CREATE_LIST_BUTTON_PADDING_TOP = 10.dp

private val USERNAME_PADDING_START = 6.dp
private val USERNAME_PADDING_TOP = 16.dp


@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory())
) {
    val bookLists by viewModel.bookLists.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showCreateListDialog by remember { mutableStateOf(false) }
    var newListName by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    // Добавляем обработчик ошибок
    val error by viewModel.error.collectAsState()
    error?.let { err ->
        LaunchedEffect(err) {
            snackbarHostState.showSnackbar(err)
            viewModel.clearError()
        }
    }

    LaunchedEffect(Unit) {
        UserSession.currentUser.value?.id?.let { userId ->
            viewModel.loadUserBookLists(userId)
        }
    }

    if (isLoading) {
        LoadingIndicator()
        return
    }

    // Диалог создания нового списка
    if (showCreateListDialog) {
        AlertDialog(
            onDismissRequest = { showCreateListDialog = false },
            title = { Text("Создать новый список") },
            text = {
                Column {
                    Text("Введите название списка:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newListName,
                        onValueChange = { newListName = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Мой список книг") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newListName.isNotBlank()) {
                            UserSession.currentUser.value?.id?.let { userId ->
                                viewModel.createBookList(userId, newListName)
                            }
                            newListName = ""
                            showCreateListDialog = false
                        }
                    },
                    enabled = newListName.isNotBlank()
                ) {
                    Text("Создать")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        newListName = ""
                        showCreateListDialog = false
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(PADDING),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Заголовок "Профиль"
            item {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Профиль",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                UserSession.currentUser.value?.let { user ->
                    Text(
                        text = user.username,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = USERNAME_PADDING_TOP)
                            .fillMaxWidth()
                            .padding(start = USERNAME_PADDING_START, top = USERNAME_PADDING_TOP),
                        textAlign = TextAlign.Left
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Списки книг
            items(bookLists.size) { index ->
                BookListSection(bookLists[index], navController)
                Spacer(modifier = Modifier.height(BOOKLIST_SPASE))
            }

            // Кнопка "Новый список"
            item {
                OutlinedButton(
                    onClick = { showCreateListDialog = true },
                    modifier = Modifier
                        .height(CREATE_LIST_BUTTON_HEIGHT)
                        .fillMaxWidth(CREATE_LIST_BUTTON_WIDTH)
                        .padding(top = CREATE_LIST_BUTTON_PADDING_TOP)
                ) {
                    Text("Новый список")
                }
            }
        }
    }
}

// Исправленный BookListSection
@Composable
private fun BookListSection(bookList: BookList, navController: NavController) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Заголовок списка с кнопкой "Посмотреть все"
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = bookList.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth(LIST_TITLE_MAX_WIDTH)
                    .weight(0.7f)
                    .padding(BOOKLIST_TITLE_PADDING)
            )

            TextButton(
                onClick = { navController.navigate("list/${bookList.id}") },
                modifier = Modifier.weight(0.5f)
            ) {
                Text("Посмотреть все", style = MaterialTheme.typography.labelLarge)
            }
        }

        //Spacer(modifier = Modifier.height(1.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(LIST_SPACING),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(bookList.books.size) { index ->
                BookItem(bookList.books[index], navController)
            }
        }
    }
}

@Composable
private fun BookItem(book: Book, navController: NavController) {
    Column(
        modifier = Modifier
            .width(BOOK_ITEM_WIDTH)
            .height(BOOK_ITEM_HEIGHT),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Обложка книги
        val painter = rememberAsyncImagePainter(
            model = book.cover_url,
            placeholder = if (book.cover_url != null) {
                // Показываем плейсхолдер, пока загружается изображение
                // Можно использовать текущий цвет или другое изображение
                null
            } else {
                // Если coverUrl null, показываем плейсхолдер
                null
            }
        )

        Image(
            painter = painter,
            contentDescription = "Обложка книги: ${book.title}",
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.2f)
                .height(32.dp)
                .clickable {
                    navController.navigate("book/${book.id}")
                },
            contentScale = ContentScale.FillHeight
        )

        Spacer(modifier = Modifier.height(7.dp))

        // Название книги
        Text(
            text = book.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = 16.sp // Уменьшаем интерлиньяж (стандартно ~20.sp)
            ),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(5.dp))

        // Автор
        Text(
            text = book.author,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}