package com.example.bookapp.repositories

import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.bookapp.models.User
import io.github.jan.supabase.postgrest.from

class AuthRepository {
    suspend fun loginUser(email: String, password: String): AuthResult {
        return try {
            val user = SupabaseClient.client
                .from("users")
                .select { filter { eq("email", email) } }
                .decodeSingleOrNull<User>() ?: return AuthResult.Error("Пользователь не найден")

            val isPasswordValid = BCrypt.verifyer()
                .verify(password.toCharArray(), user.password_hash)
                .verified

            if (!isPasswordValid) {
                AuthResult.Error("Неверный пароль")
            } else {
                AuthResult.Success(user)
            }
        } catch (e: Exception) {
            AuthResult.Error("Ошибка сети: ${e.localizedMessage}")
        }
    }
}

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()

    val isSuccess: Boolean get() = this is Success
    val errorMessage: String? get() = (this as? Error)?.message
}