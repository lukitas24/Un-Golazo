package com.example.futbolnomade.presentation.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.futbolnomade.data.repository.PartidoRepositoryImpl
import com.example.futbolnomade.domain.model.Partido
import com.example.futbolnomade.presentation.state.PartidoUiState

class PartidoViewModel : ViewModel() {

    private val repository = PartidoRepositoryImpl()

    var uiState by mutableStateOf(
        PartidoUiState()
    )
        private set

    init {
        cargarPartidos()
    }

    private fun cargarPartidos() {
        uiState = uiState.copy(
            partidos = repository.obtenerPartidos()
        )
    }

    fun obtenerPartido(id: Int): Partido? {
        return repository.obtenerPartido(id)
    }

    fun crearPartido(
        titulo: String,
        horario: String,
        fecha: String,
        ubicacion: String,
        dificultad: String,
        participantes: Int,
        descripcion: String
    ) {

        val nuevoPartido = Partido(
            id = System.currentTimeMillis().toInt(),
            titulo = titulo,
            horario = horario,
            fecha = fecha,
            ubicacion = ubicacion,
            dificultad = dificultad,
            participantesActuales = 1,
            participantesMaximos = participantes,
            creador = "admin",
            calificacionCreador = 5.0,
            descripcion = descripcion
        )

        repository.crearPartido(nuevoPartido)

        cargarPartidos()
    }
    fun anotarseAPartido(
        partidoId: Int,
        usuario: String
    ): Boolean {
        val resultado = repository.anotarseAPartido(
            partidoId = partidoId,
            usuario = usuario
        )

        cargarPartidos()

        return resultado
    }
    fun cancelarInscripcion(
        partidoId: Int,
        usuario: String
    ): Boolean {
        val resultado = repository.cancelarInscripcion(
            partidoId = partidoId,
            usuario = usuario
        )

        cargarPartidos()

        return resultado
    }
}