package com.example.futbolnomade.presentation.state

import com.example.futbolnomade.domain.model.Partido

data class PartidoUiState(
    val partidos: List<Partido> = emptyList()
)