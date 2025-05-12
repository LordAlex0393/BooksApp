package com.example.bookapp.logics

import com.example.bookapp.BuildConfig
import com.example.bookapp.models.BookListDB
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        install(Postgrest.Companion)
    }
}


//suspend fun getUserBookLists(userId: String): List<BookListDB> {
//    return SupabaseClient.client
//        .from("book_lists")
//        .select(
//            Columns.raw("id, name, users!user_book_lists(id)"
//        )) {
//            filter {
//                eq("users.id", userId)  // Фильтр через отношение
//            }
//        }
//        .decodeList<BookListDB>()
//}

suspend fun getUserBookLists(userId: String): List<BookListDB> {
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
        .decodeList<BookListDB>()
}
