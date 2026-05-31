package com.example.futbolnomade.domain.model

data class Partido(
    val id: Int,
    val titulo: String,
    val horario: String,
    val fecha: String,
    val ubicacion: String,
    val dificultad: String,
    val participantesActuales: Int,
    val participantesMaximos: Int,
    val creador: String,
    val calificacionCreador: Double,
    val descripcion: String = "",
    val usuariosAnotados: List<String> = emptyList()
)