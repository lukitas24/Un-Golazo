package com.example.futbolnomade.presentation.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futbolnomade.data.repository.CanchaRepositoryImpl
import com.example.futbolnomade.domain.model.Cancha
import com.example.futbolnomade.domain.model.HorarioDisponible
import com.example.futbolnomade.presentation.state.CanchaUiState
import kotlinx.coroutines.launch

class CanchaViewModel : ViewModel() {
    // Instanciamos el repositorio local en memoria
    private val canchaRepository = CanchaRepositoryImpl()

    var uiState by mutableStateOf(CanchaUiState())
        private set

    init {
        actualizarUiState() // Cargamos el estado inicial con lo que tiene el repositorio
    }

    // Método auxiliar para sincronizar la lista de la "Base de datos" con la UI
    private fun actualizarUiState() {
        uiState = uiState.copy(
            canchas = canchaRepository.obtenerCanchas()
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
        horariosDetallados: List<HorarioDisponible>,
        propietario: String
    ) {
        viewModelScope.launch {
            try {
                val nuevaCancha = Cancha(
                    id = 0, // El repositorio se encargará de autoincrementar este ID
                    nombre = nombre,
                    ubicacion = ubicacion,
                    descripcion = descripcion,
                    precio = precio.toDoubleOrNull() ?: 0.0,
                    telefono = telefono,
                    horarioApertura = horarioApertura,
                    horarioCierre = horarioCierre,
                    calificacion = 5.0,
                    propietario = propietario,
                    disponible = true,
                    horarios = horariosDetallados
                )

                // CORRECCIÓN: Llamamos al método correcto del repositorio
                canchaRepository.crearCancha(nuevaCancha)

                // Refrescamos la UI con la nueva lista modificada
                actualizarUiState()

            } catch (e: Exception) {
                // Manejo de errores opcional
            }
        }
    }

    // ── Eliminar ───────────────────────────────────────────────────────────
    fun eliminarCancha(canchaId: Int) {
        canchaRepository.eliminarCancha(canchaId)
        actualizarUiState()
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
        val canchaActual = canchaRepository.obtenerCancha(canchaId) ?: return

        val canchaActualizada = canchaActual.copy(
            nombre = nombre,
            ubicacion = ubicacion,
            descripcion = descripcion,
            precio = precio.toDoubleOrNull() ?: canchaActual.precio,
            telefono = telefono,
            disponible = disponible
        )

        canchaRepository.actualizarCancha(canchaActualizada)
        actualizarUiState()
    }

    // ── Horarios por día ───────────────────────────────────────────────────
    fun agregarHorario(canchaId: Int, horario: HorarioDisponible) {
        canchaRepository.agregarHorario(canchaId, horario)
        actualizarUiState()
    }

    fun eliminarHorario(canchaId: Int, horario: HorarioDisponible) {
        canchaRepository.eliminarHorario(canchaId, horario)
        actualizarUiState()
    }
    // ── Filtros ────────────────────────────────────────────────────────────
    fun misCanchas(emailPropietario: String): List<Cancha> =
        canchaRepository.obtenerCanchasPorPropietario(emailPropietario)

    fun getCanchaById(id: Int): Cancha? =
        canchaRepository.obtenerCancha(id)
}