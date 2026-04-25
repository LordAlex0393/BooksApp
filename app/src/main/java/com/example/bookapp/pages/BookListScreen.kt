package com.example.bookapp.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.bookapp.models.Book
import com.example.bookapp.models.UserSession
import com.example.bookapp.ui.theme.LoadingIndicator
import com.example.bookapp.viewModel.ProfileViewModel
import com.example.bookapp.viewModelFactory.ProfileViewModelFactory


@Composable
fun BookListScreen(
    navController: NavController,
    listId: String,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory())
) {
    val bookLists by viewModel.bookLists.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showDeleteListDialog by remember { mutableStateOf(false) }

    LaunchedEffect(listId) {
        UserSession.currentUser.value?.id?.let { userId ->
            viewModel.loadUserBookLists(userId)
        }
    }

    // Диалог подтверждения удаления списка
    if (showDeleteListDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteListDialog = false },
            title = { Text("Удаление списка") },
            text = { Text("Вы точно хотите удалить этот список? Все книги в нём будут удалены.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteBookList(listId) {
                            navController.navigate("profile") // Возвращаемся назад после удаления
                        }
                        showDeleteListDialog = false
                    }
                ) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteListDialog = false }
                ) {
                    Text("Отмена")
                }
            }
        )
    }

    if (isLoading) {
        LoadingIndicator()
        return
    }

    val currentList = bookLists.find { it.id.toString() == listId }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
            }
            Text(
                text = currentList?.name ?: "Список не найден",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        when {
            currentList == null -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Не удалось найти список с ID: $listId")
                    Text("Доступные списки:")
                    bookLists.forEach { list ->
                        Text("- ${list.name} (ID: ${list.id})")
                    }
                }
            }
            currentList.books.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Этот список пуст",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                    // Кнопка удаления списка
                    Button(
                        onClick = { showDeleteListDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(top = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Удалить список",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Удалить список")
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(currentList.books) { book ->
                        BookListItem(
                            book = book,
                            listId = listId,
                            navController = navController,
                            onDeleteClick = { bookId ->
                                viewModel.removeBookFromList(listId, bookId)
                            }
                        )
                    }

                    // Кнопка удаления списка в конце
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = { showDeleteListDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                ),
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Удалить список",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Удалить список")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookListItem(
    book: Book,
    listId: String,
    navController: NavController,
    onDeleteClick: (String) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Диалог подтверждения удаления
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удаление книги") },
            text = { Text("Вы точно хотите удалить книгу из списка?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick(book.id)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Отмена")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Обложка книги
            Box(
                modifier = Modifier
                    .width(70.dp)
                    .height(110.dp)
                    .clickable { navController.navigate("book/${book.id}") }
            ) {
                BookCoverImage(coverUrl = book.cover_url)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Информация о книге
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { navController.navigate("book/${book.id}") }
            ) {
                Text(
                    text = book.title ?: "Без названия",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = book.author ?: "Автор неизвестен",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = book.genres.take(2).joinToString { it.name } ?: "Без жанра",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = book.year?.toString()?.plus(" г.") ?: "Дата неизвестна",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Кнопка удаления
            IconButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Удалить книгу",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun BookCoverImage(coverUrl: String?) {
    val painter = rememberAsyncImagePainter(
        model = coverUrl,
        error = null
    )

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = MaterialTheme.shapes.medium
    ) {
        if (coverUrl != null) {
            Image(
                painter = painter,
                contentDescription = "Обложка книги",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📖",
                    style = MaterialTheme.typography.displayMedium
                )
            }
        }
    }
}