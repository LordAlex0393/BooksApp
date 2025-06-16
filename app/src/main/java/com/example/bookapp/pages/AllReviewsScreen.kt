package com.example.bookapp.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bookapp.models.Book
import com.example.bookapp.repositories.BookRepository
import com.example.bookapp.viewModel.BookViewModel
import com.example.bookapp.viewModelFactory.BookViewModelFactory

@Composable
fun AllReviewsScreen(
    navController: NavController,
    bookId: String,
    viewModel: BookViewModel = viewModel(factory = BookViewModelFactory(BookRepository()))
) {
    val book: Book? by viewModel.book

    LaunchedEffect(bookId) {
        viewModel.loadBook(bookId)
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Все отзывы",
                style = MaterialTheme.typography.headlineSmall)
        }

        LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
            items(book?.reviews?.size ?: 0) { index ->
                book?.reviews?.get(index)?.let { review ->
                    ReviewItem(review)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}