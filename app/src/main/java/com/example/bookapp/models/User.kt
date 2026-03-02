package com.example.bookapp.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val username: String,
    val email: String,
    val password_hash: String = "", // Только для входа/регистрации
    val bookLists: List<BookList> = emptyList(),
    val lastAuthTime: Long = 0 // Для сессии
)