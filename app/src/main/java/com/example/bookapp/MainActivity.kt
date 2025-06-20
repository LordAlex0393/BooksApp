package com.example.bookapp

import LoginScreen
import ProfileScreen
import SignUpScreen
import WelcomeScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bookapp.pages.AllReviewsScreen
import com.example.bookapp.pages.BookListScreen
import com.example.bookapp.pages.BookScreen
import com.example.bookapp.pages.LibraryScreen

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
        composable("profile") { ProfileScreen(navController) }
        composable("library") { LibraryScreen(navController) }
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
}