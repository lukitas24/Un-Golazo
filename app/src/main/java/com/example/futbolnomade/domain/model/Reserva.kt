package com.example.futbolnomade.domain.model

data class Reserva(
    val id: String = "",
    val canchaId: String = "",
    val canchaNombre: String = "",
    val usuarioId: String = "",
    val usuarioNombre: String = "",
    val fecha: String = "",
    val hora: String = "",
    val estado: String = "Pendiente", // Pendiente, Confirmada, Rechazada, Cancelada
    val partidoId: String? = null
)
