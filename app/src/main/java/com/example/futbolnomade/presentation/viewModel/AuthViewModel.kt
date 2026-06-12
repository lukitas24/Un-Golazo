package com.example.futbolnomade.presentation.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.example.futbolnomade.data.repository.JugadorRepositoryImpl
import com.example.futbolnomade.domain.model.Jugador
import com.example.futbolnomade.domain.repository.JugadorRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Usuario(
    val nombre: String,
    val email: String,
    val uid: String = "",
)

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val mensaje: String) : AuthResult()
}

class AuthViewModel(
    private val jugadorRepository: JugadorRepository = JugadorRepositoryImpl()
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    var usuarioActual by mutableStateOf<Usuario?>(null)
        private set

    init {
        val user = auth.currentUser
        usuarioActual = user?.let {
            Usuario(
                nombre = it.displayName ?: "",
                email = it.email ?: "",
                uid = it.uid
            )
        }
    }

    fun login(email: String, password: String, onResult: (AuthResult) -> Unit) {
        val cleanEmail = email.trim().lowercase()
        val cleanPassword = password.trim()

        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(cleanEmail, cleanPassword).await()
                val user = auth.currentUser
                usuarioActual = user?.let {
                    Usuario(nombre = it.displayName ?: "", email = it.email ?: "", uid = it.uid)
                }
                onResult(AuthResult.Success)
            } catch (e: Exception) {
                onResult(AuthResult.Error(e.message ?: "Email o contraseña incorrectos"))
            }
        }
    }

    fun registrar(nombre: String, email: String, password: String, onResult: (AuthResult) -> Unit) {
        val cleanEmail = email.trim().lowercase()

        viewModelScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(cleanEmail, password.trim()).await()
                val user = result.user
                
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(nombre.trim())
                    .build()
                user?.updateProfile(profileUpdates)?.await()
                
                val nuevoJugador = Jugador(
                    id = user?.uid ?: "",
                    nombre = nombre.trim(),
                    email = cleanEmail
                )
                jugadorRepository.guardarJugador(nuevoJugador)
                
                usuarioActual = Usuario(nombre = nombre.trim(), email = cleanEmail, uid = user?.uid ?: "")
                onResult(AuthResult.Success)
            } catch (e: Exception) {
                onResult(AuthResult.Error(e.message ?: "Error al registrar"))
            }
        }
    }

    fun logout() {
        auth.signOut()
        usuarioActual = null
    }
}
