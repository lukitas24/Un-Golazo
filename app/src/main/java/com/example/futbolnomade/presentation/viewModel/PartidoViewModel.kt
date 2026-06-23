package com.example.futbolnomade.presentation.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futbolnomade.data.repository.PartidoRepositoryImpl
import com.example.futbolnomade.domain.model.EstadoPartido
import com.example.futbolnomade.domain.model.Partido
import com.example.futbolnomade.domain.repository.PartidoRepository
import com.example.futbolnomade.presentation.state.PartidoUiState
import kotlinx.coroutines.launch
import com.example.futbolnomade.domain.model.calcularFechaHoraInicioMillis
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

    fun misPartidos(emailUsuario: String): List<Partido> {
        return uiState.partidos.filter { partido ->
            partido.creador == emailUsuario
        }
    }

    fun partidosVisibles(): List<Partido> {
        return uiState.partidos.filter { partido ->
            partido.estado == EstadoPartido.PUBLICADO ||
                    partido.estado == EstadoPartido.RESERVA_APROBADA
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
        creador: String,
        canchaId: String? = null,
        nombreCancha: String? = null,
        latitud: Double? = null,
        longitud: Double? = null
    ) {
        viewModelScope.launch {
            val estadoInicial = if (canchaId == null) {
                EstadoPartido.PUBLICADO
            } else {
                EstadoPartido.PENDIENTE_RESERVA
            }

            val fechaHoraInicio =
                calcularFechaHoraInicioMillis(
                    fecha = fecha,
                    horario = horario
                )

            val nuevoPartido = Partido(
                id = System.currentTimeMillis().toString(),
                titulo = titulo,
                horario = horario,
                fecha = fecha,
                fechaHoraInicio = fechaHoraInicio,
                ubicacion = ubicacion,
                dificultad = dificultad,
                participantesActuales = 1,
                participantesMaximos = participantes,
                creador = creador,
                calificacionCreador = 5.0,
                descripcion = descripcion,
                usuariosAnotados = listOf(creador),
                canchaId = canchaId,
                nombreCancha = nombreCancha,
                latitud = latitud,
                longitud = longitud,
                estado = estadoInicial
            )

            repository.crearPartido(nuevoPartido)
            cargarPartidos()
        }
    }

    fun eliminarPartido(id: String) {
        viewModelScope.launch {
            repository.eliminarPartido(id)
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
    fun eliminarJugador(
        partidoId: String,
        jugadorAEliminar: String,
        usuarioSolicitante: String
    ) {
        viewModelScope.launch {
            repository.eliminarJugador(
                partidoId = partidoId,
                jugadorAEliminar = jugadorAEliminar,
                usuarioSolicitante = usuarioSolicitante
            )

            cargarPartidos()
        }
    }
    fun partidosDelUsuario(
        emailUsuario: String
    ): List<Partido> {
        return uiState.partidos.filter { partido ->
            partido.creador == emailUsuario ||
                    emailUsuario in partido.usuariosAnotados
        }
    }

}
