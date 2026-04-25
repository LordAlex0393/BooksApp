package com.example.bookapp.models

import kotlinx.serialization.Serializable

@Serializable
data class Book(
    val id: String,
    val title: String,
    val author: String,
    val genres: List<Genre> = emptyList(),
    val description: String? = null,
    val cover_url: String? = null,
    val year: Int? = null,
    val reviews: List<Review>? = null,
    val avg_rating: Double? = null
)
