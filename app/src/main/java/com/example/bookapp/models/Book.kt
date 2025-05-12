package com.example.bookapp.models

import kotlinx.serialization.Serializable

@Serializable
data class Book(
    val id: String,
    val title: String,
    val author: String,
    val genre: String?,
    val description: String?,
    val cover_url: String?,
    val year: Int?
)