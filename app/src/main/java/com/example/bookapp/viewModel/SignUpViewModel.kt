package com.example.bookapp.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookapp.repositories.AuthRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.net.UnknownHostException

class SignUpViewModel(private val authRepository: AuthRepository) : ViewModel() {
    // Состояния формы
    var username by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")

    // Состояния UI
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var registrationSuccess by mutableStateOf(false)

    fun register() {
        if (password != confirmPassword) {
            error = "Пароли не совпадают"
            return
        }

        viewModelScope.launch {
            if (!validateEmail(email)) {
                error = "Email уже занят"
                return@launch
            }

            viewModelScope.launch {
                isLoading = true
                error = null

                val result = authRepository.registerUser(
                    username = username,
                    email = email,
                    password = password
                )

                result.onSuccess {
                    registrationSuccess = true
                    clearForm()
                }.onFailure { e ->
                    error = when (e) {
                        is UnknownHostException -> "Нет подключения к интернету"
                        else -> "Ошибка регистрации: ${e.message}"
                    }
                }

                isLoading = false
            }
        }
    }

    private fun clearForm() {
        username = ""
        email = ""
        password = ""
        confirmPassword = ""
    }

    suspend fun validateEmail(email: String): Boolean {
        return viewModelScope.async {
            !authRepository.checkEmailExists(email)
        }.await()
    }
}