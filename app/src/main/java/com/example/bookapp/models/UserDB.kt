package com.example.bookapp.models

import kotlinx.serialization.Serializable

@Serializable
data class UserDB(
    val id: String,
    val username: String,
    val email: String,
    val password_hash: String
)