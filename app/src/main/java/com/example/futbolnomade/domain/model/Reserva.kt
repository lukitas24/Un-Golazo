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
     * Usuario que hizo la reserva.
     */
    val usuarioUid: String = "",
    val usuarioEmail: String = "",
    val usuarioNombre: String = "",

    /*
     * Dueño de la cancha.
     *
     * Guardarlos también en la reserva hace que el backend no dependa
     * de una segunda consulta para enviar NUEVA_SOLICITUD_RESERVA.
     * El email sirve como fallback para canchas antiguas.
     */
    val propietarioCanchaUid: String = "",
    val propietarioCanchaEmail: String = "",

    val fecha: String = "",
    val hora: String = "",

    val estado: String = "Pendiente",

    val partidoId: String? = null
)