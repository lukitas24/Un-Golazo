package com.example.futbolnomade.presentation.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futbolnomade.data.repository.ValoracionRepositoryImpl
import com.example.futbolnomade.domain.model.ValoracionPartido
import com.example.futbolnomade.domain.repository.ValoracionRepository
import kotlinx.coroutines.launch

class ValoracionViewModel(
    private val repository: ValoracionRepository =
        ValoracionRepositoryImpl()
) : ViewModel() {

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