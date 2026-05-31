package com.example.futbolnomade.presentation.state

import com.example.futbolnomade.domain.model.Cancha

data class CanchaUiState(
    val canchas: List<Cancha> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)