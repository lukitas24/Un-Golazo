package com.example.futbolnomade.presentation.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futbolnomade.data.repository.ReservaRepositoryImpl
import com.example.futbolnomade.domain.model.Reserva
import com.example.futbolnomade.domain.repository.ReservaRepository
import kotlinx.coroutines.launch

data class ReservaUiState(
    val reservas: List<Reserva> = emptyList(),
    val isLoading: Boolean = false
)

class ReservaViewModel(
    private val repository: ReservaRepository = ReservaRepositoryImpl()
) : ViewModel() {

    var uiState by mutableStateOf(ReservaUiState())
        private set

    fun cargarReservas(usuarioId: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            repository.obtenerReservasPorUsuario(usuarioId).collect { lista ->
                uiState = uiState.copy(reservas = lista, isLoading = false)
            }
        }
    }

    fun crearReserva(reserva: Reserva) {
        viewModelScope.launch {
            repository.crearReserva(reserva)
        }
    }

    fun cancelarReserva(reservaId: String) {
        viewModelScope.launch {
            repository.cancelarReserva(reservaId)
        }
    }
}
