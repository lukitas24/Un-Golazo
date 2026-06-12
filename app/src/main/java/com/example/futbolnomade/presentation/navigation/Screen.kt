package com.example.futbolnomade.presentation.navigation

sealed class Screen(val route: String) {

    object Login    : Screen("login")
    object Register : Screen("register")

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

    object MisPartidos : Screen("mis_partidos")
    object DetallePartido : Screen("detalle_partido/{partidoId}") {
        fun createRoute(partidoId: String) = "detalle_partido/$partidoId"
    }

    object Canchas     : Screen("canchas")
    object CrearCancha : Screen("crear_cancha")

    // ── Nuevas rutas de Mis Canchas ───────────────────────────────────────
    object MisCanchas   : Screen("mis_canchas")

    object AdminCancha  : Screen("admin_cancha/{canchaId}") {
        fun createRoute(canchaId: String) = "admin_cancha/$canchaId"
    }
    object DetalleCancha : Screen("detalle_cancha/{canchaId}") {
        fun createRoute(canchaId: Int) = "detalle_cancha/$canchaId"
    }

    object CercaDeMi : Screen("cerca_de_mi")
}

private fun String.encodeForRoute() =
    this.replace("@", "%40").replace(".", "%2E")

fun String.decodeFromRoute() =
    this.replace("%40", "@").replace("%2E", ".")