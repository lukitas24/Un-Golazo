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
    var uid      by mutableStateOf("")
        private set
    var imageUri by mutableStateOf<Uri?>(null)
        private set

    // Llamado desde AppNavigation cada vez que el usuario inicia sesión
    fun inicializar(nombre: String, email: String, uid: String) {
        if (this.nombre != nombre || this.email != email || this.uid != uid) {
            this.nombre = nombre
            this.email  = email
            this.uid    = uid
            // No pisamos imageUri para conservar la foto si ya la cambió
        }
    }

    fun actualizarPerfil(nombre: String, email: String, uid: String, uri: Uri?) {
        this.nombre   = nombre
        this.email    = email
        this.uid      = uid
        if (uri != null) this.imageUri = uri
    }

    fun limpiar() {
        nombre   = ""
        email    = ""
        uid      = ""
        imageUri = null
    }
}