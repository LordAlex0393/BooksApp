package com.example.bookapp.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.bookapp.models.UserSession
import com.example.bookapp.repositories.BookRepository
import com.example.bookapp.ui.theme.PrimaryBook
import com.example.bookapp.ui.theme.SecondaryBook
import com.example.bookapp.ui.theme.SurfaceCard
import com.example.bookapp.ui.theme.TextPrimary
import com.example.bookapp.ui.theme.TextSecondary
import com.example.bookapp.viewModel.LibraryViewModel
import com.example.bookapp.viewModel.YearFilter
import com.example.bookapp.viewModelFactory.LibraryViewModelFactory

@Composable
fun LibraryScreen(
    navController: NavController,
    viewModel: LibraryViewModel = viewModel(factory = LibraryViewModelFactory(BookRepository()))
) {
    val allBooks by viewModel.books.collectAsState()
    val filteredBooks by viewModel.filteredBooks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState(initial = null)
    val searchQuery by viewModel.searchQuery.collectAsState()
    val genres by viewModel.availableGenres.collectAsState()
    val selectedGenre by viewModel.selectedGenre.collectAsState()
    val selectedYear by viewModel.selectedYearFilter.collectAsState()

    var visibleBooksCount by remember { mutableStateOf(16) }
    val currentUser = UserSession.currentUser.value

    LaunchedEffect(currentUser?.id) {
        currentUser?.id?.let { userId -> viewModel.loadUserLists(userId) }
    }

    LaunchedEffect(Unit) {
        viewModel.loadBooks()
    }

    Scaffold(
        topBar = { LibraryHeader(navController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        when {
            isLoading && allBooks.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBook)
                }
            }
            errorMessage != null && allBooks.isEmpty() -> {
                ErrorRetryView(
                    message = errorMessage ?: "Ошибка загрузки данных",
                    onRetry = {
                        viewModel.loadBooks()
                        currentUser?.id?.let { viewModel.loadUserLists(it) }
                    },
                    modifier = Modifier.fillMaxSize().padding(innerPadding)
                )
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    LibrarySearchAndFilters(
                        searchQuery = searchQuery,
                        onSearchChange = viewModel::onSearchQueryChange,
                        genres = genres,
                        selectedGenre = selectedGenre,
                        onGenreSelect = viewModel::onGenreSelected,
                        selectedYear = selectedYear,
                        onYearSelect = viewModel::onYearFilterSelected
                    )

                    if (filteredBooks.isEmpty()) {
                        Box(
                            modifier = Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "По вашему запросу книги не найдены",
                                    style = MaterialTheme.typography.bodyLarge.copy(color = TextSecondary),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                TextButton(onClick = { viewModel.resetFilters() }) {
                                    Text("Сбросить фильтры", color = PrimaryBook, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
                        val shouldLoadMore by remember(filteredBooks.size, visibleBooksCount) {
                            androidx.compose.runtime.derivedStateOf {
                                val lastVisibleIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                                val totalLoaded = minOf(visibleBooksCount, filteredBooks.size)
                                totalLoaded < filteredBooks.size && lastVisibleIndex >= totalLoaded - 4
                            }
                        }

                        LaunchedEffect(shouldLoadMore) {
                            if (shouldLoadMore) {
                                visibleBooksCount += 10
                            }
                        }

                        // Сбрасываем счетчик при смене поиска или фильтров
                        LaunchedEffect(searchQuery, selectedGenre, selectedYear) {
                            visibleBooksCount = 16
                            gridState.scrollToItem(0)
                        }

                        LazyVerticalGrid(
                            state = gridState,
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = filteredBooks.take(visibleBooksCount),
                                key = { it.id }
                            ) { book ->
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
                                            slideInVertically(initialOffsetY = { 80 }, animationSpec = spring(stiffness = Spring.StiffnessLow))
                                ) {
                                    BookGridItem(book, navController)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ErrorRetryView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(20.dp))
                .background(SurfaceCard, RoundedCornerShape(20.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(PrimaryBook.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = null,
                    tint = PrimaryBook,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Ошибка соединения",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBook)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Повторить")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryHeader(navController: NavController) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val iconScale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "profile_icon_scale"
    )
    CenterAlignedTopAppBar(
        title = {
            Text(
                "Библиотека",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)
            )
        },
        actions = {
            UserSession.currentUser.value?.let {
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(40.dp)
                        .background(PrimaryBook.copy(alpha = 0.1f), CircleShape)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { navController.navigate("profile") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Профиль",
                        modifier = Modifier
                            .size(24.dp)
                            .padding(bottom = 1.dp)
                            .alignmentOffset()
                            .scale(iconScale),
                        tint = PrimaryBook
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent
        )
    )
}
@Composable
fun BookGridItem(book: Book, navController: NavController) {
    var showAddToListDialog by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "book_card_press"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(335.dp)
            .scale(cardScale)
            .shadow(2.dp, shape = RoundedCornerShape(16.dp))
            .background(SurfaceCard, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { navController.navigate("book/${book.id}") }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(225.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                val painter = rememberAsyncImagePainter(model = book.cover_url)
                Image(
                    painter = painter,
                    contentDescription = "Обложка книги",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "%.1f".format(book.avg_rating ?: 0.0),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = book.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = book.author,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LazyRow(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (book.genres.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .background(TextSecondary.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Книга",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 11.sp)
                                )
                            }
                        }
                    } else {
                        items(book.genres.size) { index ->
                            val genreName = book.genres[index].name.trim()
                            if (genreName.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .background(PrimaryBook.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = genreName,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = PrimaryBook,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(SecondaryBook.copy(alpha = 0.12f), CircleShape)
                        .clickable { showAddToListDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Добавить в список",
                        tint = SecondaryBook,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
    if (showAddToListDialog) {
        AddToListDialog(book = book, onDismiss = { showAddToListDialog = false })
    }
}
@Composable
private fun AddToListDialog(
    book: Book,
    onDismiss: () -> Unit,
    viewModel: LibraryViewModel = viewModel(factory = LibraryViewModelFactory(BookRepository()))
) {
    val userLists by viewModel.userLists.collectAsState()
    var checkedStates by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(book.id, userLists) {
        checkedStates = userLists.associate { list ->
            list.id to list.books.any { it.id == book.id }
        }
        isLoading = false
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = SurfaceCard,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.BookmarkBorder, contentDescription = null, tint = PrimaryBook)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Сохранить в список",
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 20.sp)
                )
            }
        },
        text = {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBook)
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (userLists.isEmpty()) {
                        Text(
                            "У вас пока нет созданных списков. Создайте их в профиле.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        )
                    } else {
                        userLists.forEach { list ->
                            val isChecked = checkedStates[list.id] ?: false
                            val animatedScale by animateFloatAsState(
                                targetValue = if (isChecked) 1.02f else 1f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                label = "dialog_item_scale"
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .scale(animatedScale)
                                    .background(
                                        if (isChecked) PrimaryBook.copy(alpha = 0.04f) else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        checkedStates = checkedStates.toMutableMap().apply {
                                            put(list.id, !isChecked)
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { newCheckedState ->
                                        checkedStates = checkedStates.toMutableMap().apply {
                                            put(list.id, newCheckedState)
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = PrimaryBook)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = list.name,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    checkedStates.forEach { (listId, isChecked) ->
                        val listContainsBook = userLists
                            .find { it.id == listId }
                            ?.books?.any { it.id == book.id } ?: false
                        when {
                            isChecked && !listContainsBook -> viewModel.addBookToList(listId, book.id)
                            !isChecked && listContainsBook -> viewModel.removeBookFromList(listId, book.id)
                        }
                    }
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBook),
                enabled = !isLoading
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = TextSecondary)
            }
        }
    )
}
private fun Modifier.alignmentOffset() = this.offset(y = 0.dp)

@Composable
fun LibrarySearchAndFilters(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    genres: List<String>,
    selectedGenre: String?,
    onGenreSelect: (String?) -> Unit,
    selectedYear: YearFilter,
    onYearSelect: (YearFilter) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        // Строка поиска в карточке SurfaceCard с ручной тенью
        androidx.compose.material3.OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = {
                Text(
                    "Поиск по названию или автору...",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Search,
                    contentDescription = "Поиск",
                    tint = PrimaryBook
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    androidx.compose.material3.IconButton(onClick = { onSearchChange("") }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Close,
                            contentDescription = "Очистить",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceCard,
                unfocusedContainerColor = SurfaceCard,
                focusedBorderColor = PrimaryBook,
                unfocusedBorderColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .shadow(2.dp, RoundedCornerShape(14.dp))
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Фильтр по годам (LazyRow)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(YearFilter.entries.size) { index ->
                val filter = YearFilter.entries[index]
                val isSelected = filter == selectedYear
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) PrimaryBook else SurfaceCard)
                        .clickable { onYearSelect(filter) }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = filter.label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = if (isSelected) Color.White else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                }
            }
        }

        // Фильтр по жанрам (LazyRow)
        if (genres.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    val isAllSelected = selectedGenre == null
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isAllSelected) SecondaryBook else SurfaceCard)
                            .clickable { onGenreSelect(null) }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "Все жанры",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (isAllSelected) Color.White else TextSecondary,
                                fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    }
                }
                items(genres.size) { index ->
                    val genre = genres[index]
                    val isSelected = genre == selectedGenre
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) SecondaryBook else SurfaceCard)
                            .clickable { onGenreSelect(genre) }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = genre,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (isSelected) Color.White else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}
