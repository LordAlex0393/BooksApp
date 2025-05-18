package com.example.bookapp.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookapp.logics.BookRepository
import com.example.bookapp.models.BookList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: BookRepository) : ViewModel() {
    private val _bookLists = MutableStateFlow<List<BookList>>(emptyList())
    val bookLists: StateFlow<List<BookList>> = _bookLists

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadUserBookLists(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _bookLists.value = repository.getUserBookLists(userId)
            } catch (e: Exception) {
                // Обработка ошибок
            } finally {
                _isLoading.value = false
            }
        }
    }
}