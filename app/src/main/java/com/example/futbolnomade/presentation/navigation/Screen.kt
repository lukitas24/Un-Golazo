package com.example.futbolnomade.presentation.navigation

sealed class Screen(val route: String) {

    object Login : Screen("login")

    object Home : Screen("home/{nombreUsuario}") {
        fun createRoute(nombreUsuario: String) = "home/$nombreUsuario"
    }

    object Partidos : Screen("partidos")

    object CrearPartido : Screen("crear_partido")

    object Canchas : Screen("canchas")

    object CrearCancha : Screen("crear_cancha")

    object DetallePartido : Screen("detalle_partido/{partidoId}") {
        fun createRoute(partidoId: Int) = "detalle_partido/$partidoId"
    }

    object Elementos : Screen("elementos")

    object Acerca : Screen("acerca")

    // 🔥 NUEVO: para bottom bar (IMPORTANTE)
    object Search : Screen("search")

    object Calendar : Screen("calendar")

    object Perfil : Screen("perfil")

    object EditarPerfil : Screen("editarperfil")

}