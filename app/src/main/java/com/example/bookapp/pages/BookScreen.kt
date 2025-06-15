package com.example.bookapp.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.bookapp.models.Book
import com.example.bookapp.repositories.BookRepository
import com.example.bookapp.viewModel.BookViewModel
import com.example.bookapp.viewModel.BookViewModelFactory


@Composable
fun BookScreen(
    navController: NavController,
    bookId: String,
    viewModel: BookViewModel = viewModel(factory = BookViewModelFactory(BookRepository()))
) {
    val book: Book? by viewModel.book
    val isLoading: Boolean by viewModel.isLoading

    LaunchedEffect(bookId) {
        viewModel.loadBook(bookId)
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    book?.let { book ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Обложка книги
            BookCover(book, modifier = Modifier.size(200.dp))

            // Название и автор
            Text(
                text = book.title,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 16.dp))

                        // Автор
            Text(
            text = book.author,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)) // Добавим небольшой отступ

            // Рейтинг
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Рейтинг",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = "%.1f".format(book.avg_rating ?: 0.0),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 4.dp))
            }

            // Жанр и год
            Row(
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = book.genre ?: "Жанр не указан",
                    style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = book.year?.toString() ?: "Год не указан",
                    style = MaterialTheme.typography.bodyMedium)
            }

            // Описание
            Text(
                text = "О книге",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .align(Alignment.Start))

            Text(
                text = book.description ?: "Описание отсутствует",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
fun BookCover(book: Book, modifier: Modifier = Modifier) {
    val painter = rememberAsyncImagePainter(
        model = book.cover_url,
        //error = painterResource(R.drawable.placeholder_book) // Заглушка
    )

    Image(
        painter = painter,
        contentDescription = "Обложка: ${book.title}",
        modifier = modifier,
        contentScale = ContentScale.Fit)
}