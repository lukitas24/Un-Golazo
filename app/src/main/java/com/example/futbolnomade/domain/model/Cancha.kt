package com.example.futbolnomade.domain.model

data class HorarioDisponible(
    val dia: String,
    val horaApertura: String,
    val horaCierre: String
)

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
    val disponible: Boolean,
    // Horarios por día (para administración avanzada)
    val horarios: List<HorarioDisponible> = emptyList()
)