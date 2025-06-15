package com.example.bookapp.viewModel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookapp.models.Book
import com.example.bookapp.repositories.BookRepository
import kotlinx.coroutines.launch

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
}