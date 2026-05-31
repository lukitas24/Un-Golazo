package com.example.futbolnomade.domain.model

data class Cancha(
    val id: Int,
    val nombre: String,
    val ubicacion: String,
    val descripcion: String,
    val precio: Double,
    val telefono: String,
    val horarioApertura: String,
    val horarioCierre: String,
    val calificacion: Double,
    val propietario: String,
    val disponible: Boolean
)