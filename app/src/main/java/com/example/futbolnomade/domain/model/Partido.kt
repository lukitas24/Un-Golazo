package com.example.futbolnomade.domain.model

enum class EstadoPartido {
    PUBLICADO,
    PENDIENTE_RESERVA,
    RESERVA_APROBADA,
    RESERVA_RECHAZADA
}

data class Partido(
    val id: String = "",
    val titulo: String = "",
    val horario: String = "",
    val fecha: String = "",
    val ubicacion: String = "",
    val dificultad: String = "",
    val participantesActuales: Int = 0,
    val participantesMaximos: Int = 0,
    val creador: String = "",
    val calificacionCreador: Double = 0.0,
    val descripcion: String = "",
    val usuariosAnotados: List<String> = emptyList(),

    val canchaId: Int? = null,
    val nombreCancha: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val estado: EstadoPartido = EstadoPartido.PUBLICADO
)