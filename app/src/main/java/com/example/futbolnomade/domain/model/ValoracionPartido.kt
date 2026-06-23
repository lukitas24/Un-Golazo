package com.example.futbolnomade.domain.model

data class ValoracionJugador(
    val jugadorEmail: String = "",
    val puntuacion: Int = 0
)

data class ValoracionPartido(
    val id: String = "",
    val partidoId: String = "",
    val autorEmail: String = "",

    val valoracionesJugadores: List<ValoracionJugador> =
        emptyList(),

    val organizadorEmail: String? = null,
    val puntuacionOrganizador: Int = 0,

    val canchaId: String? = null,
    val puntuacionCancha: Int = 0,

    val fechaCreacion: Long = 0L
)