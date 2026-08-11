package com.example.futbolnomade.presentation.viewModel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futbolnomade.data.remote.notification.FcmNotificationSender
import com.example.futbolnomade.data.repository.ValoracionRepositoryImpl
import com.example.futbolnomade.domain.model.ValoracionPartido
import com.example.futbolnomade.domain.repository.ValoracionRepository
import kotlinx.coroutines.launch

class ValoracionViewModel(
    private val repository: ValoracionRepository =
        ValoracionRepositoryImpl()
) : ViewModel() {

    private val notificationSender = FcmNotificationSender()

    var valoracionesUsuario by mutableStateOf(
        emptyList<ValoracionPartido>()
    )
        private set

    var valoracionesRecibidas by mutableStateOf(
        emptyList<ValoracionPartido>()
    )
        private set

    var guardando by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun cargarValoracionesUsuario(
        emailUsuario: String
    ) {
        if (emailUsuario.isBlank()) {
            valoracionesUsuario = emptyList()
            valoracionesRecibidas = emptyList()
            return
        }

        viewModelScope.launch {
            valoracionesUsuario =
                repository.obtenerValoracionesDeUsuario(
                    emailUsuario
                )
            
            valoracionesRecibidas =
                repository.obtenerValoracionesSobreUsuario(
                    emailUsuario
                )
        }
    }

    fun obtenerPromedioOrganizador(emailUsuario: String): Double {
        val puntuaciones = valoracionesRecibidas
            .filter { it.organizadorEmail == emailUsuario }
            .map { it.puntuacionOrganizador }
        
        return if (puntuaciones.isEmpty()) 0.0 else puntuaciones.average()
    }

    fun obtenerPromedioJugador(emailUsuario: String): Double {
        val puntuaciones = valoracionesRecibidas
            .mapNotNull { v -> 
                v.valoracionesJugadores.find { it.jugadorEmail == emailUsuario }?.puntuacion 
            }
        
        return if (puntuaciones.isEmpty()) 0.0 else puntuaciones.average()
    }

    fun obtenerCantidadValoracionesOrganizador(emailUsuario: String): Int {
        return valoracionesRecibidas.count { it.organizadorEmail == emailUsuario }
    }

    fun obtenerCantidadValoracionesJugador(emailUsuario: String): Int {
        return valoracionesRecibidas.count { v -> 
            v.valoracionesJugadores.any { it.jugadorEmail == emailUsuario } 
        }
    }

    fun yaValoro(
        partidoId: String,
        emailUsuario: String
    ): Boolean {
        return valoracionesUsuario.any { valoracion ->
            valoracion.partidoId == partidoId &&
                    valoracion.autorEmail
                        .trim()
                        .equals(
                            emailUsuario.trim(),
                            ignoreCase = true
                        )
        }
    }
    fun guardarValoracion(
        context: Context,
        valoracion: ValoracionPartido,
        onResultado: (Boolean) -> Unit
    ) {
        if (guardando) {
            return
        }

        viewModelScope.launch {
            guardando = true
            error = null

            val guardada =
                repository.guardarValoracion(
                    valoracion
                )

            if (guardada) {
                valoracionesUsuario =
                    valoracionesUsuario + valoracion

                // Notificar al organizador
                val oUid = valoracion.organizadorUid
                if (!oUid.isNullOrBlank() && valoracion.autorEmail != valoracion.organizadorEmail) {
                    notificationSender.enviarNotificacionAUsuario(
                        context = context,
                        uid = oUid,
                        fallbackEmail = valoracion.organizadorEmail,
                        titulo = "¡Nueva valoración! ⭐",
                        mensaje = "Recibiste una valoración por tu labor como organizador.",
                        data = mapOf("tipo" to "VALORACION_RECIBIDA")
                    )
                }

                // Notificar a cada jugador valorado
                valoracion.valoracionesJugadores.forEach { vJugador ->
                    if (vJugador.jugadorUid.isNotBlank() && vJugador.jugadorEmail != valoracion.autorEmail) {
                        notificationSender.enviarNotificacionAUsuario(
                            context = context,
                            uid = vJugador.jugadorUid,
                            fallbackEmail = vJugador.jugadorEmail,
                            titulo = "¡Nueva valoración! ⭐",
                            mensaje = "Recibiste una valoración por tu desempeño en el partido.",
                            data = mapOf("tipo" to "VALORACION_RECIBIDA")
                        )
                    }
                }

            } else {
                error =
                    "No se pudo guardar. Es posible que ya hayas valorado este partido."
            }

            guardando = false
            onResultado(guardada)
        }
    }

    fun limpiarError() {
        error = null
    }
}