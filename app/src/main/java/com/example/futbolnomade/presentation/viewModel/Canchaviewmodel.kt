package com.example.futbolnomade.presentation.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futbolnomade.data.repository.CanchaRepositoryImpl
import com.example.futbolnomade.domain.model.Cancha
import com.example.futbolnomade.domain.model.HorarioDisponible
import com.example.futbolnomade.domain.repository.CanchaRepository
import com.example.futbolnomade.presentation.state.CanchaUiState
import kotlinx.coroutines.launch

class CanchaViewModel(
    private val repository: CanchaRepository = CanchaRepositoryImpl()
) : ViewModel() {

    private val canchaRepository = CanchaRepositoryImpl()

    var uiState by mutableStateOf(CanchaUiState())
        private set

    fun cargarCanchas(userId: String) {
        viewModelScope.launch {
            repository.getCanchas(userId).collect { canchas ->
                uiState = uiState.copy(canchas = canchas)
            }
        }
    }

    fun cargarTodasLasCanchas() {
        viewModelScope.launch {
            repository.getAllCanchas().collect { canchas ->
                uiState = uiState.copy(canchas = canchas)
            }
        }
    }

    fun crearCancha(
        nombre: String,
        ubicacion: String,
        descripcion: String,
        precio: String,
        telefono: String,
        horarioApertura: String,
        horarioCierre: String,
        horariosDetallados: List<HorarioDisponible>,
        propietario: String,
        latitud: Double,
        longitud: Double
    ) {
        viewModelScope.launch {
            try {
                val nuevaCancha = Cancha(
                    id = 0,
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
                    latitud = latitud,
                    longitud = longitud,
                    horarios = horariosDetallados
                )

                canchaRepository.crearCancha(nuevaCancha)

                actualizarUiState()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun eliminarCancha(canchaId: Int) {
        canchaRepository.eliminarCancha(canchaId)
        actualizarUiState()
    }

    fun actualizarCancha(
        canchaId: String,
        nombre: String,
        ubicacion: String,
        descripcion: String,
        precio: String,
        telefono: String,
        disponible: Boolean,
        latitud: Double? = null,
        longitud: Double? = null
    ) {
        val canchaActual = canchaRepository.obtenerCancha(canchaId) ?: return

        val canchaActualizada = canchaActual.copy(
            nombre = nombre,
            ubicacion = ubicacion,
            descripcion = descripcion,
            precio = precio.toDoubleOrNull() ?: canchaActual.precio,
            telefono = telefono,
            disponible = disponible,
            latitud = latitud ?: canchaActual.latitud,
            longitud = longitud ?: canchaActual.longitud
        )

        canchaRepository.actualizarCancha(canchaActualizada)
        actualizarUiState()
    }

    fun agregarHorario(
        canchaId: Int,
        horario: HorarioDisponible
    ) {
        canchaRepository.agregarHorario(canchaId, horario)
        actualizarUiState()
    }

    fun eliminarHorario(
        canchaId: Int,
        horario: HorarioDisponible
    ) {
        canchaRepository.eliminarHorario(canchaId, horario)
        actualizarUiState()
    }

    fun misCanchas(emailPropietario: String): List<Cancha> {
        return canchaRepository.obtenerCanchasPorPropietario(emailPropietario)
    }

    fun getCanchaById(id: Int): Cancha? {
        return canchaRepository.obtenerCancha(id)
    }
}
