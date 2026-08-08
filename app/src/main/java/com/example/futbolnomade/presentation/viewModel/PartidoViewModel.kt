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
    private val repository:
    PartidoRepository =
        PartidoRepositoryImpl(),

    private val reservaRepository:
    ReservaRepository =
        ReservaRepositoryImpl()
) : ViewModel() {

    var uiState by mutableStateOf(
        PartidoUiState()
    )
        private set

    init {
        cargarPartidos()
    }

    fun cargarPartidos() {
        viewModelScope.launch {
            val partidos =
                repository.obtenerPartidos()

            uiState =
                uiState.copy(
                    partidos = partidos
                )
        }
    }

    fun misPartidos(
        emailUsuario: String
    ): List<Partido> {
        return partidosDelUsuario(
            emailUsuario
        )
    }

    fun partidosDelUsuario(
        emailUsuario: String
    ): List<Partido> {

        val emailNormalizado =
            emailUsuario.trim()

        return uiState.partidos.filter {
                partido ->

            val esCreador =
                partido.creador
                    .trim()
                    .equals(
                        emailNormalizado,
                        ignoreCase = true
                    )

            val estaAnotado =
                partido.usuariosAnotados.any {
                        usuarioAnotado ->

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

    fun partidosVisibles(): List<Partido> {
        return uiState.partidos.filter {
                partido ->

            partido.estado ==
                    EstadoPartido.PUBLICADO ||
                    partido.estado ==
                    EstadoPartido.RESERVA_APROBADA
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
        creadorUid: String,

        canchaId: String? = null,
        nombreCancha: String? = null,
        latitud: Double? = null,
        longitud: Double? = null,

        /*
         * Datos del propietario de la cancha seleccionada.
         */
        propietarioCancha: String? = null,
        propietarioCanchaUid: String? = null
    ) {
        viewModelScope.launch {

            val esDuenioCancha =
                propietarioCancha
                    ?.trim()
                    ?.equals(
                        creador.trim(),
                        ignoreCase = true
                    ) == true

            val estadoInicial =
                when {
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

            val fechaHoraInicio =
                calcularFechaHoraInicioMillis(
                    fecha = fecha,
                    horario = horario
                )

            val partidoId =
                System
                    .currentTimeMillis()
                    .toString()

            val nuevoPartido =
                Partido(
                    id = partidoId,

                    titulo = titulo,
                    horario = horario,
                    fecha = fecha,

                    fechaHoraInicio =
                        fechaHoraInicio,

                    ubicacion = ubicacion,
                    dificultad = dificultad,

                    participantesActuales = 1,

                    participantesMaximos =
                        participantes,

                    creador = creador,
                    creadorUid = creadorUid,

                    usuariosAnotados =
                        listOf(
                            creador
                        ),

                    usuariosAnotadosUids =
                        listOf(
                            creadorUid
                        ),

                    calificacionCreador = 5.0,
                    descripcion = descripcion,

                    canchaId = canchaId,
                    nombreCancha = nombreCancha,
                    latitud = latitud,
                    longitud = longitud,

                    estado = estadoInicial
                )

            repository.crearPartido(
                nuevoPartido
            )

            if (canchaId != null) {
                val reserva =
                    Reserva(
                        canchaId =
                            canchaId,

                        canchaNombre =
                            nombreCancha ?: "",

                        usuarioId =
                            creador,

                        usuarioUid =
                            creadorUid,

                        usuarioEmail =
                            creador,

                        usuarioNombre =
                            creador,

                        /*
                         * Necesarios para avisar al dueño que
                         * recibió una nueva solicitud.
                         */
                        propietarioCanchaUid =
                            propietarioCanchaUid
                                .orEmpty(),

                        propietarioCanchaEmail =
                            propietarioCancha
                                .orEmpty(),

                        fecha =
                            fecha,

                        hora =
                            horario,

                        estado =
                            if (esDuenioCancha) {
                                "Confirmada"
                            } else {
                                "Pendiente"
                            },

                        partidoId =
                            partidoId
                    )

                reservaRepository
                    .crearReserva(
                        reserva
                    )
            }

            cargarPartidos()
        }
    }

    fun actualizarPartido(
        partido: Partido
    ) {
        viewModelScope.launch {
            repository.actualizarPartido(
                partido
            )

            cargarPartidos()
        }
    }

    fun eliminarPartido(
        id: String
    ) {
        viewModelScope.launch {
            repository.eliminarPartido(
                id
            )

            cargarPartidos()
        }
    }

    fun anotarseAPartido(
        partidoId: String,
        usuario: String,
        usuarioUid: String
    ) {
        viewModelScope.launch {
            repository.anotarseAPartido(
                partidoId = partidoId,
                usuario = usuario,
                usuarioUid = usuarioUid
            )

            cargarPartidos()
        }
    }

    fun cancelarInscripcion(
        partidoId: String,
        usuario: String,
        usuarioUid: String
    ) {
        viewModelScope.launch {
            repository.cancelarInscripcion(
                partidoId = partidoId,
                usuario = usuario,
                usuarioUid = usuarioUid
            )

            cargarPartidos()
        }
    }

    fun actualizarEstadoPartido(
        partidoId: String,
        nuevoEstado: EstadoPartido
    ) {
        viewModelScope.launch {
            repository
                .actualizarEstadoPartido(
                    partidoId =
                        partidoId,

                    nuevoEstado =
                        nuevoEstado
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

                jugadorAEliminar =
                    jugadorAEliminar,

                usuarioSolicitante =
                    usuarioSolicitante
            )

            cargarPartidos()
        }
    }
}