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

    fun crearCancha(
        nombre: String,
        ubicacion: String,
        descripcion: String,
        precio: String,
        telefono: String,
        horarioApertura: String,
        horarioCierre: String,
        propietario: String
    ) {
        viewModelScope.launch {
            val nueva = Cancha(
                nombre = nombre,
                ubicacion = ubicacion,
                descripcion = descripcion,
                precio = precio.toDoubleOrNull() ?: 0.0,
                telefono = telefono,
                horarioApertura = horarioApertura,
                horarioCierre = horarioCierre,
                propietario = propietario
            )
            repository.guardarCancha(nueva)
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
        disponible: Boolean
    ) {
        viewModelScope.launch {
            val cancha = uiState.canchas.find { it.id == canchaId } ?: return@launch
            val actualizada = cancha.copy(
                nombre = nombre,
                ubicacion = ubicacion,
                descripcion = descripcion,
                precio = precio.toDoubleOrNull() ?: cancha.precio,
                telefono = telefono,
                disponible = disponible
            )
            repository.guardarCancha(actualizada)
        }
    }

    fun agregarHorario(canchaId: String, horario: HorarioDisponible) {
        viewModelScope.launch {
            val cancha = uiState.canchas.find { it.id == canchaId } ?: return@launch
            val actualizada = cancha.copy(horarios = cancha.horarios + horario)
            repository.guardarCancha(actualizada)
        }
    }

    fun eliminarHorario(canchaId: String, horario: HorarioDisponible) {
        viewModelScope.launch {
            val cancha = uiState.canchas.find { it.id == canchaId } ?: return@launch
            val actualizada = cancha.copy(horarios = cancha.horarios - horario)
            repository.guardarCancha(actualizada)
        }
    }

    fun misCanchas(emailPropietario: String): List<Cancha> =
        uiState.canchas.filter {
            it.propietario.lowercase() == emailPropietario.lowercase()
        }

    fun getCanchaById(id: String): Cancha? =
        uiState.canchas.find { it.id == id }
}
