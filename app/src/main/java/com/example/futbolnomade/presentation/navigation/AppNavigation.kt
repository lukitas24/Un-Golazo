package com.example.futbolnomade.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.futbolnomade.presentation.ui.AcercaScreen
import com.example.futbolnomade.presentation.ui.ElementosScreen
import com.example.futbolnomade.presentation.ui.HomeScreen
import com.example.futbolnomade.presentation.ui.LoginScreen
import com.example.futbolnomade.presentation.ui.partidos.PartidosScreen
import com.example.futbolnomade.presentation.ui.partidos.CrearPartidoScreen
import com.example.futbolnomade.presentation.ui.partidos.DetallePartidoScreen
import com.example.futbolnomade.presentation.ui.perfil.PerfilScreen
import com.example.futbolnomade.presentation.ui.perfil.EditarPerfilScreen
import com.example.futbolnomade.presentation.ui.canchas.CanchasScreen
import com.example.futbolnomade.presentation.ui.canchas.CrearCanchaScreen
import com.example.futbolnomade.presentation.viewModel.PartidoViewModel
import com.example.futbolnomade.presentation.viewModel.CanchaViewModel
import com.example.futbolnomade.presentation.ui.components.AppBottomBar
import com.example.futbolnomade.presentation.viewModel.PerfilViewModel


@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val partidoViewModel: PartidoViewModel = viewModel()
    val canchaViewModel: CanchaViewModel = viewModel()
    val perfilViewModel: PerfilViewModel = viewModel()

    Scaffold(
        bottomBar = {
            // ❗ Opcional: ocultar bottom bar en login
            if (currentRoute != Screen.Login.route) {
                AppBottomBar(
                    navController = navController,
                    currentRoute = currentRoute
                )
            }
        }
    ) { padding ->

        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(padding)
        ) {

            // 🔐 LOGIN
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = { nombreUsuario ->
                        navController.navigate(
                            Screen.Home.createRoute(nombreUsuario)
                        ) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            // 🏠 HOME (con argumento)
            composable(
                route = Screen.Home.route,
                arguments = listOf(
                    navArgument("nombreUsuario") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->

                val nombreUsuario =
                    backStackEntry.arguments?.getString("nombreUsuario") ?: ""

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
                    }
                )
            }

            // 📌 ELEMENTOS
            composable(Screen.Elementos.route) {
                ElementosScreen(
                    onVolver = { navController.popBackStack() }
                )
            }

            // ℹ️ ACERCA
            composable(Screen.Acerca.route) {
                AcercaScreen(
                    onVolver = { navController.popBackStack() }
                )
            }

            // ⚽ PARTIDOS
            composable(Screen.Partidos.route) {
                PartidosScreen(
                    uiState = partidoViewModel.uiState,
                    onCrearPartido = {
                        navController.navigate(Screen.CrearPartido.route)
                    },
                    onVerDetalle = { partidoId ->
                        navController.navigate(
                            Screen.DetallePartido.createRoute(partidoId)
                        )
                    },
                    onVolver = { navController.popBackStack() }
                )
            }

            // ➕ CREAR PARTIDO
            composable(Screen.CrearPartido.route) {
                CrearPartidoScreen(
                    onCrearPartido = { titulo, horario, fecha, ubicacion, dificultad, participantes, descripcion ->
                        partidoViewModel.crearPartido(
                            titulo, horario, fecha,
                            ubicacion, dificultad,
                            participantes, descripcion
                        )
                        navController.popBackStack()
                    },
                    onVolver = { navController.popBackStack() }
                )
            }

            // 📄 DETALLE PARTIDO
            composable(
                route = Screen.DetallePartido.route,
                arguments = listOf(
                    navArgument("partidoId") {
                        type = NavType.IntType
                    }
                )
            ) { backStackEntry ->

                val partidoId =
                    backStackEntry.arguments?.getString("partidoId")?.toIntOrNull()

                val partido = partidoViewModel.uiState.partidos.find {
                    it.id == partidoId
                }

                DetallePartidoScreen(
                    partido = partido,
                    usuarioActual = "admin",
                    onAnotarse = { id, usuario ->
                        partidoViewModel.anotarseAPartido(id, usuario)
                    },
                    onCancelarInscripcion = { id, usuario ->
                        partidoViewModel.cancelarInscripcion(id, usuario)
                    },
                    onVolver = { navController.popBackStack() }
                )
            }

            // 🏟 CANCHAS
            composable(Screen.Canchas.route) {
                CanchasScreen(
                    canchas = canchaViewModel.uiState.canchas,
                    onSubirCancha = {
                        navController.navigate(Screen.CrearCancha.route)
                    },
                    onVerDetalle = { /* futuro */ }
                )
            }

            // ➕ CREAR CANCHA
            composable(Screen.CrearCancha.route) {
                CrearCanchaScreen(
                    onCrearCancha = { nombre, ubicacion, descripcion, precio, telefono, horarioApertura, horarioCierre ->
                        canchaViewModel.crearCancha(
                            nombre,
                            ubicacion,
                            descripcion,
                            precio,
                            telefono,
                            horarioApertura,
                            horarioCierre
                        )
                        navController.popBackStack()
                    },
                    onVolver = { navController.popBackStack() }
                )
            }

            // 🔍 SEARCH (BottomBar)
            composable(Screen.Search.route) {
                // TODO: SearchScreen()
            }

            // 📅 CALENDAR (BottomBar)
            composable(Screen.Calendar.route) {
                // TODO: CalendarScreen()
            }

            // 👤 PROFILE (BottomBar)
            composable(Screen.Perfil.route) {
                PerfilScreen(
                    nombre = perfilViewModel.nombre,
                    email = perfilViewModel.email,
                    imageUri = perfilViewModel.imageUri?.toString(), // 👈 CLAVE
                    onEditarPerfil = {
                        navController.navigate(Screen.EditarPerfil.route)
                    },
                    onAcercaDe = {
                        navController.navigate(Screen.Acerca.route)
                    },
                    onTerminos = {},
                    onCalificar = {},
                    onCerrarSesion = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            // ✏️ EDITAR PERFIL
            composable(Screen.EditarPerfil.route) {

                EditarPerfilScreen(
                    nombreActual = perfilViewModel.nombre,
                    emailActual = perfilViewModel.email,

                    onGuardar = { nombre, email, password, uri ->

                        perfilViewModel.actualizarPerfil(nombre, email, uri)

                        navController.popBackStack()
                    },

                    onVolver = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}