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

    object Partidos : Screen("partidos")
    object CrearPartido : Screen("crear_partido")

    object DetallePartido : Screen("detalle_partido/{partidoId}") {
        fun createRoute(partidoId: Int): String {
            return "detalle_partido/$partidoId"
        }
    }
}