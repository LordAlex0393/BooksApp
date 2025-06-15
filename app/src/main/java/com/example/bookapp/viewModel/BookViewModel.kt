package com.example.bookapp.viewModel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookapp.models.Book
import com.example.bookapp.models.Review
import com.example.bookapp.repositories.BookRepository
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock.System
import java.util.UUID

class BookViewModel(private val repository: BookRepository) : ViewModel() {
    private val _book = mutableStateOf<Book?>(null)
    val book: State<Book?> = _book

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

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