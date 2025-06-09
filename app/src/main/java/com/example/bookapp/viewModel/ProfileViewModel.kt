package com.example.bookapp.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookapp.models.BookList
import com.example.bookapp.repositories.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: BookRepository) : ViewModel() {

    // Состояние загрузки
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Списки книг
    private val _bookLists = MutableStateFlow<List<BookList>>(emptyList())
    val bookLists: StateFlow<List<BookList>> = _bookLists.asStateFlow()

    // Ошибки
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Загрузка данных
    fun loadUserBookLists(userId: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                if (forceRefresh) {
                    repository.clearCache()
                }
                _bookLists.value = repository.getUserBookLists(userId)
            } catch (e: Exception) {
                _error.value = when {
                    e is NoInternetException -> "Нет интернет-соединения"
                    e is ServerException -> "Ошибка сервера"
                    else -> "Не удалось загрузить данные"
                }
                Log.e("ProfileViewModel", "Error loading books", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Обновление отдельного списка
    fun updateBookList(updatedList: BookList) {
        _bookLists.value = _bookLists.value.map { list ->
            if (list.id == updatedList.id) updatedList else list
        }
    }

    // Сброс ошибки
    fun clearError() {
        _error.value = null
    }
}

// Кастомные исключения (можно вынести в отдельный файл)
class NoInternetException : Exception()
class ServerException : Exception()