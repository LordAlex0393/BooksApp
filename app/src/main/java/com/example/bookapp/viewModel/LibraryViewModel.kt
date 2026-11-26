package com.example.bookapp.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookapp.models.Book
import com.example.bookapp.models.BookList
import com.example.bookapp.repositories.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LibraryViewModel(private val repository: BookRepository) : ViewModel() {
    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _userLists = MutableStateFlow<List<BookList>>(emptyList())
    val userLists: StateFlow<List<BookList>> = _userLists.asStateFlow()

    fun loadBooks() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _books.value = repository.getAllBooks()
            } catch (e: Exception) {
                _errorMessage.value = "Не удалось загрузить каталог книг. Проверьте подключение к интернету."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addBookToList(listId: String, bookId: String) {
        viewModelScope.launch {
            try {
                repository.addBookToList(listId, bookId)
            } catch (e: Exception) {
            }
        }
    }

    fun removeBookFromList(listId: String, bookId: String) {
        viewModelScope.launch {
            try {
                repository.removeBookFromList(listId, bookId)
            } catch (e: Exception) {
            }
        }
    }

    fun loadUserLists(userId: String) {
        viewModelScope.launch {
            try {
                _userLists.value = repository.getUserBookLists(userId)
                    .filter { it.creator_id == userId }
            } catch (e: Exception) {
            }
        }
    }
}