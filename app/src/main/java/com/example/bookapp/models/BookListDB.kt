package com.example.bookapp.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BookListDB(
    val id: String,
    val name: String,
    @SerialName("created_at") val createdAt: String
)