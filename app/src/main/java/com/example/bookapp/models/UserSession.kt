package com.example.bookapp.models

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

object UserSession {
    private val _currentUser = mutableStateOf<User?>(null)
    val currentUser: State<User?> get() = _currentUser

    fun login(user: User) {
        _currentUser.value = user
    }
}