package com.example.futbolnomade.presentation.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class Usuario(
    val nombre: String,
    val email: String,
    val password: String
)

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val mensaje: String) : AuthResult()
}

class AuthViewModel : ViewModel() {

    private val usuarios = mutableListOf(
        Usuario(nombre = "Admin", email = "admin@gmail.com", password = "123456")
    )

    var usuarioActual by mutableStateOf<Usuario?>(null)
        private set

    fun login(email: String, password: String): AuthResult {
        val cleanEmail    = email.trim().lowercase()
        val cleanPassword = password.trim()

        val encontrado = usuarios.find {
            it.email.lowercase() == cleanEmail && it.password == cleanPassword
        }

        return if (encontrado != null) {
            usuarioActual = encontrado
            AuthResult.Success
        } else {
            AuthResult.Error("Email o contraseña incorrectos")
        }
    }

    fun registrar(nombre: String, email: String, password: String): AuthResult {
        val cleanEmail = email.trim().lowercase()

        if (usuarios.any { it.email.lowercase() == cleanEmail }) {
            return AuthResult.Error("Ya existe una cuenta con ese email")
        }

        val nuevo = Usuario(nombre = nombre.trim(), email = cleanEmail, password = password.trim())
        usuarios.add(nuevo)
        usuarioActual = nuevo
        return AuthResult.Success
    }

    // Actualiza nombre/email/password del usuario actual (desde EditarPerfil)
    fun actualizarUsuarioActual(nombre: String, email: String, password: String) {
        val actual = usuarioActual ?: return
        val index  = usuarios.indexOfFirst { it.email.lowercase() == actual.email.lowercase() }
        if (index == -1) return

        val nuevaPassword = if (password.isBlank()) actual.password else password.trim()
        val actualizado   = actual.copy(
            nombre   = nombre.trim().ifBlank { actual.nombre },
            email    = email.trim().lowercase().ifBlank { actual.email },
            password = nuevaPassword
        )
        usuarios[index] = actualizado
        usuarioActual   = actualizado
    }

    fun logout() {
        usuarioActual = null
    }
}