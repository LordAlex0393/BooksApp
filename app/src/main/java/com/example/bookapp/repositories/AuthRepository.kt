package com.example.bookapp.repositories

import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.bookapp.models.User
import io.github.jan.supabase.postgrest.from

class AuthRepository {
    suspend fun registerUser(
        username: String,
        email: String,
        password: String
    ): Result<Unit> {
        return try {
            val hashedPassword = BCrypt.withDefaults()
                .hashToString(12, password.toCharArray())

            SupabaseClient.client.from("users").insert(mapOf(
                "username" to username,
                "email" to email,
                "password_hash" to hashedPassword
            )) { select() }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkEmailExists(email: String): Boolean {
        return SupabaseClient.client
            .from("users")
            .select { filter { eq("email", email) } }
            .decodeSingleOrNull<User>() != null
    }

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