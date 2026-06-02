package com.example.futbolnomade.presentation.viewModel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class PerfilViewModel : ViewModel() {

    var nombre   by mutableStateOf("")
        private set
    var email    by mutableStateOf("")
        private set
    var imageUri by mutableStateOf<Uri?>(null)
        private set

    // Llamado desde AppNavigation cada vez que el usuario inicia sesión
    fun inicializar(nombre: String, email: String) {
        if (this.nombre != nombre || this.email != email) {
            this.nombre = nombre
            this.email  = email
            // No pisamos imageUri para conservar la foto si ya la cambió
        }
    }

    fun actualizarPerfil(nombre: String, email: String, uri: Uri?) {
        this.nombre   = nombre
        this.email    = email
        if (uri != null) this.imageUri = uri
    }

    fun limpiar() {
        nombre   = ""
        email    = ""
        imageUri = null
    }
}