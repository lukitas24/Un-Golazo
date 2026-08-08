package com.example.futbolnomade.domain.model

/*
 * Tipos de eventos que el backend convierte en notificaciones FCM.
 *
 * Android deja estos eventos en Firestore y el backend los procesa.
 */
object NotificationEventType {

    const val NUEVO_JUGADOR_PARTIDO =
        "NUEVO_JUGADOR_PARTIDO"

    const val JUGADOR_ABANDONO_PARTIDO =
        "JUGADOR_ABANDONO_PARTIDO"

    const val PARTIDO_COMPLETO =
        "PARTIDO_COMPLETO"

    const val PARTIDO_CANCELADO =
        "PARTIDO_CANCELADO"

    const val PARTIDO_MODIFICADO =
        "PARTIDO_MODIFICADO"

    const val NUEVA_SOLICITUD_RESERVA =
        "NUEVA_SOLICITUD_RESERVA"

    const val ELIMINADO_DE_PARTIDO =
        "ELIMINADO_DE_PARTIDO"

    const val RESERVA_CONFIRMADA =
        "RESERVA_CONFIRMADA"

    const val RESERVA_RECHAZADA =
        "RESERVA_RECHAZADA"

    const val VALORACION_RECIBIDA =
        "VALORACION_RECIBIDA"
}

data class NotificationEvent(
    val id: String = "",
    val tipo: String = "",

    /*
     * El backend prioriza UID.
     * Los emails quedan como fallback para registros viejos.
     */
    val destinatariosUids: List<String> = emptyList(),
    val destinatariosEmails: List<String> = emptyList(),

    val titulo: String = "",
    val mensaje: String = "",

    /*
     * Datos que viajan dentro de data de FCM.
     */
    val partidoId: String? = null,
    val canchaId: String? = null,
    val reservaId: String? = null,

    /*
     * Auditoría. No implica que el nombre/email del actor
     * se muestre al destinatario.
     */
    val actorUid: String = "",
    val actorEmail: String = "",

    val creadoEn: Long = 0L,

    /*
     * El backend lo cambia a true luego del envío correcto.
     */
    val procesado: Boolean = false
)