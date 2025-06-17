package com.example.bookapp.models

import kotlinx.serialization.Serializable

@Serializable
data class BookList(
    val id: String,
    val name: String,
    val creator_id: String,
    val created_at: String,
    val books: List<Book> = emptyList() // Поле не участвует в сериализации
) {
    fun toDBModel() = BookList(id, name, creator_id, created_at)
}