package com.example.bookapp.pages

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.bookapp.models.Book
import com.example.bookapp.models.Review
import com.example.bookapp.models.UserSession
import com.example.bookapp.repositories.BookRepository
import com.example.bookapp.ui.theme.PrimaryBook
import com.example.bookapp.ui.theme.TextSecondary
import com.example.bookapp.viewModel.BookViewModel
import com.example.bookapp.viewModelFactory.BookViewModelFactory

@Composable
fun BookScreen(
    navController: NavController,
    bookId: String,
    viewModel: BookViewModel = viewModel(factory = BookViewModelFactory(BookRepository()))
) {
    val book: Book? by viewModel.book
    val isLoading: Boolean by viewModel.isLoading
    var showReviewDialog by remember { mutableStateOf(false) }
    var rating by remember { mutableStateOf(0) }
    var reviewText by remember { mutableStateOf("") }
    val similarBooks by viewModel.similarBooks.collectAsState()

    val starColors = remember(rating) {
        List(5) { index -> if (index < rating) Color(0xFFFFD700) else Color(0xFFE0E0E0) }
    }

    LaunchedEffect(bookId) {
        viewModel.loadBook(bookId)
        viewModel.loadSimilarBooks(bookId)
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryBook)
        }
        return
    }

    book?.let { currentBook ->
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val buttonScale by animateFloatAsState(
            targetValue = if (isPressed) 0.96f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "review_btn_scale"
        )

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // Навигационная панель сверху
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                        }
                    }
                }

                // Премиальная обложка книги
                item {
                    BookCover(
                        currentBook,
                        modifier = Modifier
                            .padding(top = 8.dp, bottom = 24.dp)
                            .width(220.dp)
                            .height(320.dp)
                    )
                }

                // Информационный блок: Название, Автор, Рейтинг
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentBook.title,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = currentBook.author,
                            style = MaterialTheme.typography.titleMedium.copy(color = TextSecondary, fontWeight = FontWeight.Medium),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Красивая строка рейтинга
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color(0xFFFFD700).copy(alpha = 0.12f), RoundedCornerShape(24.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Рейтинг",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "%.1f".format(currentBook.avg_rating ?: 0.0),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD4AF37),
                                    fontSize = 16.sp
                                )
                            )
                        }
                    }
                }
                // Блок с жанрами и годом выпуска (Чипсы / Плашки)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val genreText = currentBook.genres.take(3).joinToString { it.name }
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = if (genreText.isNotBlank()) genreText else "Жанр не указан",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = currentBook.year?.toString()?.plus(" г.") ?: "Год не указан",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextSecondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }

                // Информационный блок "О книге"
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "О книге",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = currentBook.description ?: "Описание отсутствует",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary,
                                lineHeight = 22.sp
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Секция отзывов читателей
                item {
                    ReviewsSection(currentBook, navController)
                }

                // Кнопка написания нового отзыва (с мягкой пружинной анимацией нажатия)
                item {
                    Button(
                        onClick = { showReviewDialog = true },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(top = 24.dp)
                            .scale(buttonScale)
                            .height(50.dp)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) { showReviewDialog = true },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBook)
                    ) {
                        Text("Оставить свой отзыв", style = MaterialTheme.typography.labelLarge)
                    }
                }
                // Блок «Похожие книги»
                if (similarBooks.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            text = "Похожие книги",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        SimilarBooksGrid(similarBooks = similarBooks, navController = navController)
                    }
                }
            }

            // Аккуратное диалоговое окно создания отзыва
            if (showReviewDialog) {
                AlertDialog(
                    onDismissRequest = { showReviewDialog = false },
                    shape = RoundedCornerShape(24.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    title = {
                        Text(
                            "Оцените издание",
                            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 20.sp),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                (0..4).forEach { index ->
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Оценка ${index + 1}",
                                        modifier = Modifier
                                            .clickable { rating = index + 1 }
                                            .padding(6.dp)
                                            .size(32.dp),
                                        tint = starColors[index]
                                    )
                                }
                            }
                            OutlinedTextField(
                                value = reviewText,
                                onValueChange = { reviewText = it },
                                label = { Text("Ваш отзыв (необязательно)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                maxLines = 4,
                                shape = RoundedCornerShape(14.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                UserSession.currentUser.value?.let { user ->
                                    viewModel.saveReview(
                                        bookId = currentBook.id,
                                        userId = user.id,
                                        rating = rating,
                                        text = reviewText.takeIf { it.isNotBlank() }
                                    )
                                }
                                showReviewDialog = false
                            },
                            enabled = rating > 0,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBook)
                        ) {
                            Text("Отправить")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showReviewDialog = false }) {
                            Text("Отмена", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SimilarBooksGrid(similarBooks: List<Book>, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        val rows = similarBooks.chunked(2)
        rows.forEach { rowBooks ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowBooks.forEach { book ->
                    Box(modifier = Modifier.weight(1f)) {
                        BookGridItem(book = book, navController = navController)
                    }
                }
                if (rowBooks.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun BookCover(book: Book, modifier: Modifier = Modifier) {
    val painter = rememberAsyncImagePainter(model = book.cover_url)
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
    ) {
        Image(
            painter = painter,
            contentDescription = "Обложка: ${book.title}",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun ReviewsSection(book: Book, navController: NavController) {
    val visibleReviews = 3
    val reviews = book.reviews?.take(visibleReviews) ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 28.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Отзывы",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                modifier = Modifier.weight(1f)
            )

            if (reviews.isNotEmpty()) {
                TextButton(onClick = { navController.navigate("book/${book.id}/reviews") }) {
                    Text("Все отзывы", style = MaterialTheme.typography.labelLarge.copy(color = PrimaryBook))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (reviews.isEmpty()) {
            Text(
                text = "Пока никто не оставил отзыв. Будьте первым!",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
        } else {
            Column(
                modifier = Modifier.padding(horizontal = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                reviews.forEach { review ->
                    ReviewItem(review)
                }
            }
        }
    }
}

@Composable
fun ReviewItem(review: Review) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    (1..5).forEach { star ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (star <= review.rating) Color(0xFFFFD700) else Color(0xFFE0E0E0),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = review.username?.username ?: "Пользователь",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            if (!review.text.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = review.text,
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f))
                )
            }

            review.created_at?.let {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = it.formatDate(),
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary.copy(alpha = 0.7f)),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

fun String.formatDate(): String {
    return if (this.length >= 10) this.substring(0, 10).replace("-", ".") else this
}