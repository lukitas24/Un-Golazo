package com.example.futbolnomade.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.futbolnomade.presentation.ui.CalendarScreen
import com.example.futbolnomade.presentation.ui.ElementosScreen
import com.example.futbolnomade.presentation.ui.HomeScreen
import com.example.futbolnomade.presentation.ui.LoginScreen
import com.example.futbolnomade.presentation.ui.RegisterScreen
import com.example.futbolnomade.presentation.ui.canchas.AdminCanchaScreen
import com.example.futbolnomade.presentation.ui.canchas.CanchasScreen
import com.example.futbolnomade.presentation.ui.canchas.CrearCanchaScreen
import com.example.futbolnomade.presentation.ui.canchas.MisCanchasScreen
import com.example.futbolnomade.presentation.ui.canchas.DetalleCanchaScreen
import com.example.futbolnomade.presentation.ui.components.AppBottomBar
import com.example.futbolnomade.presentation.ui.partidos.CrearPartidoScreen
import com.example.futbolnomade.presentation.ui.partidos.DetallePartidoScreen
import com.example.futbolnomade.presentation.ui.partidos.PartidosScreen
import com.example.futbolnomade.presentation.ui.perfil.EditarPerfilScreen
import com.example.futbolnomade.presentation.ui.perfil.PerfilScreen
import com.example.futbolnomade.presentation.viewModel.AuthViewModel
import com.example.futbolnomade.presentation.viewModel.CanchaViewModel
import com.example.futbolnomade.presentation.viewModel.HomeViewModel
import com.example.futbolnomade.presentation.viewModel.PartidoViewModel
import com.example.futbolnomade.presentation.viewModel.PerfilViewModel
import com.example.futbolnomade.presentation.ui.CercaDeMiScreen
import com.example.futbolnomade.presentation.ui.partidos.MisPartidosScreen

private val rutasSinBottomBar = setOf(Screen.Login.route, Screen.Register.route)

@Composable
fun AppNavigation() {

    val navController  = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute   = backStackEntry?.destination?.route

    val authViewModel:    AuthViewModel    = viewModel()
    val homeViewModel:    HomeViewModel    = viewModel()
    val partidoViewModel: PartidoViewModel = viewModel()
    val canchaViewModel:  CanchaViewModel  = viewModel()
    val perfilViewModel:  PerfilViewModel  = viewModel()

    Scaffold(
        bottomBar = {
            if (currentRoute !in rutasSinBottomBar) {
                AppBottomBar(navController = navController, currentRoute = currentRoute)
            }
        }
    ) { padding ->

        NavHost(
            navController    = navController,
            startDestination = Screen.Login.route,
            modifier         = Modifier.padding(padding)
        ) {

            // 🔐 LOGIN
            composable(Screen.Login.route) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onLoginSuccess = { nombre, email ->
                        perfilViewModel.inicializar(nombre, email)
                        navController.navigate(Screen.Home.createRoute(nombre, email)) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onSignUpClick = { navController.navigate(Screen.Register.route) }
                )
            }

            // 📝 REGISTER
            composable(Screen.Register.route) {
                RegisterScreen(
                    authViewModel = authViewModel,
                    onRegistroExitoso = { nombre, email ->
                        perfilViewModel.inicializar(nombre, email)
                        navController.navigate(Screen.Home.createRoute(nombre, email)) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onVolver = { navController.popBackStack() }
                )
            }

            // 🏠 HOME
            composable(
                route = Screen.Home.route,
                arguments = listOf(
                    navArgument("nombreUsuario") { type = NavType.StringType },
                    navArgument("emailUsuario")  { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val nombre = backStackEntry.arguments?.getString("nombreUsuario")?.decodeFromRoute() ?: ""
                val email  = backStackEntry.arguments?.getString("emailUsuario")?.decodeFromRoute() ?: ""

                HomeScreen(
                    nombreUsuario       = nombre,
                    emailUsuario        = email,
                    homeViewModel       = homeViewModel,
                    onIrAPartidos       = { navController.navigate(Screen.Partidos.route) },
                    onIrACanchas        = { navController.navigate(Screen.Canchas.route) },
                    onIrAMisPartidos    = { navController.navigate(Screen.MisPartidos.route) },
                    onIrAMisCanchas     = { navController.navigate(Screen.MisCanchas.route) },
                    onIrAMisReservas    = { navController.navigate(Screen.Partidos.route) },
                    onIrACercaMio       = { navController.navigate(Screen.CercaDeMi.route) },
                    onBuscarPartido     = { navController.navigate(Screen.Search.route) },
                    onVerDetallePartido = { id -> navController.navigate(Screen.DetallePartido.createRoute(id)) },
                    onIrAElementos      = { navController.navigate(Screen.Elementos.route) },
                    onIrAAcerca         = { navController.navigate(Screen.Acerca.route) }
                )
            }

            composable(Screen.Elementos.route) {
                ElementosScreen(onVolver = { navController.popBackStack() })
            }
            composable(Screen.Acerca.route) {
                AcercaScreen(onVolver = { navController.popBackStack() })
            }

            // 🗺️ PANTALLA MAPA CERCA MÍO (Llama correctamente a CercaDeMiScreen e infiere tipos)
            composable(Screen.CercaDeMi.route) {
                CercaDeMiScreen(
                    canchas = canchaViewModel.uiState.canchas,
                    partidos = partidoViewModel.uiState.partidos,
                    onVerDetalleCancha = { id: String ->
                        navController.navigate(Screen.DetalleCancha.createRoute(id))
                    },
                    onVerDetallePartido = { id: String ->
                        navController.navigate(Screen.DetallePartido.createRoute(id))
                    },
                    onVolver = { navController.popBackStack() }
                )
            }

            // ⚽ PARTIDOS
            composable(Screen.Partidos.route) {
                LaunchedEffect(Unit) {
                    partidoViewModel.cargarPartidos()
                }
                PartidosScreen(
                    uiState        = partidoViewModel.uiState,
                    onCrearPartido = { navController.navigate(Screen.CrearPartido.route) },
                    onVerDetalle   = { id -> navController.navigate(Screen.DetallePartido.createRoute(id)) },
                    onVolver       = { navController.popBackStack() }
                )
            }
            composable(Screen.CrearPartido.route) {
                CrearPartidoScreen(
                    canchas = canchaViewModel.uiState.canchas,
                    onCrearPartido = {
                            titulo,
                            horario,
                            fecha,
                            ubicacion,
                            dificultad,
                            participantes,
                            descripcion,
                            canchaId,
                            nombreCancha,
                            latitud,
                            longitud ->

                        partidoViewModel.crearPartido(
                            titulo = titulo,
                            horario = horario,
                            fecha = fecha,
                            ubicacion = ubicacion,
                            dificultad = dificultad,
                            participantes = participantes,
                            descripcion = descripcion,
                            creador = perfilViewModel.email,
                            canchaId = canchaId,
                            nombreCancha = nombreCancha,
                            latitud = latitud,
                            longitud = longitud
                        )

                        navController.popBackStack()
                    },
                    onVolver = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.DetallePartido.route,
                arguments = listOf(navArgument("partidoId") { type = NavType.StringType })
            ) { backStackEntry ->
                val partidoId = backStackEntry.arguments?.getString("partidoId")
                val partido   = partidoViewModel.uiState.partidos.find { it.id == partidoId }
                DetallePartidoScreen(
                    partido = partido,
                    usuarioActual = perfilViewModel.email,

                    onAnotarse = { id, usuario ->
                        partidoViewModel.anotarseAPartido(
                            partidoId = id,
                            usuario = usuario
                        )
                    },

                    onCancelarInscripcion = { id, usuario ->
                        partidoViewModel.cancelarInscripcion(
                            partidoId = id,
                            usuario = usuario
                        )
                    },

                    onEliminarJugador = { partidoId, jugador ->
                        partidoViewModel.eliminarJugador(
                            partidoId = partidoId,
                            jugadorAEliminar = jugador,
                            usuarioSolicitante = perfilViewModel.email
                        )
                    },

                    onEliminarPartido = { id ->
                        partidoViewModel.eliminarPartido(id)
                        navController.popBackStack()
                    },

                    onVolver = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.MisPartidos.route) {
                MisPartidosScreen(
                    emailUsuario = perfilViewModel.email,
                    partidoViewModel = partidoViewModel,
                    onCrearPartido = {
                        navController.navigate(Screen.CrearPartido.route)
                    },
                    onAdministrarPartido = { id ->
                        navController.navigate(Screen.DetallePartido.createRoute(id))
                    },
                    onVolver = {
                        navController.popBackStack()
                    }
                )
            }

            // 🏟 CANCHAS (listado público)
            composable(Screen.Canchas.route) {
                LaunchedEffect(Unit) {
                    canchaViewModel.cargarTodasLasCanchas()
                }
                CanchasScreen(
                    canchas       = canchaViewModel.uiState.canchas,
                    onSubirCancha = { navController.navigate(Screen.CrearCancha.route) },
                    onVerDetalle  = { id -> navController.navigate(Screen.DetalleCancha.createRoute(id)) }
                )
            }

            // 🏟 DETALLE DE CANCHA PÚBLICA
            composable(
                route = Screen.DetalleCancha.route,
                arguments = listOf(navArgument("canchaId") { type = NavType.StringType })
            ) { backStackEntry ->
                val canchaId = backStackEntry.arguments?.getString("canchaId") ?: ""
                val cancha   = canchaViewModel.uiState.canchas.find { it.id == canchaId }

                DetalleCanchaScreen(
                    cancha = cancha,
                    turnosReservados = listOf("19:00", "21:00"),
                    onReservarTurno = { idCancha, hora ->
                        navController.popBackStack()
                    },
                    onVolver = { navController.popBackStack() }
                )
            }

            // ➕ CREAR CANCHA
            // ➕ CREAR CANCHA
            composable(Screen.CrearCancha.route) {
                CrearCanchaScreen(
                    onCrearCancha = {
                            nombre,
                            ubicacion,
                            descripcion,
                            precio,
                            telefono,
                            apertura,
                            cierre,
                            horariosDetallados,
                            latitud,
                            longitud ->

                        canchaViewModel.crearCancha(
                            nombre = nombre,
                            ubicacion = ubicacion,
                            descripcion = descripcion,
                            precio = precio,
                            telefono = telefono,
                            horarioApertura = apertura,
                            horarioCierre = cierre,
                            horariosDetallados = horariosDetallados,
                            propietario = perfilViewModel.email,
                            latitud = latitud,
                            longitud = longitud
                        )

                        navController.popBackStack()
                    },
                    onVolver = {
                        navController.popBackStack()
                    }
                )
            }

            // 🏠 MIS CANCHAS
            composable(Screen.MisCanchas.route) {
                LaunchedEffect(perfilViewModel.email) {
                    canchaViewModel.cargarCanchas(perfilViewModel.email)
                }
                MisCanchasScreen(
                    emailUsuario        = perfilViewModel.email,
                    canchaViewModel     = canchaViewModel,
                    onCrearCancha       = { navController.navigate(Screen.CrearCancha.route) },
                    onAdministrarCancha = { id -> navController.navigate(Screen.AdminCancha.createRoute(id)) },
                    onVolver            = { navController.popBackStack() }
                )
            }

            // ⚙️ ADMINISTRAR UNA CANCHA
            composable(
                route     = Screen.AdminCancha.route,
                arguments = listOf(navArgument("canchaId") { type = NavType.StringType })
            ) { backStackEntry ->
                val canchaId = backStackEntry.arguments?.getString("canchaId") ?: return@composable

                AdminCanchaScreen(
                    canchaId         = canchaId,
                    canchaViewModel  = canchaViewModel,
                    onEliminarYVolver = {
                        navController.popBackStack()
                    },
                    onVolver         = { navController.popBackStack() }
                )
            }

            composable(Screen.Search.route)   { /* TODO: SearchScreen() */ }
            composable(Screen.Calendar.route) {
                CalendarScreen(
                    emailUsuario = perfilViewModel.email,
                    partidoViewModel = partidoViewModel,
                    onVerDetallePartido = { id -> navController.navigate(Screen.DetallePartido.createRoute(id)) },
                    onVolver = { navController.popBackStack() }
                )
            }

            // 👤 PERFIL
            composable(Screen.Perfil.route) {
                PerfilScreen(
                    nombre   = perfilViewModel.nombre,
                    email    = perfilViewModel.email,
                    imageUri = perfilViewModel.imageUri?.toString(),
                    onEditarPerfil = { navController.navigate(Screen.EditarPerfil.route) },
                    onAcercaDe     = { navController.navigate(Screen.Acerca.route) },
                    onTerminos     = {},
                    onCalificar    = {},
                    onCerrarSesion = {
                        authViewModel.logout()
                        perfilViewModel.limpiar()
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
                    emailActual  = perfilViewModel.email,
                    onGuardar = { nombre, email, password, uri ->
                        perfilViewModel.actualizarPerfil(nombre, email, uri)
                        authViewModel.actualizarUsuarioActual(nombre, email, password)
                        navController.popBackStack()
                    },
                    onVolver = { navController.popBackStack() }
                )
            }
        }
    }
}