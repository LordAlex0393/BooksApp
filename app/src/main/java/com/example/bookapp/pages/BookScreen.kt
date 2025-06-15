package com.example.bookapp.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    var showReviewDialog by remember { mutableStateOf(false) } // Перенесено внутрь функции
    var rating by remember { mutableStateOf(0) } // Для рейтинга
    var reviewText by remember { mutableStateOf("") } // Для текста отзыва

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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Обложка книги
            BookCover(
                book,
                modifier = Modifier
                    .size(350.dp)
                    .padding(top = 24.dp)
            )

            // Название и автор
            Text(
                text = book.title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 26.dp)
            )

            // Автор
            Text(
                text = book.author,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp)
            ) // Добавим небольшой отступ

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
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // Жанр и год
            Row(
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = book.genre ?: "Жанр не указан",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = book.year?.toString() + " г." ?: "Год не указан",
                    style = MaterialTheme.typography.bodyMedium
                )
            }


            // Описание
            Text(
                text = "О книге",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(top = 14.dp)
                    .align(Alignment.Start)
            )

            Text(
                text = book.description ?: "Описание отсутствует",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 20.dp)
                    .padding(top = 12.dp)
            )

            // Кнопка для открытия диалога
            Button(
                onClick = { showReviewDialog = true },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .padding(top = 24.dp)
            ) {
                Text("Оставить отзыв")
            }

            if (showReviewDialog) {
                AlertDialog(
                    onDismissRequest = { showReviewDialog = false },
                    title = { Text("Оцените книгу") },
                    text = {
                        Column {
                            // Рейтинг звездами
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Рейтинг:", modifier = Modifier.padding(end = 8.dp))
                                (1..5).forEach { star ->
                                    Icon(
                                        imageVector = if (star <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                                        contentDescription = "Оценка $star",
                                        modifier = Modifier
                                            .clickable { rating = star }
                                            .padding(4.dp),
                                        tint = Color(0xFFFFD700))
                                }
                            }

                            // Поле для отзыва
                            OutlinedTextField(
                                value = reviewText,
                                onValueChange = { reviewText = it },
                                label = { Text("Ваш отзыв") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                maxLines = 5
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                // TODO: Сохранить отзыв и рейтинг
                                showReviewDialog = false
                            },
                            enabled = rating > 0 // Требуем хотя бы одну звезду
                        ) {
                            Text("Отправить")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showReviewDialog = false }) {
                            Text("Отмена")
                        }
                    }
                )
            }
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