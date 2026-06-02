package com.example.futbolnomade.presentation.navigation

sealed class Screen(val route: String) {

    object Login    : Screen("login")
    object Register : Screen("register")

    // nombre y email viajan como argumentos separados para mostrarlos correctamente en Home
    object Home : Screen("home/{nombreUsuario}/{emailUsuario}") {
        fun createRoute(nombre: String, email: String) =
            "home/${nombre.encodeForRoute()}/${email.encodeForRoute()}"
    }

    object Elementos    : Screen("elementos")
    object Acerca       : Screen("acerca")
    object Search       : Screen("search")
    object Calendar     : Screen("calendar")
    object Perfil       : Screen("perfil")
    object EditarPerfil : Screen("editar_perfil")

    object Partidos     : Screen("partidos")
    object CrearPartido : Screen("crear_partido")

    object DetallePartido : Screen("detalle_partido/{partidoId}") {
        fun createRoute(partidoId: Int) = "detalle_partido/$partidoId"
    }

    object Canchas     : Screen("canchas")
    object CrearCancha : Screen("crear_cancha")
}

// Codifica @ y . para que el email no rompa la ruta de navegación
private fun String.encodeForRoute() =
    this.replace("@", "%40").replace(".", "%2E")

fun String.decodeFromRoute() =
    this.replace("%40", "@").replace("%2E", ".")