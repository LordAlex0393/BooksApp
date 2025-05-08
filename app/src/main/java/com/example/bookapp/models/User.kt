package com.example.bookapp.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val username: String,
    val email: String,
    val password_hash: String
)