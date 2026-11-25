package com.example.bookapp.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Основные брендовые цвета (Мягкий премиальный фиолетовый)
val PrimaryBook = Color(0xFF6C5CE7)
val PrimaryLight = Color(0xFFA29BFE)
val SecondaryBook = Color(0xFF00CEC9)

// Нейтральные оттенки для фона и поверхностей
val BackgroundLight = Color(0xFFF9FAFC)
val SurfaceCard = Color(0xFFFFFFFF)

// Текст
val TextPrimary = Color(0xFF2D3436)
val TextSecondary = Color(0xFF636E72)

// Ошибки и Успех (для валидации форм)
val AccentError = Color(0xFFD63031)
val AccentSuccess = Color(0xFF2ECC71)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBook,
    secondary = SecondaryBook,
    background = BackgroundLight,
    surface = SurfaceCard,
    onPrimary = SurfaceCard,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = AccentError
)

@Composable
fun BookAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}