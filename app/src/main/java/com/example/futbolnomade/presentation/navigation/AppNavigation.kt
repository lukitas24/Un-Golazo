package com.example.futbolnomade.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.futbolnomade.presentation.ui.AcercaScreen
import com.example.futbolnomade.presentation.ui.ElementosScreen
import com.example.futbolnomade.presentation.ui.HomeScreen
import com.example.futbolnomade.presentation.ui.LoginScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.futbolnomade.presentation.ui.partidos.PartidosScreen
import com.example.futbolnomade.presentation.viewModel.PartidoViewModel
import com.example.futbolnomade.presentation.ui.partidos.CrearPartidoScreen
import com.example.futbolnomade.presentation.ui.partidos.DetallePartidoScreen
import com.example.futbolnomade.presentation.ui.canchas.CanchasScreen
import com.example.futbolnomade.presentation.ui.canchas.CrearCanchaScreen
import com.example.futbolnomade.presentation.viewModel.CanchaViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val partidoViewModel: PartidoViewModel = viewModel()
    val canchaViewModel: CanchaViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { nombreUsuario ->
                    navController.navigate(
                        Screen.Home.createRoute(nombreUsuario)
                    )
                }
            )
        }

        composable(Screen.Home.route) { backStackEntry ->
            val nombreUsuario = backStackEntry.arguments
                ?.getString("nombreUsuario")
                ?: ""

            HomeScreen(
                nombreUsuario = nombreUsuario,
                onIrAElementos = {
                    navController.navigate(Screen.Elementos.route)
                },
                onIrAAcerca = {
                    navController.navigate(Screen.Acerca.route)
                },
                onIrAPartidos = {
                    navController.navigate(Screen.Partidos.route)
                },
                onIrACanchas = {
                    navController.navigate(Screen.Canchas.route)
                },
            )
        }

        composable(Screen.Elementos.route) {
            ElementosScreen(
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Acerca.route) {
            AcercaScreen(
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Partidos.route) {
            PartidosScreen(
                uiState = partidoViewModel.uiState,
                onCrearPartido = {
                    navController.navigate(Screen.CrearPartido.route)
                },
                onVerDetalle = { partidoId ->
                    navController.navigate(Screen.DetallePartido.createRoute(partidoId))
                },
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.CrearPartido.route) {
            CrearPartidoScreen(
                onCrearPartido = { titulo, horario, fecha, ubicacion, dificultad, participantes, descripcion ->
                    partidoViewModel.crearPartido(
                        titulo = titulo,
                        horario = horario,
                        fecha = fecha,
                        ubicacion = ubicacion,
                        dificultad = dificultad,
                        participantes = participantes,
                        descripcion = descripcion
                    )
                    navController.popBackStack()
                },
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.DetallePartido.route) { backStackEntry ->
            val partidoId = backStackEntry.arguments
                ?.getString("partidoId")
                ?.toIntOrNull()

            val partido = partidoViewModel.uiState.partidos.find {
                it.id == partidoId
            }

            DetallePartidoScreen(
                partido = partido,
                usuarioActual = "admin",
                onAnotarse = { partidoId, usuario ->
                    partidoViewModel.anotarseAPartido(partidoId, usuario)
                },
                onCancelarInscripcion = { partidoId, usuario ->
                    partidoViewModel.cancelarInscripcion(partidoId, usuario)
                },
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Canchas.route) {
            CanchasScreen(
                canchas = canchaViewModel.uiState.canchas,
                onSubirCancha = {
                    navController.navigate(Screen.CrearCancha.route)
                },
                onVerDetalle = { canchaId ->
                    // navController.navigate(Screen.DetalleCancha.createRoute(canchaId))
                }
            )
        }

        composable(Screen.CrearCancha.route) {
            CrearCanchaScreen(
                onCrearCancha = { nombre, ubicacion, descripcion, precio, telefono, horarioApertura, horarioCierre ->
                    canchaViewModel.crearCancha(
                        nombre = nombre,
                        ubicacion = ubicacion,
                        descripcion = descripcion,
                        precio = precio,
                        telefono = telefono,
                        horarioApertura = horarioApertura,
                        horarioCierre = horarioCierre
                    )
                    navController.popBackStack()
                },
                onVolver = {
                    navController.popBackStack()
                }
            )
        }
    }
}