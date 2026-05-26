package com.example.futbolnomade.presentation.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")

    object Home : Screen("home/{nombreUsuario}") {
        fun createRoute(nombreUsuario: String): String {
            return "home/$nombreUsuario"
        }
    }

    object Elementos : Screen("elementos")
    object Acerca : Screen("acerca")
}