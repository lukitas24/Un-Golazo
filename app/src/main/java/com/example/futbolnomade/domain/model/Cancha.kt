package com.example.futbolnomade.domain.model

data class HorarioDisponible(
    val dia: String = "",
    val horaApertura: String = "",
    val horaCierre: String = ""
)

data class Cancha(
    val id: String = "",
    val nombre: String = "",
    val ubicacion: String = "",
    val descripcion: String = "",
    val precio: Double = 0.0,
    val telefono: String = "",
    val horarioApertura: String = "",
    val horarioCierre: String = "",
    val calificacion: Double = 0.0,
    val cantidadValoraciones: Int  =0,
    val propietario: String = "",
    val disponible: Boolean = true,

    val latitud: Double = -42.7692,
    val longitud: Double = -65.0385,

    val horarios: List<HorarioDisponible> = emptyList()
)