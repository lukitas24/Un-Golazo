package com.example.futbolnomade.domain.model

data class Jugador(
    val id: String = "",
    val nombre: String = "",
    val email: String = "",
    val telefono: String = "",
    val posicion: String = "",
    val rating: Double = 0.0,
    val partidosJugados: Int = 0,
    val fotoUrl: String? = null
)
