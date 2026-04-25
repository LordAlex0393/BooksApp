package com.example.bookapp.models

import kotlinx.serialization.Serializable
@Serializable
data class Genre(
    val id: Int,
    val name: String
)

@Serializable
data class GenreResponse(
    val genre_id: Int,
    val genres: GenreName
)

@Serializable
data class GenreName(
    val id: Int,
    val name: String
)