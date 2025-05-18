package com.example.bookapp.models

object UserSession {
    var currentUserDB: UserDB? = null
    var bookListsDB: List<BookListDB> = emptyList()
    var bookLists: List<BookList> = emptyList()
}