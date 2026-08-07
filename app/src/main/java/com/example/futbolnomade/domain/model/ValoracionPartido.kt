package com.example.futbolnomade.domain.model

data class ValoracionJugador(
    val jugadorEmail: String = "",

    /*
     * Nuevo: UID de Firebase Auth del jugador valorado.
     * Se usa para poder enviarle una notificación FCM.
     */
    val jugadorUid: String = "",

    val puntuacion: Int = 0
)

data class ValoracionPartido(
    val id: String = "",
    val partidoId: String = "",

    /*
     * Mantenemos email porque el resto de la aplicación todavía
     * trabaja con él.
     */
    val autorEmail: String = "",

    /*
     * Nuevo: UID de quien realizó la valoración.
     */
    val autorUid: String = "",

    val valoracionesJugadores: List<ValoracionJugador> =
        emptyList(),

    val organizadorEmail: String? = null,

    /*
     * Nuevo: UID del organizador valorado.
     */
    val organizadorUid: String? = null,

    val puntuacionOrganizador: Int = 0,

    val canchaId: String? = null,
    val puntuacionCancha: Int = 0,

    /*
     * Lista final, sin repetidos, de usuarios que deben recibir
     * "Te han puesto una valoración".
     *
     * Esto simplifica muchísimo el backend:
     * solo debe recorrer destinatariosUids.
     */
    val destinatariosUids: List<String> = emptyList(),

    val fechaCreacion: Long = 0L
)