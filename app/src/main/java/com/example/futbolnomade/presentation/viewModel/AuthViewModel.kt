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

    // Lista de usuarios registrados (en memoria; reemplazar por Room/API en el futuro)
    private val usuarios = mutableListOf(
        Usuario(nombre = "Admin", email = "admin@gmail.com", password = "123456")
    )

    // Usuario actualmente logueado
    var usuarioActual by mutableStateOf<Usuario?>(null)
        private set

    // ── Login ──────────────────────────────────────────────────────────────
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

    // ── Registro ───────────────────────────────────────────────────────────
    fun registrar(nombre: String, email: String, password: String): AuthResult {
        val cleanEmail = email.trim().lowercase()
        val cleanNombre = nombre.trim()
        val cleanPassword = password.trim()

        if (usuarios.any { it.email.lowercase() == cleanEmail }) {
            return AuthResult.Error("Ya existe una cuenta con ese email")
        }

        val nuevo = Usuario(nombre = cleanNombre, email = cleanEmail, password = cleanPassword)
        usuarios.add(nuevo)
        usuarioActual = nuevo
        return AuthResult.Success
    }

    // ── Logout ─────────────────────────────────────────────────────────────
    fun logout() {
        usuarioActual = null
    }
}