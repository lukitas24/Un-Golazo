package com.example.futbolnomade.presentation.navigation

import android.security.keystore.KeyPermanentlyInvalidatedException
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.futbolnomade.domain.model.puedeValorarse
import com.example.futbolnomade.domain.model.NotificationEventType
import com.example.futbolnomade.presentation.security.BiometricAuthManager
import com.example.futbolnomade.presentation.security.BiometricAvailability
import com.example.futbolnomade.presentation.ui.AcercaScreen
import com.example.futbolnomade.presentation.ui.CalendarScreen
import com.example.futbolnomade.presentation.ui.CercaDeMiScreen
import com.example.futbolnomade.presentation.ui.ElementosScreen
import com.example.futbolnomade.presentation.ui.HomeScreen
import com.example.futbolnomade.presentation.ui.LoginScreen
import com.example.futbolnomade.presentation.ui.RegisterScreen
import com.example.futbolnomade.presentation.ui.SearchScreen
import com.example.futbolnomade.presentation.ui.canchas.AdminCanchaScreen
import com.example.futbolnomade.presentation.ui.canchas.CanchasScreen
import com.example.futbolnomade.presentation.ui.canchas.CrearCanchaScreen
import com.example.futbolnomade.presentation.ui.canchas.DetalleCanchaScreen
import com.example.futbolnomade.presentation.ui.canchas.MisCanchasScreen
import com.example.futbolnomade.presentation.ui.components.AppBottomBar
import com.example.futbolnomade.presentation.ui.partidos.CrearPartidoScreen
import com.example.futbolnomade.presentation.ui.partidos.DetallePartidoScreen
import com.example.futbolnomade.presentation.ui.partidos.MisPartidosScreen
import com.example.futbolnomade.presentation.ui.partidos.PartidosScreen
import com.example.futbolnomade.presentation.ui.partidos.ValorarPartidoScreen
import com.example.futbolnomade.presentation.ui.perfil.EditarPerfilScreen
import com.example.futbolnomade.presentation.ui.perfil.PerfilScreen
import com.example.futbolnomade.presentation.viewModel.AuthResult
import com.example.futbolnomade.presentation.viewModel.AuthViewModel
import com.example.futbolnomade.presentation.viewModel.BiometricLoginViewModel
import com.example.futbolnomade.presentation.viewModel.CanchaViewModel
import com.example.futbolnomade.presentation.viewModel.HomeViewModel
import com.example.futbolnomade.presentation.viewModel.PartidoViewModel
import com.example.futbolnomade.presentation.viewModel.PerfilViewModel
import com.example.futbolnomade.presentation.viewModel.ReservaViewModel
import com.example.futbolnomade.presentation.viewModel.ValoracionViewModel
import com.example.futbolnomade.presentation.notification.NotificationPermissionRequester
import com.example.futbolnomade.presentation.notification.NotificationTarget
import com.example.futbolnomade.presentation.notification.PartidoReminderScheduler
import com.example.futbolnomade.presentation.notification.ValoracionReminderScheduler
import com.example.futbolnomade.presentation.notification.ValoracionReminderWorker


private data class PendingBiometricEnrollment(
    val nombre: String,
    val email: String,
    val uid: String,
    val password: String
)

private val rutasSinBottomBar = setOf(
    Screen.Login.route,
    Screen.Register.route
)

@Composable
fun AppNavigation(
    notificationTarget: NotificationTarget? = null,
    onNotificationHandled: () -> Unit = {}
) {

    val navController  = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute   = backStackEntry?.destination?.route

    val authViewModel: AuthViewModel = viewModel()
    val biometricLoginViewModel: BiometricLoginViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel()
    val partidoViewModel: PartidoViewModel = viewModel()
    val canchaViewModel: CanchaViewModel = viewModel()
    val perfilViewModel: PerfilViewModel = viewModel()
    val reservaViewModel: ReservaViewModel = viewModel()
    val valoracionViewModel: ValoracionViewModel = viewModel()

    val context = LocalContext.current

    NotificationPermissionRequester(
        enabled =
            authViewModel.accesoDesbloqueado
    )

    val activity =
        LocalActivity.current as? FragmentActivity

    val biometricAuthManager = remember(activity) {
        activity?.let { fragmentActivity ->
            BiometricAuthManager(fragmentActivity)
        }
    }

    var biometricError by remember {
        mutableStateOf<String?>(null)
    }

    fun notificationRoute(
        target: NotificationTarget?
    ): String? {
        val partidoId =
            target?.partidoId

        /*
         * La notificación "Ya podés valorar" no abre el detalle:
         * abre directamente la pantalla de valoración.
         */
        if (
            target?.tipo ==
            ValoracionReminderWorker.TIPO_VALORAR_PARTIDO &&
            !partidoId.isNullOrBlank()
        ) {
            return Screen.ValorarPartido
                .createRoute(partidoId)
        }

        /*
         * Una valoración recibida pertenece al perfil del usuario.
         * Aunque el evento también tenga partidoId, priorizamos Perfil
         * antes del routing genérico a DetallePartido.
         */
        if (
            target?.tipo ==
            NotificationEventType.VALORACION_RECIBIDA
        ) {
            return Screen.Perfil.route
        }

        /*
         * Solicitud nueva -> abre directamente la administración
         * de la cancha para aceptar/rechazar.
         */
        if (
            target?.tipo ==
            NotificationEventType.NUEVA_SOLICITUD_RESERVA
        ) {
            val canchaId =
                target.canchaId

            if (!canchaId.isNullOrBlank()) {
                return Screen.AdminCancha
                    .createRoute(canchaId)
            }
        }

        /*
         * El partido ya fue eliminado, por lo que no tiene sentido
         * abrir DetallePartido con un ID que ya no existe.
         */
        if (
            target?.tipo ==
            NotificationEventType.PARTIDO_CANCELADO
        ) {
            return Screen.MisPartidos.route
        }

        if (!partidoId.isNullOrBlank()) {
            return Screen.DetallePartido
                .createRoute(partidoId)
        }

        val canchaId =
            target?.canchaId

        if (!canchaId.isNullOrBlank()) {
            return Screen.DetalleCancha
                .createRoute(canchaId)
        }

        return null
    }

    fun navigateToHome(
        nombre: String,
        email: String,
        uid: String
    ) {
        perfilViewModel.inicializar(
            nombre,
            email,
            uid
        )

        val destination =
            notificationRoute(
                notificationTarget
            ) ?: Screen.Home.route

        navController.navigate(
            destination
        ) {
            popUpTo(Screen.Login.route) {
                inclusive = true
            }

            launchSingleTop = true
        }

        if (notificationTarget != null) {
            onNotificationHandled()
        }
    }


    fun showShortMessage(message: String) {
        activity?.let { currentActivity ->
            Toast.makeText(
                currentActivity,
                message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    LaunchedEffect(perfilViewModel.email) {
        if (perfilViewModel.email.isNotBlank()) {
            valoracionViewModel
                .cargarValoracionesUsuario(
                    perfilViewModel.email
                )
        }
    }

    /*
     * Mantiene programado el aviso local de 2 horas para todos
     * los partidos en los que participa el usuario.
     *
     * También reprograma automáticamente si cambia la lista,
     * la fecha o el estado del partido mientras la app está abierta.
     */
    LaunchedEffect(
        partidoViewModel.uiState.partidos,
        perfilViewModel.email
    ) {
        PartidoReminderScheduler
            .sincronizarRecordatorios(
                context = context,
                partidos =
                    partidoViewModel
                        .uiState
                        .partidos,
                usuarioEmail =
                    perfilViewModel.email
            )
    }

    /*
     * La valoración ya se habilita 2 horas después de fechaHoraInicio.
     * Programamos la notificación exactamente para ese mismo momento.
     */
    LaunchedEffect(
        partidoViewModel.uiState.partidos,
        valoracionViewModel.valoracionesUsuario,
        perfilViewModel.email
    ) {
        val partidosYaValorados =
            valoracionViewModel
                .valoracionesUsuario
                .map {
                    it.partidoId
                }
                .toSet()

        ValoracionReminderScheduler
            .sincronizarRecordatorios(
                context =
                    context,

                partidos =
                    partidoViewModel
                        .uiState
                        .partidos,

                usuarioEmail =
                    perfilViewModel.email,

                partidosYaValorados =
                    partidosYaValorados
            )
    }

    LaunchedEffect(
        notificationTarget
    ) {
        val target =
            notificationTarget
                ?: return@LaunchedEffect

        val usuarioEstaDentro =
            perfilViewModel.email.isNotBlank() &&
                    currentRoute != Screen.Login.route &&
                    currentRoute != Screen.Register.route

        if (!usuarioEstaDentro) {
            return@LaunchedEffect
        }

        notificationRoute(target)?.let {
                destination ->

            navController.navigate(
                destination
            ) {
                launchSingleTop = true
            }
        }

        onNotificationHandled()
    }

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
                var pendingEnrollment by remember {
                    mutableStateOf<PendingBiometricEnrollment?>(null)
                }

                val linkedAccount =
                    biometricLoginViewModel.linkedAccount

                val biometricAvailable =
                    linkedAccount != null &&
                            biometricAuthManager?.getAvailability() ==
                            BiometricAvailability.Available

                LoginScreen(
                    authViewModel = authViewModel,

                    biometricAvailable =
                        biometricAvailable,

                    biometricEmail =
                        linkedAccount?.email,

                    biometricError =
                        biometricError,

                    onBiometricClick = biometricClick@{
                        biometricError = null

                        val manager =
                            biometricAuthManager

                        val account =
                            biometricLoginViewModel.linkedAccount

                        if (
                            manager == null ||
                            account == null
                        ) {
                            biometricError =
                                "La biometría no está disponible."
                            return@biometricClick
                        }

                        val decryptionCipher =
                            try {
                                biometricLoginViewModel
                                    .createDecryptionCipher()
                            } catch (
                                exception:
                                KeyPermanentlyInvalidatedException
                            ) {
                                biometricLoginViewModel
                                    .unlinkAccount()

                                biometricError =
                                    "Las biometrías del teléfono cambiaron. La cuenta fue desvinculada por seguridad."

                                return@biometricClick
                            } catch (exception: Exception) {
                                biometricLoginViewModel
                                    .unlinkAccount()

                                biometricError =
                                    "La credencial biométrica ya no es válida. Ingresá con email y contraseña."

                                return@biometricClick
                            }

                        manager.authenticateForLogin(
                            cipher = decryptionCipher,
                            accountEmail = account.email,

                            onSuccess = biometricSuccess@{
                                    authenticatedCipher ->

                                val credentials =
                                    try {
                                        biometricLoginViewModel
                                            .decryptCredentials(
                                                authenticatedCipher
                                            )
                                    } catch (
                                        exception: Exception
                                    ) {
                                        biometricLoginViewModel
                                            .unlinkAccount()

                                        biometricError =
                                            "No se pudo recuperar la cuenta vinculada. Ingresá con email y contraseña."

                                        return@biometricSuccess
                                    }

                                authViewModel
                                    .loginWithBiometricCredentials(
                                        credentials
                                    ) { result ->
                                        when (result) {
                                            is AuthResult.Success -> {
                                                val usuario =
                                                    authViewModel
                                                        .usuarioActual

                                                if (usuario == null) {
                                                    biometricError =
                                                        "No se pudo cargar el usuario."
                                                } else {
                                                    navigateToHome(
                                                        usuario.nombre
                                                            .ifBlank {
                                                                usuario.email
                                                            },
                                                        usuario.email,
                                                        usuario.uid
                                                    )
                                                }
                                            }

                                            is AuthResult.Error -> {
                                                /*
                                                 * Firebase rechazó las credenciales
                                                 * cifradas. Dejamos de reutilizarlas
                                                 * para evitar que el mismo error se
                                                 * repita en cada intento biométrico.
                                                 */
                                                biometricLoginViewModel
                                                    .unlinkAccount()

                                                biometricError =
                                                    "La credencial biométrica dejó de ser válida. " +
                                                            "Ingresá con email y contraseña para vincularla nuevamente."
                                            }
                                        }
                                    }
                            },

                            onCancelled = {
                                // Permanece en Login.
                            },

                            onError = { message ->
                                biometricError = message
                            }
                        )
                    },

                    onLoginSuccess = {
                            nombre,
                            email,
                            uid,
                            password ->

                        biometricError = null

                        val shouldOfferEnrollment =
                            !biometricLoginViewModel
                                .hasLinkedAccount &&
                                    biometricAuthManager
                                        ?.getAvailability() ==
                                    BiometricAvailability.Available

                        if (shouldOfferEnrollment) {
                            pendingEnrollment =
                                PendingBiometricEnrollment(
                                    nombre = nombre,
                                    email = email,
                                    uid = uid,
                                    password = password
                                )
                        } else {
                            navigateToHome(
                                nombre,
                                email,
                                uid
                            )
                        }
                    },

                    onSignUpClick = {
                        navController.navigate(
                            Screen.Register.route
                        )
                    }
                )

                pendingEnrollment?.let { pending ->
                    AlertDialog(
                        onDismissRequest = {
                            pendingEnrollment = null

                            navigateToHome(
                                pending.nombre,
                                pending.email,
                                pending.uid
                            )
                        },

                        title = {
                            Text(
                                "Activar ingreso biométrico"
                            )
                        },

                        text = {
                            Text(
                                "¿Querés usar la biometría del teléfono para ingresar a ${pending.email}? Solo esta cuenta quedará vinculada en este dispositivo."
                            )
                        },

                        confirmButton = {
                            TextButton(
                                onClick = enrollmentClick@{
                                    pendingEnrollment = null

                                    val manager =
                                        biometricAuthManager

                                    if (manager == null) {
                                        navigateToHome(
                                            pending.nombre,
                                            pending.email,
                                            pending.uid
                                        )
                                        return@enrollmentClick
                                    }

                                    val encryptionCipher =
                                        try {
                                            biometricLoginViewModel
                                                .createEncryptionCipher()
                                        } catch (
                                            exception: Exception
                                        ) {
                                            showShortMessage(
                                                "No se pudo preparar el almacenamiento biométrico."
                                            )

                                            navigateToHome(
                                                pending.nombre,
                                                pending.email,
                                                pending.uid
                                            )
                                            return@enrollmentClick
                                        }

                                    manager
                                        .authenticateForEncryption(
                                            cipher =
                                                encryptionCipher,
                                            accountEmail =
                                                pending.email,

                                            onSuccess = {
                                                    authenticatedCipher ->

                                                try {
                                                    biometricLoginViewModel
                                                        .saveLinkedCredentials(
                                                            uid =
                                                                pending.uid,
                                                            email =
                                                                pending.email,
                                                            password =
                                                                pending.password,
                                                            authenticatedCipher =
                                                                authenticatedCipher
                                                        )

                                                    showShortMessage(
                                                        "Ingreso biométrico activado."
                                                    )
                                                } catch (
                                                    exception: Exception
                                                ) {
                                                    biometricLoginViewModel
                                                        .unlinkAccount()

                                                    showShortMessage(
                                                        "No se pudo guardar la credencial biométrica."
                                                    )
                                                }

                                                navigateToHome(
                                                    pending.nombre,
                                                    pending.email,
                                                    pending.uid
                                                )
                                            },

                                            onCancelled = {
                                                navigateToHome(
                                                    pending.nombre,
                                                    pending.email,
                                                    pending.uid
                                                )
                                            },

                                            onError = {
                                                    message ->

                                                showShortMessage(
                                                    message
                                                )

                                                navigateToHome(
                                                    pending.nombre,
                                                    pending.email,
                                                    pending.uid
                                                )
                                            }
                                        )
                                }
                            ) {
                                Text("Sí, activar")
                            }
                        },

                        dismissButton = {
                            TextButton(
                                onClick = {
                                    pendingEnrollment = null

                                    navigateToHome(
                                        pending.nombre,
                                        pending.email,
                                        pending.uid
                                    )
                                }
                            ) {
                                Text("Ahora no")
                            }
                        }
                    )
                }
            }

            // 📝 REGISTER
            composable(Screen.Register.route) {
                RegisterScreen(
                    authViewModel = authViewModel,
                    onRegistroExitoso = { nombre, email, uid ->
                        navigateToHome(
                            nombre,
                            email,
                            uid ?: ""
                        )
                    },
                    onVolver = { navController.popBackStack() }
                )
            }

            // 🏠 HOME
            composable(Screen.Home.route) {
                HomeScreen(
                    nombreUsuario =
                        perfilViewModel.nombre,
                    emailUsuario =
                        perfilViewModel.email,
                    homeViewModel =
                        homeViewModel,
                    onIrAPartidos = {
                        navController.navigate(
                            Screen.Partidos.route
                        )
                    },
                    onIrACanchas = {
                        navController.navigate(
                            Screen.Canchas.route
                        )
                    },
                    onIrAMisPartidos = {
                        navController.navigate(
                            Screen.MisPartidos.route
                        )
                    },
                    onIrAMisCanchas = {
                        navController.navigate(
                            Screen.MisCanchas.route
                        )
                    },
                    onIrACercaMio = {
                        navController.navigate(
                            Screen.CercaDeMi.route
                        )
                    },
                    onBuscarPartido = { query ->
                        navController.navigate(
                            Screen.Search.createRoute(
                                query
                            )
                        )
                    },
                    onVerDetallePartido = { id ->
                        navController.navigate(
                            Screen.DetallePartido
                                .createRoute(id)
                        )
                    },
                    onIrAElementos = {
                        navController.navigate(
                            Screen.Elementos.route
                        )
                    },
                    onIrAAcerca = {
                        navController.navigate(
                            Screen.Acerca.route
                        )
                    }
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
                    onVerDetalle   = { id -> navController.navigate(Screen.DetallePartido.createRoute(id)) },
                    onVolver       = { navController.popBackStack() }
                )
            }
            composable(Screen.CrearPartido.route) {
                LaunchedEffect(Unit) {
                    canchaViewModel.cargarTodasLasCanchas()
                }
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
                            context = context,
                            titulo = titulo,
                            horario = horario,
                            fecha = fecha,
                            ubicacion = ubicacion,
                            dificultad = dificultad,
                            participantes = participantes,
                            descripcion = descripcion,
                            creador = perfilViewModel.email,
                            creadorUid = authViewModel.usuarioActual?.uid.orEmpty(),
                            canchaId = canchaId,
                            nombreCancha = nombreCancha,
                            latitud = latitud,
                            longitud = longitud,
                            propietarioCancha = canchaViewModel.uiState.canchas.find { it.id == canchaId }?.propietario,
                            propietarioCanchaUid = canchaViewModel.uiState.canchas.find { it.id == canchaId }?.propietarioUid
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
                arguments = listOf(
                    navArgument("partidoId") {
                        type = NavType.StringType
                    }
                ),
                deepLinks = listOf(
                    navDeepLink {
                        uriPattern = "futbolnomade://partido/{partidoId}"
                    }
                )
            ) { backStackEntry ->

                val partidoId =
                    backStackEntry.arguments?.getString("partidoId")
                        ?: return@composable

                /*
                 * Esto es importante cuando la app se abre directamente
                 * desde el enlace y todavía no cargó la lista de partidos.
                 */
                LaunchedEffect(partidoId) {
                    partidoViewModel.cargarPartidos()
                }

                val partido = partidoViewModel.uiState.partidos.find {
                    it.id == partidoId
                }

                val puedeValorar = partido?.puedeValorarse() ?: false
                val yaValoro = partido?.let { partidoEncontrado ->
                    valoracionViewModel.yaValoro(
                        partidoId = partidoEncontrado.id,
                        emailUsuario = perfilViewModel.email
                    )
                } ?: false

                DetallePartidoScreen(
                    partido = partido,
                    usuarioActual = perfilViewModel.email,

                    onAnotarse = { id, usuario ->
                        partidoViewModel.anotarseAPartido(
                            context = context,
                            partidoId = id,
                            usuario = usuario,
                            usuarioUid = authViewModel.usuarioActual?.uid.orEmpty()
                        )
                    },

                    onCancelarInscripcion = { id, usuario ->
                        partidoViewModel.cancelarInscripcion(
                            context = context,
                            partidoId = id,
                            usuario = usuario,
                            usuarioUid = authViewModel.usuarioActual?.uid.orEmpty()
                        )
                    },

                    onEliminarJugador = { id, jugador ->
                        partidoViewModel.eliminarJugador(
                            context = context,
                            partidoId = id,
                            jugadorAEliminar = jugador,
                            usuarioSolicitante =
                                perfilViewModel.email
                        )
                    },

                    onEliminarPartido = { id ->
                        partidoViewModel.eliminarPartido(id)
                        navController.popBackStack()
                    },

                    puedeValorar = puedeValorar,
                    yaValoro = yaValoro,

                    onValorarPartido = { id ->
                        navController.navigate(
                            Screen.ValorarPartido.createRoute(id)
                        )
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
                    valoracionViewModel = valoracionViewModel,

                    onCrearPartido = {
                        navController.navigate(
                            Screen.CrearPartido.route
                        )
                    },

                    onAdministrarPartido = { id ->
                        navController.navigate(
                            Screen.DetallePartido.createRoute(id)
                        )
                    },

                    onValorarPartido = { id ->
                        navController.navigate(
                            Screen.ValorarPartido.createRoute(id)
                        )
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
                arguments = listOf(
                    navArgument("canchaId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val canchaId = backStackEntry.arguments?.getString("canchaId") ?: ""
                val cancha   = canchaViewModel.uiState.canchas.find { it.id == canchaId }

                LaunchedEffect(canchaId) {
                    /*
                     * Al abrir esta pantalla desde una notificación, la lista de
                     * canchas puede no estar cargada todavía.
                     */
                    canchaViewModel.cargarTodasLasCanchas()
                    reservaViewModel.cargarReservasPorCancha(canchaId)
                }

                DetalleCanchaScreen(
                    cancha = cancha,
                    reservasConfirmadas = reservaViewModel.uiState.reservas.filter { it.canchaId == canchaId },
                    onReservarTurno = { idCancha, fecha, hora ->
                        val esDuenio =
                            cancha?.propietario?.equals(
                                perfilViewModel.email,
                                ignoreCase = true
                            ) == true

                        android.util.Log.d("RESERVA_NAV", "Usuario reservando: ${perfilViewModel.email}. Dueño cancha: ${cancha?.propietario}")
                        reservaViewModel.crearReserva(
                            context = context,
                            reserva = com.example.futbolnomade.domain.model.Reserva(
                                canchaId = idCancha,
                                canchaNombre = cancha?.nombre ?: "",
                                usuarioId = perfilViewModel.email,
                                usuarioUid = authViewModel.usuarioActual?.uid.orEmpty(),
                                usuarioEmail = perfilViewModel.email,
                                usuarioNombre = perfilViewModel.nombre,
                                propietarioCanchaUid = cancha?.propietarioUid.orEmpty(),
                                propietarioCanchaEmail = cancha?.propietario.orEmpty(),
                                fecha = fecha,
                                hora = hora,
                                estado = if (esDuenio) "Confirmada" else "Pendiente"
                            )
                        )
                        navController.popBackStack()
                    },
                    onVolver = {
                        navController.popBackStack()
                    }
                )
            }

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
                            propietarioUid = authViewModel.usuarioActual?.uid.orEmpty(),
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
                    canchaId          = canchaId,
                    canchaViewModel   = canchaViewModel,
                    reservaViewModel  = reservaViewModel,
                    partidoViewModel  = partidoViewModel,
                    onEliminarYVolver = {
                        navController.popBackStack()
                    },
                    onVolver          = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.Search.route,
                arguments = listOf(navArgument("query") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = ""
                })
            ) { backStackEntry ->
                val initialQuery = backStackEntry.arguments?.getString("query")?.decodeFromRoute() ?: ""

                LaunchedEffect(Unit) {
                    partidoViewModel.cargarPartidos()
                    canchaViewModel.cargarTodasLasCanchas()
                }

                SearchScreen(
                    initialQuery = initialQuery,
                    partidos = partidoViewModel.partidosVisibles(),
                    canchas = canchaViewModel.uiState.canchas,
                    onVerDetallePartido = { id -> navController.navigate(Screen.DetallePartido.createRoute(id)) },
                    onVerDetalleCancha = { id -> navController.navigate(Screen.DetalleCancha.createRoute(id)) },
                    onVolver = { navController.popBackStack() }
                )
            }
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
                val currentUid =
                    authViewModel.usuarioActual?.uid

                val linkedAccount =
                    biometricLoginViewModel.linkedAccount

                val linkedToCurrentAccount =
                    biometricLoginViewModel.isLinkedTo(
                        currentUid
                    )

                PerfilScreen(
                    nombre =
                        perfilViewModel.nombre,
                    email =
                        perfilViewModel.email,
                    imageUri =
                        perfilViewModel.imageUri
                            ?.toString(),

                    biometricLinkedToCurrentAccount =
                        linkedToCurrentAccount,

                    biometricLinkedToAnotherAccount =
                        linkedAccount != null &&
                                !linkedToCurrentAccount,

                    onEditarPerfil = {
                        navController.navigate(
                            Screen.EditarPerfil.route
                        )
                    },

                    onAcercaDe = {
                        navController.navigate(
                            Screen.Acerca.route
                        )
                    },

                    onTerminos = {},

                    onCalificar = {},

                    onUnlinkBiometric = {
                        biometricLoginViewModel
                            .unlinkAccount()

                        showShortMessage(
                            "Ingreso biométrico desvinculado."
                        )
                    },

                    onCerrarSesion = {
                        /*
                         * Este dispositivo ya no debe mostrar recordatorios
                         * pertenecientes a la cuenta que cerró sesión.
                         */
                        PartidoReminderScheduler
                            .cancelarTodos(
                                context
                            )

                        ValoracionReminderScheduler
                            .cancelarTodos(
                                context
                            )

                        authViewModel.logout {
                            perfilViewModel.limpiar()

                            navController.navigate(
                                Screen.Login.route
                            ) {
                                popUpTo(
                                    navController.graph.id
                                ) {
                                    inclusive = true
                                }

                                launchSingleTop = true
                            }
                        }
                    }
                )
            }

            composable(
                route = Screen.ValorarPartido.route,
                arguments = listOf(
                    navArgument("partidoId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->

                val partidoId =
                    backStackEntry.arguments
                        ?.getString("partidoId")
                        ?: return@composable

                LaunchedEffect(partidoId) {
                    partidoViewModel.cargarPartidos()
                }

                val partido =
                    partidoViewModel.uiState.partidos.find {
                        it.id == partidoId
                    }

                ValorarPartidoScreen(
                    partido = partido,
                    usuarioActual = perfilViewModel.email,
                    usuarioActualUid = perfilViewModel.uid,
                    guardando = valoracionViewModel.guardando,
                    error = valoracionViewModel.error,

                    onGuardar = { valoracion ->
                        valoracionViewModel.guardarValoracion(
                            valoracion = valoracion
                        ) { guardada ->
                            if (guardada) {
                                navController.popBackStack()
                            }
                        }
                    },

                    onVolver = {
                        valoracionViewModel.limpiarError()
                        navController.popBackStack()
                    }
                )
            }

            // ✏️ EDITAR PERFIL
            composable(Screen.EditarPerfil.route) {
                EditarPerfilScreen(
                    nombreActual = perfilViewModel.nombre,
                    emailActual  = perfilViewModel.email,
                    onGuardar = {
                            nombre,
                            email,
                            password,
                            uri ->

                        val linkedToCurrentAccount =
                            biometricLoginViewModel
                                .isLinkedTo(
                                    authViewModel
                                        .usuarioActual
                                        ?.uid
                                )

                        val credentialsChanged =
                            password.isNotBlank() ||
                                    !email.equals(
                                        perfilViewModel.email,
                                        ignoreCase = true
                                    )

                        if (
                            linkedToCurrentAccount &&
                            credentialsChanged
                        ) {
                            biometricLoginViewModel
                                .unlinkAccount()

                            showShortMessage(
                                "La biometría se desvinculó porque cambiaste el email o la contraseña."
                            )
                        }

                        perfilViewModel.actualizarPerfil(
                            nombre,
                            email,
                            perfilViewModel.uid,
                            uri
                        )

                        authViewModel
                            .actualizarUsuarioActual(
                                nombre,
                                email,
                                password
                            )

                        navController.popBackStack()
                    },
                    onVolver = { navController.popBackStack() }
                )
            }
        }
    }
}