package com.example.exchangingprivatelessons.ui.splash

data class SplashUiState(
    val loading : Boolean = false,
    val loggedIn: Boolean = false,
    val error   : String? = null
)
