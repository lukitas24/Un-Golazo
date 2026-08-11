package com.example.futbolnomade.presentation.viewModel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futbolnomade.data.remote.notification.FcmNotificationSender
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

    private val notificationSender = FcmNotificationSender()

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

    fun partidosDelUsuario(emailUsuario: String): List<Partido> {
        val emailNormalizado = emailUsuario.trim()
        return uiState.partidos.filter { partido ->
            val esCreador = partido.creador.trim().equals(emailNormalizado, ignoreCase = true)
            val estaAnotado = partido.usuariosAnotados.any { it.trim().equals(emailNormalizado, ignoreCase = true) }
            esCreador || estaAnotado
        }
    }

    fun partidosVisibles(): List<Partido> {
        return uiState.partidos.filter {
            it.estado == EstadoPartido.PUBLICADO || it.estado == EstadoPartido.RESERVA_APROBADA
        }
    }

    fun crearPartido(
        context: Context,
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
        propietarioCancha: String? = null,
        propietarioCanchaUid: String? = null
    ) {
        viewModelScope.launch {
            val esDuenioCancha = propietarioCancha?.trim()?.equals(creador.trim(), ignoreCase = true) == true
            val estadoInicial = when {
                canchaId == null -> EstadoPartido.PUBLICADO
                esDuenioCancha -> EstadoPartido.RESERVA_APROBADA
                else -> EstadoPartido.PENDIENTE_RESERVA
            }

            val fechaHoraInicio = calcularFechaHoraInicioMillis(fecha = fecha, horario = horario)
            val partidoId = System.currentTimeMillis().toString()

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
                creadorUid = creadorUid,
                usuariosAnotados = listOf(creador),
                usuariosAnotadosUids = listOf(creadorUid),
                calificacionCreador = 5.0,
                descripcion = descripcion,
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
                    usuarioUid = creadorUid,
                    usuarioEmail = creador,
                    usuarioNombre = creador,
                    propietarioCanchaUid = propietarioCanchaUid.orEmpty(),
                    propietarioCanchaEmail = propietarioCancha.orEmpty(),
                    fecha = fecha,
                    hora = horario,
                    estado = if (esDuenioCancha) "Confirmada" else "Pendiente",
                    partidoId = partidoId
                )

                val rid = reservaRepository.crearReserva(reserva)
                
                if (!esDuenioCancha && (!propietarioCanchaUid.isNullOrBlank() || !propietarioCancha.isNullOrBlank())) {
                    Log.i("NOTIFICACION_CHECK", "🏟️ Notificando al dueño de la cancha sobre el nuevo partido...")
                    notificationSender.enviarNotificacionAUsuario(
                        context = context,
                        uid = propietarioCanchaUid.orEmpty(),
                        fallbackEmail = propietarioCancha,
                        titulo = "Nueva Solicitud de Reserva 🏟️",
                        mensaje = "$creador quiere reservar en $nombreCancha para un partido.",
                        data = mapOf("tipo" to "NUEVA_SOLICITUD", "reservaId" to rid, "partidoId" to partidoId)
                    )
                }
            }
            cargarPartidos()
        }
    }

    fun anotarseAPartido(context: Context, partidoId: String, usuario: String, usuarioUid: String) {
        viewModelScope.launch {
            val partido = uiState.partidos.find { it.id == partidoId }
            val exito = repository.anotarseAPartido(partidoId, usuario, usuarioUid)

            if (exito && partido != null) {
                val destinatarioUid = partido.creadorUid
                val destinatarioEmail = partido.creador
                
                if (destinatarioUid != usuarioUid) {
                    Log.i("NOTIFICACION_CHECK", "🏃 Jugador se anotó. Notificando al creador ($destinatarioEmail)...")
                    notificationSender.enviarNotificacionAUsuario(
                        context = context,
                        uid = destinatarioUid,
                        fallbackEmail = destinatarioEmail,
                        titulo = "Nuevo jugador en tu partido ⚽",
                        mensaje = "$usuario se anotó a \"${partido.titulo}\".",
                        data = mapOf("tipo" to "NUEVO_JUGADOR", "partidoId" to partidoId)
                    )
                }
            }
            cargarPartidos()
        }
    }

    fun cancelarInscripcion(context: Context, partidoId: String, usuario: String, usuarioUid: String) {
        viewModelScope.launch {
            val partido = uiState.partidos.find { it.id == partidoId }
            val exito = repository.cancelarInscripcion(partidoId, usuario, usuarioUid)

            if (exito && partido != null) {
                val destinatarioUid = partido.creadorUid
                val destinatarioEmail = partido.creador

                if (destinatarioUid != usuarioUid) {
                    Log.i("NOTIFICACION_CHECK", "🏃 Un jugador se bajó. Notificando al creador ($destinatarioEmail)...")
                    notificationSender.enviarNotificacionAUsuario(
                        context = context,
                        uid = destinatarioUid,
                        fallbackEmail = destinatarioEmail,
                        titulo = "Un jugador se bajó del partido 🏃",
                        mensaje = "$usuario ya no participará de \"${partido.titulo}\".",
                        data = mapOf("tipo" to "JUGADOR_ABANDONO", "partidoId" to partidoId)
                    )
                }
            }
            cargarPartidos()
        }
    }

    fun eliminarJugador(context: Context, partidoId: String, jugadorAEliminar: String, usuarioSolicitante: String) {
        viewModelScope.launch {
            val partido = uiState.partidos.find { it.id == partidoId }
            var uidExpulsado = ""
            partido?.let { p ->
                val index = p.usuariosAnotados.indexOf(jugadorAEliminar)
                if (index >= 0) uidExpulsado = p.usuariosAnotadosUids.getOrNull(index).orEmpty()
            }

            val exito = repository.eliminarJugador(partidoId, jugadorAEliminar, usuarioSolicitante)

            if (exito && partido != null) {
                Log.i("NOTIFICACION_CHECK", "🚫 Jugador expulsado. Notificando al jugador ($jugadorAEliminar)...")
                notificationSender.enviarNotificacionAUsuario(
                    context = context,
                    uid = uidExpulsado,
                    fallbackEmail = jugadorAEliminar,
                    titulo = "Ya no participás de este partido ⚽",
                    mensaje = "El organizador te quitó de \"${partido.titulo}\".",
                    data = mapOf("tipo" to "ELIMINADO_DE_PARTIDO", "partidoId" to partidoId)
                )
            }
            cargarPartidos()
        }
    }

    fun actualizarEstadoPartido(partidoId: String, nuevoEstado: EstadoPartido) {
        viewModelScope.launch {
            repository.actualizarEstadoPartido(partidoId, nuevoEstado)
            cargarPartidos()
        }
    }

    fun actualizarPartido(context: Context, partido: Partido) {
        viewModelScope.launch {
            val partidoAntes = uiState.partidos.find { it.id == partido.id }
            repository.actualizarPartido(partido)

            if (partidoAntes != null) {
                val cambios = mutableListOf<String>()
                if (partidoAntes.fecha != partido.fecha) cambios.add("fecha")
                if (partidoAntes.horario != partido.horario) cambios.add("horario")
                if (partidoAntes.ubicacion.trim() != partido.ubicacion.trim()) cambios.add("ubicación")

                if (cambios.isNotEmpty()) {
                    val destinatarios = partidoAntes.usuariosAnotadosUids.filter { it.isNotBlank() && it != partidoAntes.creadorUid }
                    val destinatariosEmails = partidoAntes.usuariosAnotados.filter { it != partidoAntes.creador }

                    Log.i("NOTIFICACION_CHECK", "⚙️ Partido modificado. Notificando a ${destinatarios.size} participantes...")

                    val cambiosTexto = cambios.joinToString(", ")

                    // Notificar a cada participante (excepto al creador)
                    destinatarios.zip(destinatariosEmails).forEach { (uid, email) ->
                        notificationSender.enviarNotificacionAUsuario(
                            context = context,
                            uid = uid,
                            fallbackEmail = email,
                            titulo = "Cambio en tu partido ⚽",
                            mensaje = "Se modificó la $cambiosTexto en \"${partido.titulo}\".",
                            data = mapOf("tipo" to "PARTIDO_MODIFICADO", "partidoId" to partido.id)
                        )
                    }
                }
            }
            cargarPartidos()
        }
    }

    fun eliminarPartido(context: Context, id: String) {
        viewModelScope.launch {
            val partido = uiState.partidos.find { it.id == id }

            if (partido != null) {
                val destinatarios = partido.usuariosAnotadosUids.filter { it.isNotBlank() && it != partido.creadorUid }
                val destinatariosEmails = partido.usuariosAnotados.filter { it != partido.creador }

                Log.i("NOTIFICACION_CHECK", "🗑️ Partido eliminado. Notificando a ${destinatarios.size} participantes...")

                destinatarios.zip(destinatariosEmails).forEach { (uid, email) ->
                    notificationSender.enviarNotificacionAUsuario(
                        context = context,
                        uid = uid,
                        fallbackEmail = email,
                        titulo = "Partido cancelado ❌",
                        mensaje = "El organizador canceló el partido \"${partido.titulo}\".",
                        data = mapOf("tipo" to "PARTIDO_CANCELADO")
                    )
                }
            }

            repository.eliminarPartido(id)
            cargarPartidos()
        }
    }
}
