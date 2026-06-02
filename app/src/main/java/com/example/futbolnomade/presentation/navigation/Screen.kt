package com.example.futbolnomade.presentation.navigation

sealed class Screen(val route: String) {

    object Login    : Screen("login")
    object Register : Screen("register")

    object Home : Screen("home/{nombreUsuario}") {
        fun createRoute(nombreUsuario: String) = "home/$nombreUsuario"
    }

    object Elementos : Screen("elementos")
    object Acerca    : Screen("acerca")
    object Search    : Screen("search")
    object Calendar  : Screen("calendar")
    object Perfil    : Screen("perfil")
    object EditarPerfil : Screen("editar_perfil")

    object Partidos     : Screen("partidos")
    object CrearPartido : Screen("crear_partido")

    object DetallePartido : Screen("detalle_partido/{partidoId}") {
        fun createRoute(partidoId: Int) = "detalle_partido/$partidoId"
    }

    object Canchas     : Screen("canchas")
    object CrearCancha : Screen("crear_cancha")
}

