package com.example.bookapp.pages

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
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
import com.example.bookapp.ui.theme.SurfaceCard
import com.example.bookapp.ui.theme.TextPrimary
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
    val errorMessage: String? by viewModel.errorMessage
    var showReviewDialog by remember { mutableStateOf(false) }
    var rating by remember { mutableStateOf(0) }
    var reviewText by remember { mutableStateOf("") }
    var isSpoiler by remember { mutableStateOf(false) }
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

    if (errorMessage != null && book == null) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
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
        ) { innerPadding ->
            ErrorRetryView(
                message = errorMessage ?: "Ошибка загрузки книги",
                onRetry = {
                    viewModel.loadBook(bookId)
                    viewModel.loadSimilarBooks(bookId)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
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

                item {
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp, bottom = 24.dp)
                            .width(220.dp)
                            .height(320.dp)
                    ) {
                        BookCover(
                            currentBook,
                            modifier = Modifier.fillMaxSize()
                        )

                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .align(Alignment.TopStart),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "%.1f".format(currentBook.avg_rating ?: 0.0),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }

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
                    }
                }
                item {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentBook.genres.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .background(TextSecondary.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Книга",
                                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, fontWeight = FontWeight.SemiBold)
                                    )
                                }
                            }
                        } else {
                            items(currentBook.genres.size) { index ->
                                val genreName = currentBook.genres[index].name.trim()
                                if (genreName.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .background(PrimaryBook.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = genreName,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = PrimaryBook,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    Box(
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .background(TextSecondary.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = currentBook.year?.toString()?.plus(" г.") ?: "Год издания не указан",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
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
                item {
                    ReviewsSection(currentBook, navController)
                }
                item {
                    Button(
                        onClick = { showReviewDialog = true },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(top = 24.dp)
                            .scale(buttonScale)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBook)
                    ) {
                        Text("Оставить свой отзыв", style = MaterialTheme.typography.labelLarge)
                    }
                }
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
            if (showReviewDialog) {
                AlertDialog(
                    onDismissRequest = { showReviewDialog = false },
                    shape = RoundedCornerShape(24.dp),
                    containerColor = SurfaceCard,
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
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { isSpoiler = !isSpoiler }
                                    .padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = isSpoiler,
                                    onCheckedChange = { isSpoiler = it },
                                    colors = CheckboxDefaults.colors(checkedColor = PrimaryBook)
                                )
                                Text(
                                    text = "Отзыв содержит спойлеры",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                                )
                            }
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
                                        text = reviewText.takeIf { it.isNotBlank() },
                                        isSpoiler = isSpoiler
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
                            Text("Отмена", color = TextSecondary)
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
    Box(
        modifier = modifier
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
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
                modifier = Modifier.padding(horizontal = 22.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                reviews.forEach { review ->
                    ReviewCardContent(review = review)
                }
            }
        }
    }
}
@Composable
fun ReviewCardContent(review: Review, modifier: Modifier = Modifier) {
    var spoilerRevealed by remember(review.id) { mutableStateOf(false) }
    var isExpanded by remember(review.id) { mutableStateOf(false) }
    var isTextOverflowing by remember(review.id) { mutableStateOf(false) }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .background(SurfaceCard, RoundedCornerShape(16.dp))
            .animateContentSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = review.username?.username ?: "Пользователь",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(10.dp))
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
        }
        if (!review.text.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            if (review.is_spoiler && !spoilerRevealed) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(PrimaryBook.copy(alpha = 0.08f))
                        .clickable { spoilerRevealed = true }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = PrimaryBook,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Осторожно, спойлер! Нажмите, чтобы показать",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = PrimaryBook,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            } else {
                Text(
                    text = review.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextPrimary.copy(alpha = 0.9f),
                        lineHeight = 20.sp
                    ),
                    maxLines = if (isExpanded) Int.MAX_VALUE else 4,
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = { result ->
                        if (!isExpanded) {
                            isTextOverflowing = result.hasVisualOverflow
                        }
                    }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isTextOverflowing || isExpanded) {
                        Text(
                            text = if (isExpanded) "Свернуть" else "Читать полностью",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = PrimaryBook,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .clickable { isExpanded = !isExpanded }
                        )
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                    if (review.is_spoiler && spoilerRevealed) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .clickable { spoilerRevealed = false }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Скрыть спойлер",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                            )
                        }
                    }
                }
            }
        }
        review.created_at?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it.formatDate(),
                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary.copy(alpha = 0.7f)),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
fun String.formatDate(): String {
    return if (this.length >= 10) this.substring(0, 10).replace("-", ".") else this
}