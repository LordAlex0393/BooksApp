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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.bookapp.viewModel.ProfileViewModelFactory

@Composable
fun BookListScreen(
    navController: NavController,
    listId: String,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory())
) {
    val bookLists by viewModel.bookLists.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(listId) {
        UserSession.currentUser.value?.id?.let { userId ->
            viewModel.loadUserBookLists(userId)
        }
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
                Text(
                    text = "Этот список пуст",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(currentList.books) { book ->
                        BookListItem(book, navController)
                    }
                }
            }
        }
    }
}

@Composable
fun BookListItem(book: Book, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("book/${book.id}") }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Обложка книги с фиксированной шириной
            BookCoverImage(
                coverUrl = book.cover_url,
                modifier = Modifier
                    .width(70.dp)
                    .height(110.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Информация о книге
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = book.title ?: "Без названия",
                    style = MaterialTheme.typography.titleLarge,
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
                    text = (book.genre.toString()) ?: "Без жанра",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = (book.year.toString() + " г.") ?: "Дата неизвестна",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun BookCoverImage(coverUrl: String?, modifier: Modifier = Modifier) {
    val painter = rememberAsyncImagePainter(
        model = coverUrl,
        error = null // Плейсхолдер при ошибке загрузки
    )

    Card(
        modifier = modifier,
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