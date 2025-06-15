package com.example.bookapp.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Review(
    val id: String? = null,
    val book_id: String? = null,
    val user_id: String? = null,
    val rating: Int,
    val text: String? = null,
    val created_at: String? = null,
    @SerialName("user")
    val username: ReviewUser? = null
)

@Serializable
data class ReviewUser(
    val username: String
)