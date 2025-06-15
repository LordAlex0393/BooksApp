package com.example.bookapp.repositories

import android.util.Log
import com.example.bookapp.models.Book
import com.example.bookapp.models.BookList
import com.example.bookapp.models.Review

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class BookRepository {
    private val cache = mutableMapOf<String, List<BookList>>()

    suspend fun getUserBookLists(userId: String): List<BookList> {
        // Проверяем кэш перед запросом к серверу
        return cache[userId] ?: loadAndCacheUserBookLists(userId)
    }

    private suspend fun loadAndCacheUserBookLists(userId: String): List<BookList> {
        val bookListsDB = SupabaseClient.client
            .from("book_lists")
            .select(Columns.raw("""
                id, 
                name, 
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
                created_at = bookListDB.created_at,
                books = getBooksByListId(bookListDB.id)
            )
        }

        // Сохраняем в кэш
        cache[userId] = bookLists
        return bookLists
    }

    private suspend fun getBooksByListId(bookListId: String): List<Book> {
        return SupabaseClient.client
            .from("books")
            .select(Columns.raw("""
            id, 
            title, 
            author, 
            genre, 
            description, 
            cover_url, 
            year, 
            avg_rating,
            book_list_items!inner(book_list_id)
        """)) {
                filter { eq("book_list_items.book_list_id", bookListId) }
            }
            .decodeList<Book>()
    }

    suspend fun getBookById2(bookId: String): Book {
        return SupabaseClient.client
            .from("books")
            .select {
                filter { eq("id", bookId) }
            }
            .decodeSingle<Book>()
    }

    suspend fun getBookById(bookId: String): Book {
        return SupabaseClient.client.from("books")
            .select(Columns.raw("""
            *,
            reviews:reviews!book_id(
                id,
                book_id,
                user_id,
                rating,
                text,
                created_at,
                user:users!user_id(username)
            )
            """
            )) {
                filter { eq("id", bookId) }
            }
            .decodeSingle<Book>()
    }


    // BookRepository.kt
    suspend fun saveReview(review: Review) {
        try {
            SupabaseClient.client.from("reviews")
                .upsert(review) {  // Используем upsert вместо insert
                    onConflict = "user_id,book_id"  // Указываем колонки для проверки конфликта
                    ignoreDuplicates = false  // Обновляем при конфликте
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

    fun clearCache() {
        cache.clear()
    }
}