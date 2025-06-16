package com.example.bookapp.viewModelFactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bookapp.repositories.BookRepository
import com.example.bookapp.viewModel.ProfileViewModel

class ProfileViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(BookRepository()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}