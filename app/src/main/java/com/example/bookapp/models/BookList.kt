package com.example.bookapp.models

import kotlinx.serialization.Serializable

@Serializable
data class BookList(
    val id: String,
    val name: String,
    val created_at: String,
    val books: List<Book> = emptyList() // Поле не участвует в сериализации
) {
    fun toDBModel() = BookList(id, name, created_at)
}