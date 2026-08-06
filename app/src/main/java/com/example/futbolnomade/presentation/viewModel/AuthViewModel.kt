package com.example.futbolnomade.presentation.viewModel

import android.util.Log
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

    data class Error(
        val mensaje: String
    ) : AuthResult()
}

class AuthViewModel(
    private val jugadorRepository: JugadorRepository =
        JugadorRepositoryImpl(),

    private val notificationRepository: NotificationRepository =
        NotificationRepositoryImpl()
) : ViewModel() {

    private val auth: FirebaseAuth =
        FirebaseAuth.getInstance()

    var usuarioActual by mutableStateOf<Usuario?>(null)
        private set

    /*
     * Solo vive en memoria.
     * Pasar la app a segundo plano no lo borra.
     */
    var accesoDesbloqueado by mutableStateOf(false)
        private set

    init {
        actualizarUsuarioDesdeFirebase()
        accesoDesbloqueado = false
    }

    fun login(
        email: String,
        password: String,
        onResult: (AuthResult) -> Unit
    ) {
        val cleanEmail =
            email
                .trim()
                .lowercase()

        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(
                    cleanEmail,
                    password
                ).await()

                actualizarUsuarioDesdeFirebase()

                /*
                 * Asocia el token FCM de este teléfono
                 * con el UID que acaba de iniciar sesión.
                 */
                registrarDispositivoActual()

                accesoDesbloqueado = true

                onResult(AuthResult.Success)
            } catch (exception: Exception) {
                accesoDesbloqueado = false

                onResult(
                    AuthResult.Error(
                        exception.message
                            ?: "Email o contraseña incorrectos"
                    )
                )
            }
        }
    }

    fun loginWithBiometricCredentials(
        credentials: BiometricCredentials,
        onResult: (AuthResult) -> Unit
    ) {
        viewModelScope.launch {
            try {
                /*
                 * Puede haber quedado otra cuenta activa.
                 */
                auth.signOut()

                auth.signInWithEmailAndPassword(
                    credentials.email,
                    credentials.password
                ).await()

                val firebaseUser =
                    auth.currentUser
                        ?: throw IllegalStateException(
                            "Firebase no devolvió un usuario."
                        )

                if (firebaseUser.uid != credentials.uid) {
                    auth.signOut()

                    throw IllegalStateException(
                        "La credencial biométrica no coincide con la cuenta vinculada."
                    )
                }

                actualizarUsuarioDesdeFirebase()

                /*
                 * También registra el teléfono cuando
                 * se ingresa usando biometría.
                 */
                registrarDispositivoActual()

                accesoDesbloqueado = true

                onResult(AuthResult.Success)
            } catch (exception: Exception) {
                usuarioActual = null
                accesoDesbloqueado = false

                onResult(
                    AuthResult.Error(
                        exception.message
                            ?: "No se pudo iniciar sesión con biometría."
                    )
                )
            }
        }
    }

    fun registrar(
        nombre: String,
        email: String,
        password: String,
        onResult: (AuthResult) -> Unit
    ) {
        val cleanName =
            nombre.trim()

        val cleanEmail =
            email
                .trim()
                .lowercase()

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
                            "Firebase no devolvió el usuario registrado."
                        )

                val profileUpdates =
                    UserProfileChangeRequest.Builder()
                        .setDisplayName(cleanName)
                        .build()

                firebaseUser
                    .updateProfile(profileUpdates)
                    .await()

                val nuevoJugador =
                    Jugador(
                        id = firebaseUser.uid,
                        nombre = cleanName,
                        email = cleanEmail
                    )

                jugadorRepository.guardarJugador(
                    nuevoJugador
                )

                usuarioActual =
                    Usuario(
                        nombre = cleanName,
                        email = cleanEmail,
                        uid = firebaseUser.uid
                    )

                /*
                 * Registra el dispositivo después de
                 * crear la cuenta.
                 */
                registrarDispositivoActual()

                accesoDesbloqueado = true

                onResult(AuthResult.Success)
            } catch (exception: Exception) {
                accesoDesbloqueado = false

                onResult(
                    AuthResult.Error(
                        exception.message
                            ?: "No se pudo registrar el usuario"
                    )
                )
            }
        }
    }

    /**
     * Obtiene el token actual de Firebase Messaging y lo guarda en:
     *
     * jugadores/{uid}/dispositivos/{hashToken}
     *
     * Si falla, no impide que el usuario ingrese.
     */
    private suspend fun registrarDispositivoActual() {
        val uid =
            auth.currentUser
                ?.uid
                ?: return

        try {
            notificationRepository
                .registrarDispositivo(
                    uid
                )

            Log.d(
                "FutbolNomadeFCM",
                "Dispositivo asociado correctamente al usuario $uid"
            )
        } catch (exception: Exception) {
            Log.e(
                "FutbolNomadeFCM",
                "No se pudo asociar el dispositivo con el usuario.",
                exception
            )
        }
    }

    private fun actualizarUsuarioDesdeFirebase() {
        usuarioActual =
            auth.currentUser?.let { firebaseUser ->
                Usuario(
                    nombre =
                        firebaseUser
                            .displayName
                            .orEmpty(),

                    email =
                        firebaseUser
                            .email
                            .orEmpty(),

                    uid =
                        firebaseUser.uid
                )
            }
    }

    /**
     * Primero elimina la asociación del token FCM y después
     * cierra Firebase Auth.
     *
     * Es importante respetar ese orden porque las reglas de
     * Firestore necesitan que el usuario siga autenticado para
     * eliminar jugadores/{uid}/dispositivos/{deviceId}.
     */
    fun logout(
        onComplete: () -> Unit = {}
    ) {
        val uid =
            auth.currentUser
                ?.uid

        viewModelScope.launch {
            try {
                if (!uid.isNullOrBlank()) {
                    notificationRepository
                        .eliminarDispositivo(
                            uid
                        )

                    Log.d(
                        "FutbolNomadeFCM",
                        "Dispositivo eliminado del usuario $uid"
                    )
                }
            } catch (exception: Exception) {
                /*
                 * Aunque no se pueda borrar el token,
                 * igualmente cerramos la sesión.
                 */
                Log.e(
                    "FutbolNomadeFCM",
                    "No se pudo eliminar el dispositivo antes del logout.",
                    exception
                )
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
                    email
                        .trim()
                        .lowercase()

                if (cleanName.isNotBlank()) {
                    val profileUpdates =
                        UserProfileChangeRequest.Builder()
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

                usuarioActual =
                    Usuario(
                        nombre =
                            jugadorActualizado.nombre,

                        email =
                            jugadorActualizado.email,

                        uid =
                            firebaseUser.uid
                    )
            } catch (exception: Exception) {
                Log.e(
                    "AuthViewModel",
                    "No se pudo actualizar el usuario.",
                    exception
                )
            }
        }
    }
}