package com.example.futbolnomade.presentation.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futbolnomade.data.repository.PartidoRepositoryImpl
import com.example.futbolnomade.data.repository.ReservaRepositoryImpl
import com.example.futbolnomade.domain.model.EstadoPartido
import com.example.futbolnomade.domain.model.Partido
import com.example.futbolnomade.domain.model.Reserva
import com.example.futbolnomade.domain.model.calcularFechaHoraInicioMillis
import com.example.futbolnomade.domain.repository.PartidoRepository
import com.example.futbolnomade.domain.repository.ReservaRepository
import com.example.futbolnomade.presentation.state.PartidoUiState
import kotlinx.coroutines.launch

class PartidoViewModel(
    private val repository: PartidoRepository = PartidoRepositoryImpl(),
    private val reservaRepository: ReservaRepository = ReservaRepositoryImpl()
) : ViewModel() {

    var uiState by mutableStateOf(PartidoUiState())
        private set

    init {
        cargarPartidos()
    }

    fun cargarPartidos() {
        viewModelScope.launch {
            val partidos = repository.obtenerPartidos()

            uiState = uiState.copy(
                partidos = partidos
            )
        }
    }

    /*
     * Se mantiene esta función por si otra pantalla
     * todavía utiliza misPartidos().
     *
     * Ahora devuelve tanto los partidos creados por el usuario
     * como aquellos en los que está anotado.
     */
    fun misPartidos(
        emailUsuario: String
    ): List<Partido> {
        return partidosDelUsuario(emailUsuario)
    }

    /*
     * Devuelve:
     *
     * 1. Los partidos que creó el usuario.
     * 2. Los partidos en los que se anotó.
     *
     * Se ignoran mayúsculas, minúsculas y espacios.
     */
    fun partidosDelUsuario(
        emailUsuario: String
    ): List<Partido> {
        val emailNormalizado = emailUsuario
            .trim()

        return uiState.partidos.filter { partido ->

            val esCreador = partido.creador
                .trim()
                .equals(
                    emailNormalizado,
                    ignoreCase = true
                )

            val estaAnotado = partido.usuariosAnotados.any { usuarioAnotado ->
                usuarioAnotado
                    .trim()
                    .equals(
                        emailNormalizado,
                        ignoreCase = true
                    )
            }

            esCreador || estaAnotado
        }
    }

    /*
     * Estos son los partidos que pueden mostrarse
     * públicamente para que otros usuarios se anoten.
     */
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
        longitud: Double? = null,
        propietarioCancha: String? = null
    ) {
        viewModelScope.launch {

            val esDuenioCancha =
                propietarioCancha
                    ?.trim()
                    ?.equals(
                        creador.trim(),
                        ignoreCase = true
                    ) == true

            val estadoInicial = when {
                canchaId == null -> {
                    EstadoPartido.PUBLICADO
                }

                esDuenioCancha -> {
                    EstadoPartido.RESERVA_APROBADA
                }

                else -> {
                    EstadoPartido.PENDIENTE_RESERVA
                }
            }

            val fechaHoraInicio = calcularFechaHoraInicioMillis(
                fecha = fecha,
                horario = horario
            )

            val partidoId = System
                .currentTimeMillis()
                .toString()

            val nuevoPartido = Partido(
                id = partidoId,
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

            if (canchaId != null) {
                val reserva = Reserva(
                    canchaId = canchaId,
                    canchaNombre = nombreCancha ?: "",
                    usuarioId = creador,
                    usuarioNombre = creador,
                    fecha = fecha,
                    hora = horario,
                    estado = if (esDuenioCancha) {
                        "Confirmada"
                    } else {
                        "Pendiente"
                    },
                    partidoId = partidoId
                )

                reservaRepository.crearReserva(reserva)
            }

            cargarPartidos()
        }
    }

    fun eliminarPartido(
        id: String
    ) {
        viewModelScope.launch {
            repository.eliminarPartido(id)
            cargarPartidos()
        }
    }

    fun anotarseAPartido(
        partidoId: String,
        usuario: String
    ) {
        viewModelScope.launch {
            repository.anotarseAPartido(
                partidoId = partidoId,
                usuario = usuario
            )

            cargarPartidos()
        }
    }

    fun cancelarInscripcion(
        partidoId: String,
        usuario: String
    ) {
        viewModelScope.launch {
            repository.cancelarInscripcion(
                partidoId = partidoId,
                usuario = usuario
            )

            cargarPartidos()
        }
    }

    fun actualizarEstadoPartido(
        partidoId: String,
        nuevoEstado: EstadoPartido
    ) {
        viewModelScope.launch {
            val partido = uiState.partidos.find {
                it.id == partidoId
            } ?: return@launch

            repository.crearPartido(
                partido.copy(
                    estado = nuevoEstado
                )
            )

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
}