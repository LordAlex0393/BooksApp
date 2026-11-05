package com.example.bookapp.repositories

import android.util.Log
import com.example.bookapp.models.Book
import com.example.bookapp.models.BookList
import com.example.bookapp.models.Genre
import com.example.bookapp.models.Review
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock.System
import kotlinx.serialization.Serializable
import kotlin.coroutines.cancellation.CancellationException

class BookRepository {
    private val cache = mutableMapOf<String, List<BookList>>()
    private val similarBooksCache = mutableMapOf<String, List<Book>>()

    suspend fun getUserBookLists(userId: String): List<BookList> {
        return cache[userId] ?: loadAndCacheUserBookLists(userId)
    }

    private suspend fun loadAndCacheUserBookLists(userId: String): List<BookList> {
        val bookListsDB = SupabaseClient.client
            .from("book_lists")
            .select(Columns.raw("""
                id, 
                name, 
                creator_id,
                created_at, 
                user_book_lists!inner(user_id)
            """)) {
                filter { eq("user_book_lists.user_id", userId) }
            }
            .decodeList<BookList>()

        val bookLists = bookListsDB.map { bookListDB ->
            BookList(
                id = bookListDB.id,
                name = bookListDB.name,
                creator_id = bookListDB.creator_id,
                created_at = bookListDB.created_at,
                books = getBooksByListId(bookListDB.id)
            )
        }

        cache[userId] = bookLists
        return bookLists
    }

    private suspend fun getBooksByListId(bookListId: String): List<Book> = withContext(Dispatchers.IO) {
        SupabaseClient.client
            .from("books")
            .select(Columns.raw("""
            id,
            title,
            author,
            description,
            cover_url,
            year,
            avg_rating,
            book_genres(
                genres(
                    id,
                    name
                )
            ),
            book_list_items!inner(book_list_id)
        """)) {
                filter { eq("book_list_items.book_list_id", bookListId) }
            }
            .decodeList<BookWithGenres>()
            .map { it.toBook() }
    }

    suspend fun getAllBooks(): List<Book> = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.client
                .from("books")
                .select(Columns.raw("""
                    id,
                    title,
                    author,
                    cover_url,
                    description,
                    year,
                    avg_rating,
                    book_genres(
                        genres(
                            id,
                            name
                        )
                    )
                """))
                .decodeList<BookWithGenres>()
                .map { it.toBook() }
        } catch (e: Exception) {
            Log.e("BookRepo", "Error loading books", e)
            emptyList()
        }
    }

    suspend fun getBookById(bookId: String): Book = withContext(Dispatchers.IO) {
        SupabaseClient.client
            .from("books")
            .select(Columns.raw("""
            id,
            title,
            author,
            description,
            cover_url,
            year,
            avg_rating,
            book_genres(
                genres(
                    id,
                    name
                )
            ),
            reviews:reviews!book_id(
                id,
                book_id,
                user_id,
                rating,
                text,
                created_at,
                user:users!user_id(username)
            )
        """)) {
                filter { eq("id", bookId) }
            }
            .decodeSingle<BookWithGenresAndReviews>()
            .toBook()
    }

    suspend fun saveReview(review: Review) {
        try {
            SupabaseClient.client.from("reviews")
                .upsert(review) {
                    onConflict = "user_id,book_id"
                    ignoreDuplicates = false
                }
            Log.d("Review", "Review upserted successfully")
        } catch (e: Exception) {
            Log.e("Review", "Error upserting review", e)
            throw e
        }
    }

    suspend fun getUserReview(bookId: String, userId: String): Review? {
        return SupabaseClient.client.from("reviews")
            .select {
                filter {
                    eq("book_id", bookId)
                    eq("user_id", userId)
                }
            }
            .decodeSingleOrNull()
    }

    suspend fun removeBookFromList(listId: String, bookId: String) {
        SupabaseClient.client.from("book_list_items")
            .delete {
                filter {
                    eq("book_list_id", listId)
                    eq("book_id", bookId)
                }
            }
        cache.clear()
    }

    suspend fun addBookToList(listId: String, bookId: String) {
        try {
            SupabaseClient.client.from("book_list_items")
                .insert(
                    mapOf(
                        "book_list_id" to listId,
                        "book_id" to bookId,
                        "added_at" to System.now().toString()
                    )
                )
            cache.clear()
        } catch (e: Exception) {
            Log.e("BookRepository", "Error adding book to list", e)
            throw e
        }
    }

    suspend fun createBookList(userId: String, listName: String): BookList {
        try {
            val newList = SupabaseClient.client.from("book_lists")
                .insert(
                    mapOf(
                        "name" to listName,
                        "created_at" to System.now().toString(),
                        "creator_id" to userId
                    )
                ) {
                    select(columns = Columns.list("id", "name", "creator_id", "created_at"))
                }
                .decodeSingle<BookList>()

            SupabaseClient.client.from("user_book_lists")
                .insert(
                    mapOf(
                        "user_id" to userId,
                        "book_list_id" to newList.id
                    )
                )

            cache.clear()
            return newList.copy(books = emptyList())

        } catch (e: Exception) {
            Log.e("BookRepository", "Error creating list", e)
            throw e
        }
    }

    suspend fun deleteBookList(listId: String) {
        try {
            SupabaseClient.client.from("book_lists")
                .delete {
                    filter { eq("id", listId) }
                }
            cache.clear()
        } catch (e: Exception) {
            Log.e("BookRepository", "Error deleting list", e)
            throw e
        }
    }

    suspend fun isListNameUnique(userId: String, name: String): Boolean {
        return SupabaseClient.client.from("book_lists")
            .select {
                filter {
                    eq("creator_id", userId)
                    eq("name", name)
                }
            }
            .decodeList<BookList>()
            .isEmpty()
    }

    fun clearCache() {
        cache.clear()
    }

    // Добавьте этот data class в файл (например, в конец файла BookRepository.kt)
    @Serializable
    private data class SimilarBooksParams(
        val p_book_id: String,
        val p_limit: Int
    )

    // Исправленная функция
    suspend fun getSimilarBooks(bookId: String, limit: Int = 6): List<Book> = withContext(Dispatchers.IO) {
        similarBooksCache[bookId]?.let {
            if (it.isNotEmpty()) return@withContext it.take(limit)
        }

        try {
            val similarIds = mutableListOf<String>()

            // Запрос для book1_id (bookId как первая книга) - используем конкретный тип
            val response1 = SupabaseClient.client
                .from("book_similarity")
                .select(columns = Columns.raw("book2_id, score")) {
                    filter { eq("book1_id", bookId) }
                    order(column = "score", Order.DESCENDING)
                    limit(limit.toLong())
                }
                .decodeList<SimilarityResponse>()

            similarIds.addAll(response1.mapNotNull { it.book2_id })

            // Если не нашли, ищем как book2_id
            if (similarIds.isEmpty()) {
                val response2 = SupabaseClient.client
                    .from("book_similarity")
                    .select(columns = Columns.raw("book1_id, score")) {
                        filter { eq("book2_id", bookId) }
                        order(column = "score", Order.DESCENDING)
                        limit(limit.toLong())
                    }
                    .decodeList<SimilarityResponse>()

                similarIds.addAll(response2.mapNotNull { it.book1_id })
            }

            if (similarIds.isEmpty()) {
                return@withContext emptyList()
            }

            // Загружаем книги через getAllBooks()
            val allBooks = getAllBooks()
            val bookMap = allBooks.associateBy { it.id }

            val result = similarIds.mapNotNull { bookMap[it] }

            if (result.isNotEmpty()) {
                similarBooksCache[bookId] = result
            }

            result

        } catch (e: CancellationException) {
            Log.d("BookRepo", "Similar books loading was cancelled for $bookId")
            emptyList()
        } catch (e: Exception) {
            Log.e("BookRepo", "Error loading similar books for $bookId", e)
            emptyList()
        }
    }

    // Добавьте функцию очистки кэша
    fun clearSimilarBooksCache() {
        similarBooksCache.clear()
    }

    // Data class для ответа из таблицы book_similarity
    @Serializable
    private data class SimilarityResponse(
        val book2_id: String? = null,
        val book1_id: String? = null,
        val score: Double
    )

// Добавьте эти data классы:

    @Serializable
    private data class SimilarBookWithDetails(
        val book2_id: String,
        val score: Double,
        val books: BookWithGenres? = null
    ) {
        fun toBook(): Book? = books?.toBook()
    }

    @Serializable
    private data class BookWithGenres(
        val id: String,
        val title: String,
        val author: String,
        val cover_url: String,
        val description: String,
        val year: Int,
        val avg_rating: Double,
        val book_genres: List<BookGenreRelation> = emptyList()
    ) {
        fun toBook() = Book(
            id = id,
            title = title,
            author = author,
            genres = book_genres.mapNotNull { it.genres },
            cover_url = cover_url,
            description = description,
            year = year,
            avg_rating = avg_rating
        )
    }

//    @Serializable
//    private data class SimilarityResponse(
//        val book2_id: String,
//        val score: Double
//    )

    // ======================== МОДЕЛИ ========================

//    @Serializable
//    private data class BookWithGenres(
//        val id: String,
//        val title: String,
//        val author: String,
//        val cover_url: String,
//        val description: String,
//        val year: Int,
//        val avg_rating: Double,
//        val book_genres: List<BookGenreRelation> = emptyList()
//    ) {
//        fun toBook() = Book(
//            id = id,
//            title = title,
//            author = author,
//            genres = book_genres.mapNotNull { it.genres },
//            cover_url = cover_url,
//            description = description,
//            year = year,
//            avg_rating = avg_rating
//        )
//    }

    @Serializable
    private data class BookWithGenresAndReviews(
        val id: String,
        val title: String,
        val author: String,
        val cover_url: String,
        val description: String,
        val year: Int,
        val avg_rating: Double,
        val book_genres: List<BookGenreRelation> = emptyList(),
        val reviews: List<Review> = emptyList()
    ) {
        fun toBook() = Book(
            id = id,
            title = title,
            author = author,
            genres = book_genres.mapNotNull { it.genres },
            cover_url = cover_url,
            description = description,
            year = year,
            avg_rating = avg_rating,
            reviews = reviews
        )
    }
    @Serializable
    private data class BookGenreRelation(
        val genres: Genre? = null
    )

    @Serializable
    private data class GenreResponse(
        val genres: Genre?
    )
}