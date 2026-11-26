package com.example.bookapp.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bookapp.models.Book
import com.example.bookapp.repositories.BookRepository
import com.example.bookapp.ui.theme.PrimaryBook
import com.example.bookapp.ui.theme.SurfaceCard
import com.example.bookapp.ui.theme.TextSecondary
import com.example.bookapp.viewModel.BookViewModel
import com.example.bookapp.viewModelFactory.BookViewModelFactory

private enum class ReviewSortOption(val label: String) {
    NEWEST("Сначала новые"),
    OLDEST("Сначала старые"),
    POSITIVE("Сначала положительные"),
    NEGATIVE("Сначала отрицательные")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllReviewsScreen(
    navController: NavController,
    bookId: String,
    viewModel: BookViewModel = viewModel(factory = BookViewModelFactory(BookRepository()))
) {
    val book: Book? by viewModel.book
    val isLoading: Boolean by viewModel.isLoading
    val errorMessage: String? by viewModel.errorMessage
    var selectedSort by remember { mutableStateOf(ReviewSortOption.NEWEST) }

    LaunchedEffect(bookId) {
        viewModel.loadBook(bookId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Все отзывы",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        val rawReviews = book?.reviews ?: emptyList()

        val sortedReviews = remember(rawReviews, selectedSort) {
            when (selectedSort) {
                ReviewSortOption.NEWEST -> rawReviews.sortedByDescending { it.created_at ?: "" }
                ReviewSortOption.OLDEST -> rawReviews.sortedBy { it.created_at ?: "" }
                ReviewSortOption.POSITIVE -> rawReviews.sortedByDescending { it.rating }
                ReviewSortOption.NEGATIVE -> rawReviews.sortedBy { it.rating }
            }
        }

        when {
            isLoading && rawReviews.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryBook)
                }
            }
            errorMessage != null && rawReviews.isEmpty() -> {
                ErrorRetryView(
                    message = errorMessage ?: "Ошибка загрузки отзывов",
                    onRetry = { viewModel.loadBook(bookId) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
            rawReviews.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Отзывов пока нет",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(ReviewSortOption.entries) { option ->
                            val isSelected = option == selectedSort
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) PrimaryBook else SurfaceCard
                                    )
                                    .clickable { selectedSort = option }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = option.label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = if (isSelected) Color.White else TextSecondary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                )
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = sortedReviews,
                            key = { it.id ?: it.hashCode() }
                        ) { review ->
                            ReviewCardContent(review = review)
                        }
                    }
                }
            }
        }
    }
}