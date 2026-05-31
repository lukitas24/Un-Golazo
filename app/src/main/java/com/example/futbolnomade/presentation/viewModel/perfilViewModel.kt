package com.example.futbolnomade.presentation.viewModel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class PerfilViewModel : ViewModel() {

    var nombre by mutableStateOf("maia")
        private set

    var email by mutableStateOf("maia@email.com")
        private set

    var imageUri by mutableStateOf<Uri?>(null)
        private set

    fun actualizarPerfil(nuevoNombre: String, nuevoEmail: String, uri: Uri?) {
        nombre = nuevoNombre
        email = nuevoEmail
        imageUri = uri
    }
}