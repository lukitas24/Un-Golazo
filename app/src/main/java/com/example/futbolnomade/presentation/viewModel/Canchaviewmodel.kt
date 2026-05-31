package com.example.futbolnomade.presentation.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.futbolnomade.domain.model.Cancha
import com.example.futbolnomade.presentation.state.CanchaUiState

class CanchaViewModel : ViewModel() {

    var uiState by mutableStateOf(CanchaUiState())
        private set

    init {
        cargarCanchasEjemplo()
    }

    private fun cargarCanchasEjemplo() {
        uiState = uiState.copy(
            canchas = listOf(
                Cancha(
                    id = 1,
                    nombre = "Cancha J.J. Moreno",
                    ubicacion = "Puerto Madryn, Mitre 423",
                    descripcion = "Cancha de césped sintético techada",
                    precio = 5000.0,
                    telefono = "2804001234",
                    horarioApertura = "08:00",
                    horarioCierre = "22:00",
                    calificacion = 4.9,
                    propietario = "admin",
                    disponible = true
                ),
                Cancha(
                    id = 2,
                    nombre = "Cancha J.J. Moreno",
                    ubicacion = "Puerto Madryn, Mitre 423",
                    descripcion = "Cancha de césped natural al aire libre",
                    precio = 3500.0,
                    telefono = "2804005678",
                    horarioApertura = "09:00",
                    horarioCierre = "21:00",
                    calificacion = 4.9,
                    propietario = "admin",
                    disponible = true
                ),
                Cancha(
                    id = 3,
                    nombre = "Cancha J.J. Moreno",
                    ubicacion = "Puerto Madryn, Mitre 423",
                    descripcion = "Cancha de fútbol 7",
                    precio = 4000.0,
                    telefono = "2804009999",
                    horarioApertura = "10:00",
                    horarioCierre = "20:00",
                    calificacion = 4.9,
                    propietario = "admin",
                    disponible = true
                )
            )
        )
    }

    fun crearCancha(
        nombre: String,
        ubicacion: String,
        descripcion: String,
        precio: String,
        telefono: String,
        horarioApertura: String,
        horarioCierre: String
    ) {
        val nuevaCancha = Cancha(
            id = (uiState.canchas.maxOfOrNull { it.id } ?: 0) + 1,
            nombre = nombre,
            ubicacion = ubicacion,
            descripcion = descripcion,
            precio = precio.toDoubleOrNull() ?: 0.0,
            telefono = telefono,
            horarioApertura = horarioApertura,
            horarioCierre = horarioCierre,
            calificacion = 0.0,
            propietario = "admin",
            disponible = true
        )

        uiState = uiState.copy(
            canchas = uiState.canchas + nuevaCancha
        )
    }
}