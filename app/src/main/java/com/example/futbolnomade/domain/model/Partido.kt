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

    // Momento exacto en el que comienza el partido.
    val fechaHoraInicio: Long = 0L,

    val ubicacion: String = "",
    val dificultad: String = "",
    val participantesActuales: Int = 0,
    val participantesMaximos: Int = 0,

    /*
     * Se mantienen los emails por compatibilidad con el código
     * y con los partidos que ya existen en Firestore.
     */
    val creador: String = "",
    val usuariosAnotados: List<String> = emptyList(),

    /*
     * Nuevos campos usados para poder dirigir notificaciones FCM
     * al usuario correcto.
     *
     * usuariosAnotadosUids mantiene el mismo orden que
     * usuariosAnotados. En partidos viejos puede estar vacío.
     */
    val creadorUid: String = "",
    val usuariosAnotadosUids: List<String> = emptyList(),

    val calificacionCreador: Double = 0.0,
    val descripcion: String = "",

    val canchaId: String? = null,
    val nombreCancha: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,

    val estado: EstadoPartido = EstadoPartido.PUBLICADO
)