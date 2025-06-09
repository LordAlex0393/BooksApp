package com.example.bookapp.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookapp.models.UserSession
import com.example.bookapp.repositories.AuthRepository
import com.example.bookapp.repositories.AuthResult
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var error by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var loginSuccess by mutableStateOf(false)

    fun login() {
        if (email.isEmpty() || password.isEmpty()) {
            error = "Заполните все поля"
            return
        }

        viewModelScope.launch {
            isLoading = true
            error = ""

            val result = authRepository.loginUser(email, password)

            if (result.isSuccess) {
                UserSession.login((result as AuthResult.Success).user)
                loginSuccess = true
            } else {
                error = result.errorMessage ?: "Ошибка входа"
            }

            isLoading = false
        }
    }
}