package com.example.futbolnomade.presentation.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.futbolnomade.domain.model.Cancha
import com.example.futbolnomade.domain.model.HorarioDisponible
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
                    id = 1, nombre = "Cancha Maracana",
                    ubicacion = "Puerto Madryn, Mitre 423",
                    descripcion = "Cancha de césped sintético techada",
                    precio = 5000.0, telefono = "2804001234",
                    horarioApertura = "08:00", horarioCierre = "22:00",
                    calificacion = 4.9, propietario = "admin@gmail.com", disponible = true
                ),
                Cancha(
                    id = 2, nombre = "Cancha San Martín 132",
                    ubicacion = "Puerto Madryn, San Martín 132",
                    descripcion = "Cancha de césped natural al aire libre",
                    precio = 3500.0, telefono = "2804005678",
                    horarioApertura = "09:00", horarioCierre = "21:00",
                    calificacion = 3.9, propietario = "admin@gmail.com", disponible = true
                ),
                Cancha(
                    id = 3, nombre = "Cancha Norte",
                    ubicacion = "Puerto Madryn, Av. Gales 500",
                    descripcion = "Cancha de fútbol 7",
                    precio = 4000.0, telefono = "2804009999",
                    horarioApertura = "10:00", horarioCierre = "20:00",
                    calificacion = 4.2, propietario = "otro@gmail.com", disponible = false
                )
            )
        )
    }

    // ── Crear ──────────────────────────────────────────────────────────────
    fun crearCancha(
        nombre: String,
        ubicacion: String,
        descripcion: String,
        precio: String,
        telefono: String,
        horarioApertura: String,
        horarioCierre: String,
        propietario: String = ""        // ← ahora recibe el email del usuario
    ) {
        val nueva = Cancha(
            id              = (uiState.canchas.maxOfOrNull { it.id } ?: 0) + 1,
            nombre          = nombre,
            ubicacion       = ubicacion,
            descripcion     = descripcion,
            precio          = precio.toDoubleOrNull() ?: 0.0,
            telefono        = telefono,
            horarioApertura = horarioApertura,
            horarioCierre   = horarioCierre,
            calificacion    = 0.0,
            propietario     = propietario,
            disponible      = true
        )
        uiState = uiState.copy(canchas = uiState.canchas + nueva)
    }

    // ── Eliminar ───────────────────────────────────────────────────────────
    fun eliminarCancha(canchaId: Int) {
        uiState = uiState.copy(
            canchas = uiState.canchas.filter { it.id != canchaId }
        )
    }

    // ── Editar datos básicos ───────────────────────────────────────────────
    fun actualizarCancha(
        canchaId: Int,
        nombre: String,
        ubicacion: String,
        descripcion: String,
        precio: String,
        telefono: String,
        disponible: Boolean
    ) {
        uiState = uiState.copy(
            canchas = uiState.canchas.map { c ->
                if (c.id == canchaId) c.copy(
                    nombre      = nombre,
                    ubicacion   = ubicacion,
                    descripcion = descripcion,
                    precio      = precio.toDoubleOrNull() ?: c.precio,
                    telefono    = telefono,
                    disponible  = disponible
                ) else c
            }
        )
    }

    // ── Horarios por día ───────────────────────────────────────────────────
    fun agregarHorario(canchaId: Int, horario: HorarioDisponible) {
        uiState = uiState.copy(
            canchas = uiState.canchas.map { c ->
                if (c.id == canchaId) c.copy(horarios = c.horarios + horario)
                else c
            }
        )
    }

    fun eliminarHorario(canchaId: Int, horario: HorarioDisponible) {
        uiState = uiState.copy(
            canchas = uiState.canchas.map { c ->
                if (c.id == canchaId) c.copy(horarios = c.horarios - horario)
                else c
            }
        )
    }

    // ── Filtros ────────────────────────────────────────────────────────────
    fun misCanchas(emailPropietario: String): List<Cancha> =
        uiState.canchas.filter {
            it.propietario.lowercase() == emailPropietario.lowercase()
        }

    fun getCanchaById(id: Int): Cancha? =
        uiState.canchas.find { it.id == id }
}