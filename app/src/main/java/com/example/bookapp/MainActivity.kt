package com.example.bookapp

import LoginScreen
import ProfileScreen
import SignUpScreen
import WelcomeScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bookapp.models.Book
import com.example.bookapp.models.Genre
import com.example.bookapp.pages.AllReviewsScreen
import com.example.bookapp.pages.BookListScreen
import com.example.bookapp.pages.BookScreen
import com.example.bookapp.pages.LibraryScreen
import com.example.bookapp.repositories.SupabaseClient
import io.github.jan.supabase.postgrest.from

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@Composable
fun App() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "welcome") {
        composable("welcome") { WelcomeScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("signup") { SignUpScreen(navController) }
        // ЭКРАН БИБЛИОТЕКИ (стартовый)
        composable(
            "library",
            // Когда заходим в библиотеку (например, после логина) - плавно проявляется
            enterTransition = { fadeIn(animationSpec = tween(800)) },
            exitTransition = { fadeOut(animationSpec = tween(800)) },
            // Когда возвращаемся на библиотеку из других экранов - плавно проявляется
            popEnterTransition = { fadeIn(animationSpec = tween(800)) },
            popExitTransition = { fadeOut(animationSpec = tween(800)) }
        ) { LibraryScreen(navController) }

// ЭКРАН ПРОФИЛЯ
        composable(
            "profile",
            // Когда нажимаем на профиль (идем вперед) - выезжает СЛЕВА (то есть сдвиг вправо)
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
            // Когда уходим из профиля вперед (например, в список книг) - уезжает ВЛЕВО
            exitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() },

            // Когда возвращаемся НАЗАД в профиль (например, из списка книг) - выезжает СПРАВА (сдвиг влево)
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
            // Когда нажимаем "Назад" из профиля в библиотеку - уезжает ВПРАВО
            popExitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() }
        ) { ProfileScreen(navController) }
        composable(
            "book/{bookId}",
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            BookScreen(
                navController = navController,
                bookId = backStackEntry.arguments?.getString("bookId")!!
            )
        }
        composable("book/{bookId}/reviews") { backStackEntry ->
            AllReviewsScreen(
                navController = navController,
                bookId = backStackEntry.arguments?.getString("bookId")!!
            )
        }
        composable("list/{listId}") { backStackEntry ->
            BookListScreen(
                navController = navController,
                listId = backStackEntry.arguments?.getString("listId")!!
            )
        }
    }
    LaunchedEffect(Unit) {
        // Делаем "лёгкий" запрос, чтобы прогреть соединение
        try {
            SupabaseClient.client.from("genres").select().decodeList<Genre>()
            SupabaseClient.client.from("books").select().decodeList<Book>()
        } catch (e: Exception) {
            // Игнорируем ошибки прогрева
        }
    }
}