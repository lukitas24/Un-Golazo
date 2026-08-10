package com.example.futbolnomade.presentation.viewModel

import android.content.Context
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
                e.printStackTrace()
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
                e.printStackTrace()
                uiState = uiState.copy(isLoading = false)
            }
        }
    }

    fun crearReserva(context: Context, reserva: Reserva) {
        viewModelScope.launch {
            val reservaId = repository.crearReserva(reserva)
            
            // Notificar al dueño de la cancha (Gratis, sin Plan Blaze)
            if (reserva.propietarioCanchaUid.isNotBlank() && reserva.estado == "Pendiente") {
                notificationSender.enviarNotificacionAUsuario(
                    context = context,
                    uid = reserva.propietarioCanchaUid,
                    titulo = "Nueva Solicitud de Reserva 🏟️",
                    mensaje = "${reserva.usuarioNombre} quiere reservar en ${reserva.canchaNombre}",
                    data = mapOf(
                        "tipo" to "NUEVA_SOLICITUD",
                        "reservaId" to reservaId,
                        "canchaId" to reserva.canchaId
                    )
                )
            }
        }
    }

    fun cancelarReserva(reservaId: String) {
        viewModelScope.launch {
            repository.cancelarReserva(reservaId)
        }
    }

    fun responderReserva(context: Context, reserva: Reserva, nuevoEstado: String) {
        viewModelScope.launch {
            repository.actualizarEstadoReserva(reserva.id, nuevoEstado)
            
            if (reserva.usuarioUid.isNotBlank()) {
                val titulo = if (nuevoEstado == "Confirmada") "¡Reserva Aprobada! ⚽" else "Reserva Rechazada ❌"
                val mensaje = if (nuevoEstado == "Confirmada") 
                    "Tu turno en ${reserva.canchaNombre} ha sido confirmado." else 
                    "Lo sentimos, tu turno en ${reserva.canchaNombre} no fue aceptado."

                notificationSender.enviarNotificacionAUsuario(
                    context = context,
                    uid = reserva.usuarioUid,
                    titulo = titulo,
                    mensaje = mensaje,
                    data = mapOf(
                        "tipo" to "RESERVA_STATUS",
                        "reservaId" to reserva.id,
                        "estado" to nuevoEstado
                    )
                )
            }
        }
    }
}
