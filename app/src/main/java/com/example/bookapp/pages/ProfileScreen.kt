import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bookapp.models.UserSession

private const val LIST_TITLE_MAX_WIDTH = 0.8f
private val BOOK_ITEM_HEIGHT = 180.dp
private val BOOK_ITEM_WIDTH = 120.dp
private val LIST_SPACING = 12.dp
private val BUTTON_MAX_HEIGHT = 48.dp
private val PADDING = 52.dp

@Composable
fun ProfileScreen(navController: NavController) {
    // Временные данные для демонстрации
    val bookLists = listOf(
        BookList("Category1", listOf(
            BookSample("BookName1", "BookAuthor1"),
            BookSample("BookName2", "BookAuthor2"),
            BookSample("BookName3", "BookAuthor3"),
            BookSample("BookName4", "BookAuthor4"),
        )),
        BookList("Category2", listOf(
            BookSample("BookName5", "BookAuthor5"),
        )),
        BookList("Category3", listOf(
            BookSample("BookName6", "BookAuthor6"),
            BookSample("BookName7", "BookAuthor7"),
        )),
    )

// Основной контейнер с возможностью скролла
    Box(modifier = Modifier.fillMaxSize()) {
        // Вертикальный скроллинг
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(PADDING),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Заголовок "Профиль"
            item {
                Text(
                    text = "Профиль",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                UserSession.currentUser?.let { user ->
                    Text(
                        text = user.username,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 16.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Left
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Списки книг
            items(bookLists.size) { index ->
                val bookList = bookLists[index]
                BookListSection(bookList, navController)
                Spacer(modifier = Modifier.height(28.dp))
            }

            // Кнопка "Новый список"
            item {
                OutlinedButton(
                    onClick = { /* ... */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(BUTTON_MAX_HEIGHT)
                ) {
                    Text("Новый список")
                }
            }
        }
    }
}

// Исправленный BookListSection
@Composable
private fun BookListSection(bookList: BookList, navController: NavController) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Заголовок списка с кнопкой "Посмотреть все"
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = bookList.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth(LIST_TITLE_MAX_WIDTH)
                    .weight(1f)
            )

            TextButton(
                onClick = { /* Навигация на полный список */ },
                modifier = Modifier.weight(0.8f)
            ) {
                Text("Посмотреть все", style = MaterialTheme.typography.labelLarge)
            }
        }

        //Spacer(modifier = Modifier.height(1.dp))

        // Исправленный LazyRow
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(LIST_SPACING),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(bookList.books.size) { index ->
                BookItem(bookList.books[index])
            }
        }
    }
}

@Composable
private fun BookItem(book: BookSample) {
    Column(
        modifier = Modifier
            .width(BOOK_ITEM_WIDTH)
            .height(BOOK_ITEM_HEIGHT),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Временная заглушка для обложки книги
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Название книги
        Text(
            text = book.title,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Автор
        Text(
            text = book.author,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// Модели данных
data class BookList(
    val title: String,
    val books: List<BookSample>
)

data class BookSample(
    val title: String,
    val author: String
)