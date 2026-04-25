package com.example.bookapp.viewModel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookapp.models.Book
import com.example.bookapp.models.Review
import com.example.bookapp.repositories.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock.System
import java.util.UUID

class BookViewModel(private val repository: BookRepository) : ViewModel() {
    private val _book = mutableStateOf<Book?>(null)
    val book: State<Book?> = _book

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _similarBooks = MutableStateFlow<List<Book>>(emptyList())
    val similarBooks: StateFlow<List<Book>> = _similarBooks.asStateFlow()

    fun loadSimilarBooks(bookId: String) {
        viewModelScope.launch {
            val books = repository.getSimilarBooks(bookId)
            _similarBooks.value = books
        }
    }

    fun loadBook(bookId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _book.value = repository.getBookById(bookId)
            _isLoading.value = false
        }
    }


    fun saveReview(bookId: String, userId: String, rating: Int, text: String?) {
        viewModelScope.launch {
            try {
                val review = Review(
                    id = UUID.randomUUID().toString(),
                    book_id = bookId,
                    user_id = userId,
                    rating = rating,
                    text = text,
                    created_at = System.now().toString()
                )
                repository.saveReview(review)
                // Обновляем данные книги
                loadBook(bookId)
            } catch (e: Exception) {
                // Обработка ошибок
            }
        }
    }
}