package com.example.futbolnomade.presentation.viewModel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futbolnomade.data.remote.notification.FcmNotificationSender
import com.example.futbolnomade.data.repository.ReservaRepositoryImpl
import com.example.futbolnomade.domain.model.Reserva
import com.example.futbolnomade.domain.repository.ReservaRepository
import kotlinx.coroutines.launch

data class ReservaUiState(
    val reservas: List<Reserva> = emptyList(),
    val isLoading: Boolean = false
)

class ReservaViewModel : ViewModel() {

    private val repository: ReservaRepository = ReservaRepositoryImpl()
    private val notificationSender = FcmNotificationSender()
    
    var uiState by mutableStateOf(ReservaUiState())
        private set

    fun cargarReservas(usuarioId: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            try {
                repository.obtenerReservasPorUsuario(usuarioId).collect { lista ->
                    uiState = uiState.copy(reservas = lista, isLoading = false)
                }
            } catch (e: Exception) {
                Log.e("NOTIFICACION_CHECK", "Error cargando reservas usuario", e)
                uiState = uiState.copy(isLoading = false)
            }
        }
    }

    fun cargarReservasPorCancha(canchaId: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            try {
                repository.obtenerReservasPorCancha(canchaId).collect { lista ->
                    val otrasReservas = uiState.reservas.filter { it.canchaId != canchaId }
                    uiState = uiState.copy(
                        reservas = (otrasReservas + lista).distinctBy { it.id },
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("NOTIFICACION_CHECK", "Error cargando reservas cancha", e)
                uiState = uiState.copy(isLoading = false)
            }
        }
    }

    fun crearReserva(context: Context, reserva: Reserva) {
        viewModelScope.launch {
            try {
                Log.i("NOTIFICACION_CHECK", "1. Guardando reserva en DB...")
                val reservaId = repository.crearReserva(reserva)
                
                if (reservaId.isBlank()) {
                    Log.e("NOTIFICACION_CHECK", "❌ Error: No se pudo guardar la reserva en Firestore.")
                    return@launch
                }

                Log.i("NOTIFICACION_CHECK", "2. Reserva guardada con ID: $reservaId")
                
                if (reserva.estado == "Pendiente") {
                    val uidDestino = reserva.propietarioCanchaUid
                    val emailDestino = reserva.propietarioCanchaEmail
                    
                    Log.i("NOTIFICACION_CHECK", "3. Intentando notificar al dueño ($emailDestino)...")
                    notificationSender.enviarNotificacionAUsuario(
                        context = context,
                        uid = uidDestino,
                        fallbackEmail = emailDestino,
                        titulo = "Nueva Solicitud de Reserva 🏟️",
                        mensaje = "${reserva.usuarioNombre} quiere reservar en ${reserva.canchaNombre}",
                        data = mapOf(
                            "tipo" to "NUEVA_SOLICITUD",
                            "reservaId" to reservaId,
                            "canchaId" to reserva.canchaId
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("NOTIFICACION_CHECK", "❌ Error fatal en flujo de reserva", e)
            }
        }
    }

    fun responderReserva(context: Context, reserva: Reserva, nuevoEstado: String) {
        viewModelScope.launch {
            try {
                Log.i("NOTIFICACION_CHECK", "1. Actualizando estado a: $nuevoEstado")
                repository.actualizarEstadoReserva(reserva.id, nuevoEstado)
                
                val destinatarioUid = reserva.usuarioUid
                val destinatarioEmail = reserva.usuarioEmail.ifBlank { reserva.usuarioId }

                Log.i("NOTIFICACION_CHECK", "2. Notificando resultado al jugador ($destinatarioEmail)...")
                val titulo = if (nuevoEstado == "Confirmada") "¡Reserva Aprobada! ⚽" else "Reserva Rechazada ❌"
                val mensaje = if (nuevoEstado == "Confirmada") 
                    "Tu turno en ${reserva.canchaNombre} ha sido confirmado." else 
                    "Lo sentimos, tu turno en ${reserva.canchaNombre} no fue aceptado."

                notificationSender.enviarNotificacionAUsuario(
                    context = context,
                    uid = destinatarioUid,
                    fallbackEmail = destinatarioEmail,
                    titulo = titulo,
                    mensaje = mensaje,
                    data = mapOf(
                        "tipo" to "RESERVA_STATUS",
                        "reservaId" to reserva.id,
                        "estado" to nuevoEstado
                    )
                )
            } catch (e: Exception) {
                Log.e("NOTIFICACION_CHECK", "❌ Error fatal al responder reserva", e)
            }
        }
    }
}
