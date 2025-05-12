package com.example.bookapp.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Book(
    val id: String,
    val title: String,
    val author: String,
    val genre: String? = null,
    val description: String? = null,
    @SerialName("cover_url") val coverUrl: String? = null,
    val year: Int? = null
)