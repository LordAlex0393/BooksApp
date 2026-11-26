package com.example.bookapp.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookapp.models.Book
import com.example.bookapp.models.BookList
import com.example.bookapp.repositories.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class YearFilter(val label: String) {
    ALL("Все годы"),
    CENTURY_19("XIX век (до 1900)"),
    FIRST_HALF_20("1900–1950 гг."),
    SECOND_HALF_20("1951–1990 гг."),
    MODERN("Современные (с 1991)")
}


class LibraryViewModel(private val repository: BookRepository) : ViewModel() {
    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _userLists = MutableStateFlow<List<BookList>>(emptyList())
    val userLists: StateFlow<List<BookList>> = _userLists.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedGenre = MutableStateFlow<String?>(null)
    val selectedGenre: StateFlow<String?> = _selectedGenre.asStateFlow()

    private val _selectedYearFilter = MutableStateFlow(YearFilter.ALL)
    val selectedYearFilter: StateFlow<YearFilter> = _selectedYearFilter.asStateFlow()

    val availableGenres: StateFlow<List<String>> = _books
        .map { list ->
            list.flatMap { it.genres }
                .map { it.name.trim() }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredBooks: StateFlow<List<Book>> = combine(
        _books,
        _searchQuery,
        _selectedGenre,
        _selectedYearFilter
    ) { allBooks, query, genre, yearFilter ->
        allBooks.filter { book ->
            val matchesQuery = query.isBlank() ||
                    book.title.contains(query.trim(), ignoreCase = true) ||
                    book.author.contains(query.trim(), ignoreCase = true)

            val matchesGenre = genre == null ||
                    book.genres.any { it.name.trim().equals(genre, ignoreCase = true) }

            val matchesYear = when (yearFilter) {
                YearFilter.ALL -> true
                YearFilter.CENTURY_19 -> (book.year ?: 0) in 1..1899
                YearFilter.FIRST_HALF_20 -> (book.year ?: 0) in 1900..1950
                YearFilter.SECOND_HALF_20 -> (book.year ?: 0) in 1951..1990
                YearFilter.MODERN -> (book.year ?: 0) >= 1991
            }

            matchesQuery && matchesGenre && matchesYear
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onGenreSelected(genre: String?) {
        _selectedGenre.value = if (_selectedGenre.value == genre) null else genre
    }

    fun onYearFilterSelected(filter: YearFilter) {
        _selectedYearFilter.value = filter
    }

    fun resetFilters() {
        _searchQuery.value = ""
        _selectedGenre.value = null
        _selectedYearFilter.value = YearFilter.ALL
    }

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
            try { repository.addBookToList(listId, bookId) } catch (_: Exception) {}
        }
    }

    fun removeBookFromList(listId: String, bookId: String) {
        viewModelScope.launch {
            try { repository.removeBookFromList(listId, bookId) } catch (_: Exception) {}
        }
    }

    fun loadUserLists(userId: String) {
        viewModelScope.launch {
            try {
                _userLists.value = repository.getUserBookLists(userId)
                    .filter { it.creator_id == userId }
            } catch (_: Exception) {}
        }
    }
}
