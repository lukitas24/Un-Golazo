package com.example.futbolnomade.data.remote

import com.example.futbolnomade.domain.model.EstadoPartido
import com.example.futbolnomade.domain.model.NotificationEvent
import com.example.futbolnomade.domain.model.NotificationEventType
import com.example.futbolnomade.domain.model.Partido
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class PartidoRemoteDataSource {

    private val db =
        FirebaseFirestore.getInstance()

    private val partidosCollection =
        db.collection("partidos")

    /*
     * Outbox de notificaciones.
     *
     * El backend de tu compañera procesa estos documentos y envía FCM.
     */
    private val notificationEventsCollection =
        db.collection("notification_events")

    suspend fun obtenerPartidos(): List<Partido> {
        return try {
            val snapshot =
                partidosCollection
                    .get()
                    .await()

            snapshot.documents.mapNotNull { document ->
                document
                    .toObject(Partido::class.java)
                    ?.copy(id = document.id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun obtenerPartido(
        id: String
    ): Partido? {
        return try {
            val document =
                partidosCollection
                    .document(id)
                    .get()
                    .await()

            document
                .toObject(Partido::class.java)
                ?.copy(id = document.id)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun crearPartido(
        partido: Partido
    ) {
        try {
            val docRef =
                if (partido.id.isBlank()) {
                    partidosCollection.document()
                } else {
                    partidosCollection.document(
                        partido.id
                    )
                }

            val partidoConId =
                partido.copy(
                    id = docRef.id
                )

            docRef
                .set(partidoConId)
                .await()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /*
     * NOTIFICACIÓN: PARTIDO_MODIFICADO
     *
     * Solo notifica cambios realmente importantes para quien va a jugar:
     * - fecha
     * - horario
     * - ubicación
     * - cancha
     *
     * NO dispara por:
     * - alguien que se anota/se baja
     * - participantesActuales
     * - usuariosAnotados
     * - calificaciones
     * - cambios de estado de reserva
     *
     * De esa forma evitamos notificaciones duplicadas.
     */
    suspend fun actualizarPartido(
        partido: Partido
    ) {
        if (partido.id.isBlank()) {
            return
        }

        try {
            val partidoRef =
                partidosCollection.document(
                    partido.id
                )

            db.runTransaction { transaction ->

                val partidoAnterior =
                    transaction
                        .get(partidoRef)
                        .toObject(
                            Partido::class.java
                        )
                        ?: return@runTransaction false

                val cambios =
                    mutableListOf<String>()

                if (
                    partidoAnterior.fecha !=
                    partido.fecha
                ) {
                    cambios.add("fecha")
                }

                if (
                    partidoAnterior.horario !=
                    partido.horario
                ) {
                    cambios.add("horario")
                }

                if (
                    partidoAnterior.ubicacion
                        .trim() !=
                    partido.ubicacion
                        .trim()
                ) {
                    cambios.add("ubicación")
                }

                val cambioCancha =
                    partidoAnterior.canchaId !=
                            partido.canchaId ||
                            partidoAnterior.nombreCancha
                                .orEmpty()
                                .trim() !=
                            partido.nombreCancha
                                .orEmpty()
                                .trim()

                if (cambioCancha) {
                    cambios.add("cancha")
                }

                /*
                 * Conservamos el comportamiento que ya tenía el método:
                 * merge para no borrar campos agregados por backend.
                 */
                transaction.set(
                    partidoRef,
                    partido,
                    SetOptions.merge()
                )

                if (cambios.isNotEmpty()) {

                    val uids =
                        normalizarUids(
                            partidoAnterior
                        )

                    val destinatariosUids =
                        uids
                            .filter {
                                it.isNotBlank() &&
                                        it !=
                                        partidoAnterior.creadorUid
                            }
                            .distinct()

                    val destinatariosEmails =
                        partidoAnterior
                            .usuariosAnotados
                            .filter {
                                !it.trim().equals(
                                    partidoAnterior
                                        .creador
                                        .trim(),
                                    ignoreCase = true
                                )
                            }
                            .map {
                                it.trim().lowercase()
                            }
                            .filter {
                                it.isNotBlank()
                            }
                            .distinct()

                    if (
                        destinatariosUids.isNotEmpty() ||
                        destinatariosEmails.isNotEmpty()
                    ) {
                        val eventoRef =
                            notificationEventsCollection
                                .document()

                        val cambiosTexto =
                            cambios.joinToString(
                                separator = ", "
                            )

                        val evento =
                            NotificationEvent(
                                id =
                                    eventoRef.id,

                                tipo =
                                    NotificationEventType
                                        .PARTIDO_MODIFICADO,

                                destinatariosUids =
                                    destinatariosUids,

                                destinatariosEmails =
                                    destinatariosEmails,

                                titulo =
                                    "Se modificó tu partido ⚽",

                                mensaje =
                                    "Cambió $cambiosTexto en \"${partidoAnterior.titulo}\". Revisá los nuevos datos.",
                        partidoId =
                            partido.id,

                        actorUid =
                            partidoAnterior
                                .creadorUid,

                        actorEmail =
                            partidoAnterior
                                .creador,

                        creadoEn =
                            System
                                .currentTimeMillis(),

                        procesado =
                            false
                        )

                        transaction.set(
                            eventoRef,
                            evento
                        )
                    }
                }

                true
            }.await()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun actualizarEstadoPartido(
        partidoId: String,
        nuevoEstado: EstadoPartido
    ) {
        if (partidoId.isBlank()) {
            return
        }

        partidosCollection
            .document(partidoId)
            .update(
                "estado",
                nuevoEstado.name
            )
            .await()
    }

    /*
     * NOTIFICACIÓN 4:
     * "El organizador canceló el partido".
     *
     * Antes de borrar el partido dejamos el evento en la misma
     * transacción. Así no perdemos la lista de destinatarios.
     */
    suspend fun eliminarPartido(
        id: String
    ) {
        if (id.isBlank()) {
            return
        }

        try {
            val partidoRef =
                partidosCollection.document(id)

            db.runTransaction { transaction ->

                val partido =
                    transaction
                        .get(partidoRef)
                        .toObject(
                            Partido::class.java
                        )
                        ?: return@runTransaction false

                val uids =
                    normalizarUids(partido)

                val destinatariosUids =
                    uids
                        .filter {
                            it.isNotBlank() &&
                                    it != partido.creadorUid
                        }
                        .distinct()

                val destinatariosEmails =
                    partido.usuariosAnotados
                        .filter {
                            !it.trim().equals(
                                partido.creador.trim(),
                                ignoreCase = true
                            )
                        }
                        .map {
                            it.trim().lowercase()
                        }
                        .filter {
                            it.isNotBlank()
                        }
                        .distinct()

                if (
                    destinatariosUids.isNotEmpty() ||
                    destinatariosEmails.isNotEmpty()
                ) {
                    val eventoRef =
                        notificationEventsCollection
                            .document()

                    val evento =
                        NotificationEvent(
                            id = eventoRef.id,
                            tipo =
                                NotificationEventType
                                    .PARTIDO_CANCELADO,

                            destinatariosUids =
                                destinatariosUids,

                            destinatariosEmails =
                                destinatariosEmails,

                            titulo =
                                "Partido cancelado",

                            mensaje =
                                "El organizador canceló \"${partido.titulo}\".",

                            partidoId =
                                partido.id,

                            actorUid =
                                partido.creadorUid,

                            actorEmail =
                                partido.creador,

                            creadoEn =
                                System.currentTimeMillis()
                        )

                    transaction.set(
                        eventoRef,
                        evento
                    )
                }

                transaction.delete(
                    partidoRef
                )

                true
            }.await()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /*
     * NOTIFICACIONES 1 y 3:
     *
     * - NUEVO_JUGADOR_PARTIDO al organizador.
     * - PARTIDO_COMPLETO al organizador si este ingreso llena el cupo.
     */
    suspend fun anotarseAPartido(
        partidoId: String,
        usuario: String,
        usuarioUid: String
    ): Boolean {
        return try {
            val docRef =
                partidosCollection.document(
                    partidoId
                )

            db.runTransaction { transaction ->

                val snapshot =
                    transaction.get(docRef)

                val partido =
                    snapshot.toObject(
                        Partido::class.java
                    ) ?: return@runTransaction false

                val uidsNormalizados =
                    normalizarUids(
                        partido
                    )

                val indiceExistente =
                    partido.usuariosAnotados
                        .indexOfFirst {
                            it.trim().equals(
                                usuario.trim(),
                                ignoreCase = true
                            )
                        }

                /*
                 * Partido viejo: si ya estaba por email solamente
                 * completamos UID. Esto NO es un nuevo ingreso y por
                 * eso no generamos notificación.
                 */
                if (indiceExistente >= 0) {
                    if (
                        usuarioUid.isNotBlank() &&
                        uidsNormalizados[
                            indiceExistente
                        ] != usuarioUid
                    ) {
                        uidsNormalizados[
                            indiceExistente
                        ] = usuarioUid

                        transaction.update(
                            docRef,
                            "usuariosAnotadosUids",
                            uidsNormalizados
                        )

                        return@runTransaction true
                    }

                    return@runTransaction false
                }

                if (
                    partido.participantesActuales >=
                    partido.participantesMaximos
                ) {
                    return@runTransaction false
                }

                val nuevosUsuarios =
                    partido.usuariosAnotados +
                            usuario

                val nuevosUids =
                    uidsNormalizados.apply {
                        add(usuarioUid)
                    }

                val nuevaCantidad =
                    partido.participantesActuales + 1

                transaction.update(
                    docRef,
                    mapOf(
                        "usuariosAnotados" to
                                nuevosUsuarios,

                        "usuariosAnotadosUids" to
                                nuevosUids,

                        "participantesActuales" to
                                nuevaCantidad
                    )
                )

                /*
                 * No tiene sentido notificar al propio usuario
                 * si por alguna inconsistencia fuera también creador.
                 */
                val puedeAvisarAlCreador =
                    !partido.creador
                        .trim()
                        .equals(
                            usuario.trim(),
                            ignoreCase = true
                        )

                if (puedeAvisarAlCreador) {
                    crearEventoEnTransaccion(
                        transaction = transaction,

                        tipo =
                            NotificationEventType
                                .NUEVO_JUGADOR_PARTIDO,

                        destinatariosUids =
                            listOfNotBlank(
                                partido.creadorUid
                            ),

                        destinatariosEmails =
                            listOfNotBlank(
                                partido.creador
                            ),

                        titulo =
                            "Nuevo jugador en tu partido",

                        mensaje =
                            "$usuario se anotó a \"${partido.titulo}\".",

                        partidoId =
                            partido.id,

                        actorUid =
                            usuarioUid,

                        actorEmail =
                            usuario
                    )
                }

                /*
                 * Si este ingreso completa el partido, generamos un
                 * segundo evento para el organizador.
                 */
                if (
                    nuevaCantidad ==
                    partido.participantesMaximos
                ) {
                    crearEventoEnTransaccion(
                        transaction = transaction,

                        tipo =
                            NotificationEventType
                                .PARTIDO_COMPLETO,

                        destinatariosUids =
                            listOfNotBlank(
                                partido.creadorUid
                            ),

                        destinatariosEmails =
                            listOfNotBlank(
                                partido.creador
                            ),

                        titulo =
                            "¡Partido completo! ⚽",

                        mensaje =
                            "\"${partido.titulo}\" ya tiene todos los jugadores.",

                        partidoId =
                            partido.id
                    )
                }

                true
            }.await()

        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /*
     * NOTIFICACIÓN 2:
     * el jugador se baja voluntariamente.
     */
    suspend fun cancelarInscripcion(
        partidoId: String,
        usuario: String,
        usuarioUid: String
    ): Boolean {
        return try {
            val docRef =
                partidosCollection.document(
                    partidoId
                )

            db.runTransaction { transaction ->

                val snapshot =
                    transaction.get(docRef)

                val partido =
                    snapshot.toObject(
                        Partido::class.java
                    ) ?: return@runTransaction false

                val indiceUsuario =
                    partido.usuariosAnotados
                        .indexOfFirst {
                            it.trim().equals(
                                usuario.trim(),
                                ignoreCase = true
                            )
                        }

                if (indiceUsuario < 0) {
                    return@runTransaction false
                }

                if (
                    partido.creador
                        .trim()
                        .equals(
                            usuario.trim(),
                            ignoreCase = true
                        )
                ) {
                    return@runTransaction false
                }

                val nuevosUsuarios =
                    partido.usuariosAnotados
                        .toMutableList()
                        .apply {
                            removeAt(
                                indiceUsuario
                            )
                        }

                val nuevosUids =
                    normalizarUids(
                        partido
                    )
                        .apply {
                            if (
                                indiceUsuario in indices
                            ) {
                                removeAt(
                                    indiceUsuario
                                )
                            }
                        }

                transaction.update(
                    docRef,
                    mapOf(
                        "usuariosAnotados" to
                                nuevosUsuarios,

                        "usuariosAnotadosUids" to
                                nuevosUids,

                        "participantesActuales" to
                                (
                                        partido.participantesActuales -
                                                1
                                        ).coerceAtLeast(1)
                    )
                )

                crearEventoEnTransaccion(
                    transaction = transaction,

                    tipo =
                        NotificationEventType
                            .JUGADOR_ABANDONO_PARTIDO,

                    destinatariosUids =
                        listOfNotBlank(
                            partido.creadorUid
                        ),

                    destinatariosEmails =
                        listOfNotBlank(
                            partido.creador
                        ),

                    titulo =
                        "Un jugador se bajó del partido",

                    mensaje =
                        "$usuario ya no participará de \"${partido.titulo}\".",

                    partidoId =
                        partido.id,

                    actorUid =
                        usuarioUid,

                    actorEmail =
                        usuario
                )

                true
            }.await()

        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /*
     * NOTIFICACIÓN 8:
     * el organizador elimina a un jugador.
     *
     * Es importante que tenga un evento diferente de cancelarInscripcion,
     * porque mirando solamente before/after Firestore no se puede saber
     * si el jugador se bajó solo o si fue expulsado.
     */
    suspend fun eliminarJugador(
        partidoId: String,
        jugadorAEliminar: String,
        usuarioSolicitante: String
    ): Boolean {
        return try {
            val docRef =
                partidosCollection.document(
                    partidoId
                )

            db.runTransaction { transaction ->

                val snapshot =
                    transaction.get(docRef)

                val partido =
                    snapshot.toObject(
                        Partido::class.java
                    ) ?: return@runTransaction false

                if (
                    !partido.creador
                        .trim()
                        .equals(
                            usuarioSolicitante.trim(),
                            ignoreCase = true
                        )
                ) {
                    return@runTransaction false
                }

                val indiceJugador =
                    partido.usuariosAnotados
                        .indexOfFirst {
                            it.trim().equals(
                                jugadorAEliminar.trim(),
                                ignoreCase = true
                            )
                        }

                if (indiceJugador < 0) {
                    return@runTransaction false
                }

                if (
                    partido.creador
                        .trim()
                        .equals(
                            jugadorAEliminar.trim(),
                            ignoreCase = true
                        )
                ) {
                    return@runTransaction false
                }

                val uidsAntes =
                    normalizarUids(
                        partido
                    )

                val uidJugadorEliminado =
                    uidsAntes
                        .getOrNull(
                            indiceJugador
                        )
                        .orEmpty()

                val nuevosUsuarios =
                    partido.usuariosAnotados
                        .toMutableList()
                        .apply {
                            removeAt(
                                indiceJugador
                            )
                        }

                val nuevosUids =
                    uidsAntes
                        .toMutableList()
                        .apply {
                            if (
                                indiceJugador in indices
                            ) {
                                removeAt(
                                    indiceJugador
                                )
                            }
                        }

                val nuevaCantidad =
                    (
                            partido.participantesActuales -
                                    1
                            ).coerceAtLeast(1)

                transaction.update(
                    docRef,
                    mapOf(
                        "usuariosAnotados" to
                                nuevosUsuarios,

                        "usuariosAnotadosUids" to
                                nuevosUids,

                        "participantesActuales" to
                                nuevaCantidad
                    )
                )

                crearEventoEnTransaccion(
                    transaction = transaction,

                    tipo =
                        NotificationEventType
                            .ELIMINADO_DE_PARTIDO,

                    destinatariosUids =
                        listOfNotBlank(
                            uidJugadorEliminado
                        ),

                    destinatariosEmails =
                        listOfNotBlank(
                            jugadorAEliminar
                        ),

                    titulo =
                        "Ya no participás de este partido",

                    mensaje =
                        "El organizador te quitó de \"${partido.titulo}\".",

                    partidoId =
                        partido.id,

                    actorUid =
                        partido.creadorUid,

                    actorEmail =
                        partido.creador
                )

                true
            }.await()

        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun crearEventoEnTransaccion(
        transaction:
        com.google.firebase.firestore.Transaction,

        tipo: String,
        destinatariosUids: List<String>,
        destinatariosEmails: List<String>,

        titulo: String,
        mensaje: String,

        partidoId: String? = null,
        canchaId: String? = null,
        reservaId: String? = null,

        actorUid: String = "",
        actorEmail: String = ""
    ) {
        val uids =
            destinatariosUids
                .filter {
                    it.isNotBlank()
                }
                .distinct()

        val emails =
            destinatariosEmails
                .map {
                    it.trim().lowercase()
                }
                .filter {
                    it.isNotBlank()
                }
                .distinct()

        if (
            uids.isEmpty() &&
            emails.isEmpty()
        ) {
            return
        }

        val eventoRef =
            notificationEventsCollection
                .document()

        val evento =
            NotificationEvent(
                id =
                    eventoRef.id,

                tipo =
                    tipo,

                destinatariosUids =
                    uids,

                destinatariosEmails =
                    emails,

                titulo =
                    titulo,

                mensaje =
                    mensaje,

                partidoId =
                    partidoId,

                canchaId =
                    canchaId,

                reservaId =
                    reservaId,

                actorUid =
                    actorUid,

                actorEmail =
                    actorEmail,

                creadoEn =
                    System.currentTimeMillis(),

                procesado =
                    false
            )

        transaction.set(
            eventoRef,
            evento
        )
    }

    private fun listOfNotBlank(
        value: String
    ): List<String> {
        return if (
            value.isBlank()
        ) {
            emptyList()
        } else {
            listOf(value)
        }
    }

    /*
     * Mantiene usuariosAnotados y usuariosAnotadosUids alineados.
     */
    private fun normalizarUids(
        partido: Partido
    ): MutableList<String> {

        val resultado =
            partido.usuariosAnotadosUids
                .toMutableList()

        while (
            resultado.size <
            partido.usuariosAnotados.size
        ) {
            resultado.add("")
        }

        while (
            resultado.size >
            partido.usuariosAnotados.size
        ) {
            resultado.removeAt(
                resultado.lastIndex
            )
        }

        return resultado
    }
}