package com.example.futbolnomade.presentation.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futbolnomade.data.repository.JugadorRepositoryImpl
import com.example.futbolnomade.data.security.BiometricCredentials
import com.example.futbolnomade.domain.model.Jugador
import com.example.futbolnomade.domain.repository.JugadorRepository
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
        JugadorRepositoryImpl()
) : ViewModel() {

    private val auth: FirebaseAuth =
        FirebaseAuth.getInstance()

    var usuarioActual by mutableStateOf<Usuario?>(null)
        private set

    /*
     * Solo vive en memoria. Pasar la app a segundo plano no lo borra.
     * Al destruirse el proceso, la nueva ejecución vuelve a empezar
     * en la pantalla de Login.
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
        val cleanEmail = email
            .trim()
            .lowercase()

        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(
                    cleanEmail,
                    password
                ).await()

                actualizarUsuarioDesdeFirebase()
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
                 * Puede haber quedado otra cuenta activa en Firebase.
                 * La cerramos antes de recuperar la cuenta biométrica.
                 */
                auth.signOut()

                auth.signInWithEmailAndPassword(
                    credentials.email,
                    credentials.password
                ).await()

                val firebaseUser = auth.currentUser
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
        val cleanName = nombre.trim()
        val cleanEmail = email
            .trim()
            .lowercase()

        viewModelScope.launch {
            try {
                val authResult =
                    auth.createUserWithEmailAndPassword(
                        cleanEmail,
                        password
                    ).await()

                val firebaseUser = authResult.user

                val profileUpdates =
                    UserProfileChangeRequest.Builder()
                        .setDisplayName(cleanName)
                        .build()

                firebaseUser
                    ?.updateProfile(profileUpdates)
                    ?.await()

                val nuevoJugador = Jugador(
                    id = firebaseUser?.uid.orEmpty(),
                    nombre = cleanName,
                    email = cleanEmail
                )

                jugadorRepository.guardarJugador(
                    nuevoJugador
                )

                usuarioActual = Usuario(
                    nombre = cleanName,
                    email = cleanEmail,
                    uid = firebaseUser?.uid.orEmpty()
                )

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

    private fun actualizarUsuarioDesdeFirebase() {
        usuarioActual = auth.currentUser?.let { firebaseUser ->
            Usuario(
                nombre = firebaseUser.displayName.orEmpty(),
                email = firebaseUser.email.orEmpty(),
                uid = firebaseUser.uid
            )
        }
    }

    fun logout() {
        auth.signOut()

        usuarioActual = null
        accesoDesbloqueado = false
    }

    fun actualizarUsuarioActual(
        nombre: String,
        email: String,
        password: String
    ) {
        val firebaseUser = auth.currentUser
            ?: return

        viewModelScope.launch {
            try {
                val cleanName = nombre.trim()

                val cleanEmail = email
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

                val jugadorActualizado = Jugador(
                    id = firebaseUser.uid,
                    nombre = cleanName.ifBlank {
                        firebaseUser.displayName.orEmpty()
                    },
                    email = cleanEmail.ifBlank {
                        firebaseUser.email.orEmpty()
                    }
                )

                jugadorRepository.guardarJugador(
                    jugadorActualizado
                )

                usuarioActual = Usuario(
                    nombre = jugadorActualizado.nombre,
                    email = jugadorActualizado.email,
                    uid = firebaseUser.uid
                )
            } catch (_: Exception) {
                /*
                 * Después se puede exponer un estado de error
                 * para mostrarlo en EditarPerfilScreen.
                 */
            }
        }
    }
}