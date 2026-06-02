package com.example.futbolnomade.presentation.viewModel

import androidx.lifecycle.ViewModel
import com.example.futbolnomade.presentation.state.HomeUiState
import com.example.futbolnomade.presentation.state.PartidoResumen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun inicializar(nombreUsuario: String, emailUsuario: String) {
        _uiState.value = _uiState.value.copy(
            nombreUsuario = nombreUsuario,
            emailUsuario = emailUsuario,
            proximosPartidos = cargarPartidosFake()
        )
    }

    private fun cargarPartidosFake(): List<PartidoResumen> = listOf(
        PartidoResumen(
            id = 1,
            titulo = "Cancha Maracana 19:00hs",
            horario = "19:00hs",
            fecha = "23/03",
            anfitrion = "Tomas Gutierrez",
            rating = 4.9f
        ),
        PartidoResumen(
            id = 2,
            titulo = "Cancha San Martin 132 19:00",
            horario = "19:00hs",
            fecha = "01/04",
            anfitrion = "Esteban Quito",
            rating = 3.9f
        )
    )
}