package com.example.futbolnomade.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futbolnomade.data.repository.PartidoRepositoryImpl
import com.example.futbolnomade.domain.model.Partido
import com.example.futbolnomade.domain.repository.PartidoRepository
import com.example.futbolnomade.presentation.state.HomeUiState
import com.example.futbolnomade.presentation.state.PartidoResumen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: PartidoRepository = PartidoRepositoryImpl()) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun inicializar(nombreUsuario: String, emailUsuario: String) {
        _uiState.value = _uiState.value.copy(
            nombreUsuario = nombreUsuario,
            emailUsuario = emailUsuario
        )
        cargarPartidos()
    }

    private fun cargarPartidos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val partidos = repository.obtenerPartidos().map { it.toResumen() }
            _uiState.value = _uiState.value.copy(
                proximosPartidos = partidos,
                isLoading = false
            )
        }
    }

    private fun Partido.toResumen() = PartidoResumen(
        id = id,
        titulo = titulo,
        horario = horario,
        fecha = fecha,
        anfitrion = creador,
        rating = calificacionCreador.toFloat()
    )
}
