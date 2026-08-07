package com.example.futbolnomade.domain.model

data class Reserva(
    val id: String = "",

    val canchaId: String = "",
    val canchaNombre: String = "",

    /*
     * Se mantiene por compatibilidad con las reservas existentes.
     * En el código actual contiene el email del usuario.
     */
    val usuarioId: String = "",

    /*
     * Nuevos campos para identificar correctamente al destinatario
     * de las notificaciones.
     */
    val usuarioUid: String = "",
    val usuarioEmail: String = "",

    val usuarioNombre: String = "",

    val fecha: String = "",
    val hora: String = "",

    val estado: String = "Pendiente", // Pendiente, Confirmada, Rechazada, Cancelada

    val partidoId: String? = null
)