package com.example.bookapp.logics

import com.example.bookapp.models.Book
import com.example.bookapp.models.BookList
import com.example.bookapp.models.BookListDB
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class BookRepository {

    private val cache = mutableMapOf<String, List<BookList>>()

    suspend fun getUserBookLists(userId: String): List<BookList> {
        val bookListsDB = SupabaseClient.client
            .from("book_lists")
            .select(Columns.Companion.raw("""id, name, created_at, user_book_lists!inner(user_id)""")) {
                filter { eq("user_book_lists.user_id", userId) }
            }
            .decodeList<BookListDB>()

        return bookListsDB.map { bookListDB ->
            val books = getBooksByListId(bookListDB.id)
            BookList(
                id = bookListDB.id,
                name = bookListDB.name,
                createdAt = bookListDB.createdAt,
                books = books
            )
        }
    }

    private suspend fun getBooksByListId(bookListId: String): List<Book> {
        return SupabaseClient.client
            .from("books")
            .select(Columns.Companion.raw("""id, title, author, genre, description, cover_url, year, book_list_items!inner(book_list_id)""")) {
                filter { eq("book_list_items.book_list_id", bookListId) }
            }
            .decodeList<Book>()
    }

}