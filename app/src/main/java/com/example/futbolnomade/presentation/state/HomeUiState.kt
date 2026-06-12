package com.example.futbolnomade.presentation.state

data class PartidoResumen(
    val id: String,
    val titulo: String,
    val horario: String,
    val fecha: String,
    val anfitrion: String,
    val rating: Float,
    val imageRes: Int? = null
)

data class HomeUiState(
    val nombreUsuario: String = "",
    val emailUsuario: String = "",
    val proximosPartidos: List<PartidoResumen> = emptyList(),
    val isLoading: Boolean = false
)

