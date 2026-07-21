package com.example.futbolnomade.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futbolnomade.data.repository.PartidoRepositoryImpl
import com.example.futbolnomade.domain.model.EstadoPartido
import com.example.futbolnomade.domain.model.Partido
import com.example.futbolnomade.domain.model.obtenerFechaHoraInicioMillis
import com.example.futbolnomade.domain.repository.PartidoRepository
import com.example.futbolnomade.presentation.state.HomeUiState
import com.example.futbolnomade.presentation.state.PartidoResumen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: PartidoRepository = PartidoRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun inicializar(
        nombreUsuario: String,
        emailUsuario: String
    ) {
        _uiState.value = _uiState.value.copy(
            nombreUsuario = nombreUsuario,
            emailUsuario = emailUsuario
        )

        cargarPartidos()
    }

    private fun cargarPartidos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true
            )

            try {
                val emailUsuarioActual = _uiState.value.emailUsuario
                    .trim()
                    .lowercase()

                val momentoActual = System.currentTimeMillis()

                val partidos = repository.obtenerPartidos()
                    .filter { partido ->
                        val inicioMillis = partido.obtenerFechaHoraInicioMillis()

                        val estadoValido =
                            partido.estado == EstadoPartido.PUBLICADO ||
                                    partido.estado == EstadoPartido.RESERVA_APROBADA

                        val usuarioInvolucrado = emailUsuarioActual.isNotBlank() && (
                                partido.creador.trim().lowercase() == emailUsuarioActual ||
                                        partido.usuariosAnotados.any {
                                            it.trim().lowercase() == emailUsuarioActual
                                        }
                                )

                        val partidoTodaviaNoOcurrio =
                            inicioMillis > momentoActual

                        estadoValido && usuarioInvolucrado && partidoTodaviaNoOcurrio
                    }
                    .sortedBy { partido ->
                        partido.obtenerFechaHoraInicioMillis()
                    }
                    .map { partido ->
                        partido.toResumen()
                    }

                _uiState.value = _uiState.value.copy(
                    proximosPartidos = partidos,
                    isLoading = false
                )
            } catch (e: Exception) {
                e.printStackTrace()

                _uiState.value = _uiState.value.copy(
                    proximosPartidos = emptyList(),
                    isLoading = false
                )
            }
        }
    }

    private fun Partido.toResumen(): PartidoResumen {
        return PartidoResumen(
            id = id,
            titulo = titulo,
            horario = horario,
            fecha = fecha,
            anfitrion = creador,
            rating = calificacionCreador.toFloat()
        )
    }
}