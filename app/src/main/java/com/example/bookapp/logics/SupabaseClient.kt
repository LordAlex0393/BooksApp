package com.example.bookapp.logics

import com.example.bookapp.BuildConfig
import com.example.bookapp.models.Book
import com.example.bookapp.models.BookList
import com.example.bookapp.models.UserSession
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        install(Postgrest.Companion)
    }
}

suspend fun getUserBookLists(userId: String): List<BookList> {
    return SupabaseClient.client
        .from("book_lists")
        .select(
            Columns.raw("""
            id,
            name,
            created_at,
            user_book_lists!inner(user_id)
            """)
        ) {
            filter {
                eq("user_book_lists.user_id", userId)
            }
        }
        .decodeList<BookList>()
}

suspend fun getBooksByListId(bookListId: String): List<Book> {
    return SupabaseClient.client
        .from("books")
        .select(
            Columns.raw("""
            id,
            title,
            author,
            genre,
            description,
            cover_url,
            year,
            book_list_items!inner(book_list_id)
            """)
        ) {
            filter {
                eq("book_list_items.book_list_id", bookListId)
            }
        }
        .decodeList<Book>()
}

suspend fun initializeUserBookListsParallel() = coroutineScope {
    val currentUser = UserSession.currentUser.value ?: return@coroutineScope

    val bookListsDB = getUserBookLists(currentUser.id)

    val updatedBookLists = bookListsDB.map { bookListDB ->
        async {
            val books = getBooksByListId(bookListDB.id)
            BookList(
                id = bookListDB.id,
                name = bookListDB.name,
                created_at = bookListDB.created_at,
                books = books
            )
        }
    }.awaitAll()

    UserSession.updateBookLists(updatedBookLists)
}
