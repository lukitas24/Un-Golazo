package com.example.futbolnomade.presentation.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futbolnomade.data.repository.JugadorRepositoryImpl
import com.example.futbolnomade.data.repository.NotificationRepositoryImpl
import com.example.futbolnomade.data.security.BiometricCredentials
import com.example.futbolnomade.domain.model.Jugador
import com.example.futbolnomade.domain.repository.JugadorRepository
import com.example.futbolnomade.domain.repository.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Usuario(
    val nombre: String,
    val email: String,
    val uid: String = ""
)

sealed class AuthResult {
    data object Success : AuthResult()
    data class Error(val mensaje: String) : AuthResult()
}

class AuthViewModel(
    private val jugadorRepository: JugadorRepository = JugadorRepositoryImpl(),
    private val notificationRepository: NotificationRepository = NotificationRepositoryImpl()
) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    var usuarioActual by mutableStateOf<Usuario?>(null)
        private set

    var accesoDesbloqueado by mutableStateOf(false)
        private set

    init {
        actualizarUsuarioDesdeFirebase()

        usuarioActual?.let {
            accesoDesbloqueado = true

            viewModelScope.launch {
                registrarDispositivoActual()
            }
        }
    }

    fun login(
        email: String,
        password: String,
        onResult: (AuthResult) -> Unit
    ) {
        val cleanEmail = email.trim().lowercase()

        viewModelScope.launch {

            val firebaseUser = try {
                val authResult = auth.signInWithEmailAndPassword(
                    cleanEmail,
                    password
                ).await()

                authResult.user
            } catch (exception: Exception) {

                val firebaseException =
                    exception as? FirebaseAuthException

                accesoDesbloqueado = false

                val mensaje =
                    when (firebaseException?.errorCode) {

                        "ERROR_INVALID_CREDENTIAL",
                        "ERROR_WRONG_PASSWORD",
                        "ERROR_USER_NOT_FOUND" ->
                            "Email o contraseña incorrectos."

                        "ERROR_TOO_MANY_REQUESTS" ->
                            "Demasiados intentos de inicio de sesión. Probá nuevamente más tarde."

                        "ERROR_NETWORK_REQUEST_FAILED" ->
                            "No se pudo conectar con Firebase. Revisá tu conexión."

                        "ERROR_USER_DISABLED" ->
                            "Esta cuenta fue deshabilitada."

                        else ->
                            exception.message
                                ?: "No se pudo iniciar sesión."
                    }

                onResult(
                    AuthResult.Error(mensaje)
                )

                return@launch
            }

            if (firebaseUser == null) {
                accesoDesbloqueado = false

                onResult(
                    AuthResult.Error(
                        "Firebase no devolvió el usuario."
                    )
                )

                return@launch
            }

            try {
                val jugadorExistente =
                    jugadorRepository.obtenerJugador(
                        firebaseUser.uid
                    )

                if (jugadorExistente == null) {

                    val nuevoJugador =
                        Jugador(
                            id = firebaseUser.uid,
                            nombre =
                                firebaseUser.displayName
                                    ?: firebaseUser.email
                                        ?.substringBefore("@")
                                    ?: "Usuario",
                            email =
                                firebaseUser.email
                                    ?: cleanEmail
                        )

                    jugadorRepository.guardarJugador(
                        nuevoJugador
                    )
                }
            } catch (_: Exception) {
            }

            actualizarUsuarioDesdeFirebase()

            registrarDispositivoActual()

            accesoDesbloqueado = true

            onResult(
                AuthResult.Success
            )
        }
    }

    fun registrar(
        nombre: String,
        email: String,
        password: String,
        onResult: (AuthResult) -> Unit
    ) {
        val cleanName = nombre.trim()
        val cleanEmail = email.trim().lowercase()

        viewModelScope.launch {
            try {
                val authResult =
                    auth.createUserWithEmailAndPassword(
                        cleanEmail,
                        password
                    ).await()

                val firebaseUser =
                    authResult.user
                        ?: throw IllegalStateException(
                            "Firebase creó la cuenta pero no devolvió el usuario."
                        )

                try {
                    val profileUpdates =
                        UserProfileChangeRequest
                            .Builder()
                            .setDisplayName(cleanName)
                            .build()

                    firebaseUser
                        .updateProfile(profileUpdates)
                        .await()
                } catch (_: Exception) {
                }

                try {
                    val nuevoJugador =
                        Jugador(
                            id = firebaseUser.uid,
                            nombre = cleanName,
                            email = cleanEmail
                        )

                    jugadorRepository.guardarJugador(
                        nuevoJugador
                    )
                } catch (_: Exception) {
                }

                actualizarUsuarioDesdeFirebase()

                usuarioActual =
                    Usuario(
                        nombre = cleanName,
                        email =
                            firebaseUser.email
                                ?: cleanEmail,
                        uid = firebaseUser.uid
                    )

                registrarDispositivoActual()

                accesoDesbloqueado = true

                onResult(
                    AuthResult.Success
                )

            } catch (exception: Exception) {

                val firebaseException =
                    exception as? FirebaseAuthException

                accesoDesbloqueado = false

                val mensaje =
                    when (firebaseException?.errorCode) {

                        "ERROR_EMAIL_ALREADY_IN_USE" ->
                            "Ya existe una cuenta con ese email."

                        "ERROR_WEAK_PASSWORD" ->
                            "La contraseña es demasiado débil."

                        "ERROR_INVALID_EMAIL" ->
                            "El email no es válido."

                        "ERROR_NETWORK_REQUEST_FAILED" ->
                            "No se pudo conectar con Firebase."

                        "ERROR_TOO_MANY_REQUESTS" ->
                            "Demasiados intentos. Probá nuevamente más tarde."

                        else ->
                            exception.message
                                ?: "No se pudo registrar el usuario."
                    }

                onResult(
                    AuthResult.Error(mensaje)
                )
            }
        }
    }

    private suspend fun registrarDispositivoActual() {
        val uid =
            auth.currentUser?.uid
                ?: return

        try {
            notificationRepository.registrarDispositivo(
                uid
            )
        } catch (_: Exception) {
        }
    }

    private fun actualizarUsuarioDesdeFirebase() {
        usuarioActual =
            auth.currentUser?.let { firebaseUser ->
                Usuario(
                    nombre =
                        firebaseUser.displayName.orEmpty(),
                    email =
                        firebaseUser.email.orEmpty(),
                    uid =
                        firebaseUser.uid
                )
            }
    }

    fun logout(
        onComplete: () -> Unit = {}
    ) {
        val uid =
            auth.currentUser?.uid

        viewModelScope.launch {
            try {
                if (!uid.isNullOrBlank()) {
                    notificationRepository
                        .eliminarDispositivo(uid)
                }
            } catch (_: Exception) {
            }

            auth.signOut()

            usuarioActual = null
            accesoDesbloqueado = false

            onComplete()
        }
    }

    fun actualizarUsuarioActual(
        nombre: String,
        email: String,
        password: String
    ) {
        val firebaseUser =
            auth.currentUser
                ?: return

        viewModelScope.launch {
            try {
                val cleanName =
                    nombre.trim()

                val cleanEmail =
                    email.trim().lowercase()

                if (cleanName.isNotBlank()) {
                    val profileUpdates =
                        UserProfileChangeRequest
                            .Builder()
                            .setDisplayName(cleanName)
                            .build()

                    firebaseUser
                        .updateProfile(profileUpdates)
                        .await()
                }

                if (
                    cleanEmail.isNotBlank() &&
                    !cleanEmail.equals(
                        firebaseUser.email,
                        ignoreCase = true
                    )
                ) {
                    firebaseUser
                        .updateEmail(cleanEmail)
                        .await()
                }

                if (password.isNotBlank()) {
                    firebaseUser
                        .updatePassword(password)
                        .await()
                }

                val jugadorActualizado =
                    Jugador(
                        id = firebaseUser.uid,
                        nombre =
                            cleanName.ifBlank {
                                firebaseUser
                                    .displayName
                                    .orEmpty()
                            },
                        email =
                            cleanEmail.ifBlank {
                                firebaseUser
                                    .email
                                    .orEmpty()
                            }
                    )

                jugadorRepository.guardarJugador(
                    jugadorActualizado
                )

                actualizarUsuarioDesdeFirebase()

            } catch (_: Exception) {
            }
        }
    }

    fun loginWithBiometricCredentials(
        credentials: BiometricCredentials,
        onResult: (AuthResult) -> Unit
    ) {
        viewModelScope.launch {
            try {
                auth.signOut()

                auth.signInWithEmailAndPassword(
                    credentials.email
                        .trim()
                        .lowercase(),
                    credentials.password
                ).await()

                val firebaseUser =
                    auth.currentUser
                        ?: throw IllegalStateException(
                            "Firebase no devolvió usuario en login biométrico."
                        )

                if (firebaseUser.uid != credentials.uid) {
                    throw IllegalStateException(
                        "UID biométrico no coincide."
                    )
                }

                actualizarUsuarioDesdeFirebase()

                registrarDispositivoActual()

                accesoDesbloqueado = true

                onResult(
                    AuthResult.Success
                )

            } catch (exception: Exception) {

                accesoDesbloqueado = false

                onResult(
                    AuthResult.Error(
                        exception.message
                            ?: "No se pudo ingresar con biometría."
                    )
                )
            }
        }
    }
}