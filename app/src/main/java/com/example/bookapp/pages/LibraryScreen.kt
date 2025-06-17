package com.example.bookapp.pages

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.bookapp.models.Book
import com.example.bookapp.models.UserSession
import com.example.bookapp.repositories.BookRepository
import com.example.bookapp.viewModel.LibraryViewModel
import com.example.bookapp.viewModelFactory.LibraryViewModelFactory

@Composable
fun LibraryScreen(
    navController: NavController,
    viewModel: LibraryViewModel = viewModel(factory = LibraryViewModelFactory(BookRepository()))
) {
    val books by viewModel.books.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var visibleBooksCount by remember { mutableStateOf(8) }

    LaunchedEffect(Unit) {
        viewModel.loadBooks()
        // Log the number of books loaded
        Log.d("LibraryScreen", "Books loaded: ${viewModel.books.value.size}")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Шапка с названием и кнопкой профиля
        LibraryHeader(navController)

        // Сетка книг
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(books.take(visibleBooksCount)) { book ->
                BookGridItem(book, navController)
            }
        }

        // Inside Column, after LazyVerticalGrid
        if (visibleBooksCount < books.size) {
            Log.d(
                "LibraryScreen",
                "Show 'Load More' button. Visible: $visibleBooksCount, Total: ${books.size}"
            )
            Button(
                onClick = {
                    visibleBooksCount += 8
                    Log.d(
                        "LibraryScreen",
                        "Loading more books. New visible count: $visibleBooksCount"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Загрузить ещё")
            }
        } else {
            Log.d(
                "LibraryScreen",
                "Do NOT show 'Load More' button. Visible: $visibleBooksCount, Total: ${books.size}"
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class) // Add this annotation to LibraryScreen or the whole file
@Composable
private fun LibraryHeader(navController: NavController) {
    CenterAlignedTopAppBar(
        title = { Text("Библиотека", style = MaterialTheme.typography.headlineSmall) },
        actions = {
            UserSession.currentUser.value?.let { user ->
                IconButton(onClick = { navController.navigate("profile") }) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Профиль",
                        modifier = Modifier.size(36.dp)
                            .padding(horizontal = 10.dp), // Slightly larger icon for aesthetics
                        tint = MaterialTheme.colorScheme.primary // Give it a primary color tint
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface, // Adjust background if needed
            titleContentColor = MaterialTheme.colorScheme.onSurface // Adjust title color
        )
    )
}

@Composable
private fun BookGridItem(book: Book, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp) // Фиксированная высота для всех карточек
            .clickable { navController.navigate("book/${book.id}") },
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Обложка книги (фиксированный размер)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(220.dp)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                val painter = rememberAsyncImagePainter(model = book.cover_url)
                Image(
                    painter = painter,
                    contentDescription = "Обложка книги",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Название книги (фиксированное количество строк)
            // Use a Box with a predefined height to ensure two lines
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((MaterialTheme.typography.bodyMedium.lineHeight.value * 1).dp) // Calculate height for two lines
            ) {
                Text(
                    text = book.title ?: "Без названия",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Автор (1 строка)
            Text(
                text = book.author ?: "Автор неизвестен",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(1.dp))

            // Жанр (1 строка)
            Text(
                text = book.genre ?: "Жанр не указан",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Рейтинг
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Рейтинг",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "%.1f".format(book.avg_rating ?: 0.0),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}