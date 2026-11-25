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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
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
import com.example.bookapp.ui.theme.TextSecondary
import com.example.bookapp.viewModel.LibraryViewModel
import com.example.bookapp.viewModelFactory.LibraryViewModelFactory

@Composable
fun LibraryScreen(
    navController: NavController,
    viewModel: LibraryViewModel = viewModel(factory = LibraryViewModelFactory(BookRepository()))
) {
    val books by viewModel.books.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var visibleBooksCount by remember { mutableStateOf(26) }
    val currentUser = UserSession.currentUser.value

    LaunchedEffect(currentUser?.id) {
        currentUser?.id?.let { userId ->
            viewModel.loadUserLists(userId)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadBooks()
    }

    Scaffold(
        topBar = { LibraryHeader(navController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (isLoading && books.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBook)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(
                        items = books.take(visibleBooksCount),
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

                if (visibleBooksCount < books.size) {
                    Button(
                        onClick = { visibleBooksCount += 8 },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBook)
                    ) {
                        Text("Загрузить ещё", style = MaterialTheme.typography.labelLarge)
                    }
                }
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

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(335.dp) // Уменьшили высоту карточки, чтобы она выглядела аккуратной и плотной
            .scale(cardScale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { navController.navigate("book/${book.id}") },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            // Красивая портретная обложка книги
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(225.dp) // Оставили отличную высоту для портретных пропорций
                    .clip(RoundedCornerShape(12.dp))
                    .shadow(1.dp, shape = RoundedCornerShape(12.dp))
            ) {
                val painter = rememberAsyncImagePainter(model = book.cover_url)
                Image(
                    painter = painter,
                    contentDescription = "Обложка книги",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Аккуратный бейдж рейтинга
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

            // Название книги
            Text(
                text = book.title ?: "Без названия",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Автор книги
            Text(
                text = book.author ?: "Автор неизвестен",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            // Аккуратный фиксированный отступ вместо расталкивающего weight(1f)
            Spacer(modifier = Modifier.height(10.dp))
            // Нижняя строка: Горизонтальный скролл жанров + Кнопка "+"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Горизонтальная скролл-лента для жанров
                androidx.compose.foundation.lazy.LazyRow(
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
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
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
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = genreName,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.primary,
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

                // Минималистичная стильная кнопка "+"
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), CircleShape)
                        .clickable { showAddToListDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Добавить в список",
                        tint = MaterialTheme.colorScheme.secondary,
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
        containerColor = MaterialTheme.colorScheme.surface,
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
                Text("Отмена", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    )
}
// Вспомогательный модификатор для выравнивания иконки профиля
private fun Modifier.alignmentOffset() = this.offset(y = (0).dp)