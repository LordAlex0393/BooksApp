package com.example.bookapp.models

data class BookList(
    val id: String,
    val name: String,
    val createdAt: String,
    val books: List<Book>
)
