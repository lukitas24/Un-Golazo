package com.example.futbolnomade.presentation.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futbolnomade.data.repository.PartidoRepositoryImpl
import com.example.futbolnomade.domain.model.Partido
import com.example.futbolnomade.domain.repository.PartidoRepository
import com.example.futbolnomade.presentation.state.PartidoUiState
import kotlinx.coroutines.launch

class PartidoViewModel(private val repository: PartidoRepository = PartidoRepositoryImpl()) : ViewModel() {

    var uiState by mutableStateOf(PartidoUiState())
        private set

    init {
        cargarPartidos()
    }

    fun cargarPartidos() {
        viewModelScope.launch {
            val partidos = repository.obtenerPartidos()
            uiState = uiState.copy(partidos = partidos)
        }
    }

    fun crearPartido(
        titulo: String,
        horario: String,
        fecha: String,
        ubicacion: String,
        dificultad: String,
        participantes: Int,
        descripcion: String,
        creador: String
    ) {
        viewModelScope.launch {
            val nuevoPartido = Partido(
                titulo = titulo,
                horario = horario,
                fecha = fecha,
                ubicacion = ubicacion,
                dificultad = dificultad,
                participantesActuales = 1,
                participantesMaximos = participantes,
                creador = creador,
                calificacionCreador = 5.0,
                descripcion = descripcion
            )
            repository.crearPartido(nuevoPartido)
            cargarPartidos()
        }
    }

    fun anotarseAPartido(partidoId: String, usuario: String) {
        viewModelScope.launch {
            repository.anotarseAPartido(partidoId, usuario)
            cargarPartidos()
        }
    }

    fun cancelarInscripcion(partidoId: String, usuario: String) {
        viewModelScope.launch {
            repository.cancelarInscripcion(partidoId, usuario)
            cargarPartidos()
        }
    }
}
