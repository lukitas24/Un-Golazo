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
                    id = "", // Firebase will generate it
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

                repository.guardarCancha(nuevaCancha)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun eliminarCancha(canchaId: String) {
        viewModelScope.launch {
            repository.eliminarCancha(canchaId)
        }
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
        viewModelScope.launch {
            val canchaActual = uiState.canchas.find { it.id == canchaId } ?: return@launch

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

            repository.guardarCancha(canchaActualizada)
        }
    }

    fun agregarHorario(
        canchaId: String,
        horario: HorarioDisponible
    ) {
        viewModelScope.launch {
            val cancha = uiState.canchas.find { it.id == canchaId } ?: return@launch
            repository.guardarCancha(cancha.copy(horarios = cancha.horarios + horario))
        }
    }

    fun eliminarHorario(
        canchaId: String,
        horario: HorarioDisponible
    ) {
        viewModelScope.launch {
            val cancha = uiState.canchas.find { it.id == canchaId } ?: return@launch
            repository.guardarCancha(cancha.copy(horarios = cancha.horarios - horario))
        }
    }

    fun misCanchas(emailPropietario: String): List<Cancha> {
        return uiState.canchas.filter { it.propietario == emailPropietario }
    }

    fun getCanchaById(id: String): Cancha? {
        return uiState.canchas.find { it.id == id }
    }
}
