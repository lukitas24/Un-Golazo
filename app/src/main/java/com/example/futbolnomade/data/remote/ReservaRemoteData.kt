package com.example.futbolnomade.data.remote

import com.example.futbolnomade.domain.model.NotificationEvent
import com.example.futbolnomade.domain.model.NotificationEventType
import com.example.futbolnomade.domain.model.Reserva
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ReservaRemoteDataSource {

    private val db =
        FirebaseFirestore.getInstance()

    private val reservasCollection =
        db.collection("reservas")

    private val notificationEventsCollection =
        db.collection("notification_events")

    fun obtenerReservasPorUsuario(
        usuarioId: String
    ): Flow<List<Reserva>> =
        callbackFlow {

            val listener =
                reservasCollection
                    .whereEqualTo(
                        "usuarioId",
                        usuarioId
                    )
                    .addSnapshotListener {
                            snapshot,
                            error ->

                        if (error != null) {
                            close(error)
                            return@addSnapshotListener
                        }

                        val lista =
                            snapshot
                                ?.documents
                                ?.mapNotNull {
                                    it
                                        .toObject(
                                            Reserva::class.java
                                        )
                                        ?.copy(
                                            id = it.id
                                        )
                                }
                                ?: emptyList()

                        trySend(
                            lista
                        )
                    }

            awaitClose {
                listener.remove()
            }
        }

    fun obtenerReservasPorCancha(
        canchaId: String
    ): Flow<List<Reserva>> =
        callbackFlow {

            val listener =
                reservasCollection
                    .whereEqualTo(
                        "canchaId",
                        canchaId
                    )
                    .addSnapshotListener {
                            snapshot,
                            error ->

                        if (error != null) {
                            close(error)
                            return@addSnapshotListener
                        }

                        val lista =
                            snapshot
                                ?.documents
                                ?.mapNotNull {
                                    it
                                        .toObject(
                                            Reserva::class.java
                                        )
                                        ?.copy(
                                            id = it.id
                                        )
                                }
                                ?: emptyList()

                        trySend(
                            lista
                        )
                    }

            awaitClose {
                listener.remove()
            }
        }

    /*
     * NOTIFICACIÓN 6:
     * Nueva solicitud de reserva -> dueño de la cancha.
     *
     * La reserva y el evento se guardan en el mismo batch.
     */
    suspend fun crearReserva(
        reserva: Reserva
    ) {
        try {
            val reservaRef =
                reservasCollection
                    .document()

            val reservaConId =
                reserva.copy(
                    id = reservaRef.id
                )

            val batch =
                db.batch()

            batch.set(
                reservaRef,
                reservaConId
            )

            val esPendiente =
                reserva.estado
                    .trim()
                    .equals(
                        "Pendiente",
                        ignoreCase = true
                    )

            val esElMismoUsuario =
                reserva.usuarioUid.isNotBlank() &&
                        reserva.usuarioUid ==
                        reserva.propietarioCanchaUid

            if (
                esPendiente &&
                !esElMismoUsuario
            ) {
                val evento =
                    crearEvento(
                        tipo =
                            NotificationEventType
                                .NUEVA_SOLICITUD_RESERVA,

                        destinatariosUids =
                            listOfNotBlank(
                                reserva
                                    .propietarioCanchaUid
                            ),

                        destinatariosEmails =
                            listOfNotBlank(
                                reserva
                                    .propietarioCanchaEmail
                            ),

                        titulo =
                            "Nueva solicitud de reserva 🏟️",

                        mensaje =
                            "${reserva.usuarioNombre.ifBlank { reserva.usuarioEmail }} quiere reservar ${reserva.canchaNombre} el ${reserva.fecha} a las ${reserva.hora}.",

                        partidoId =
                            reserva.partidoId,

                        canchaId =
                            reserva.canchaId,

                        reservaId =
                            reservaRef.id,

                        actorUid =
                            reserva.usuarioUid,

                        actorEmail =
                            reserva.usuarioEmail
                    )

                if (evento != null) {
                    batch.set(
                        notificationEventsCollection
                            .document(
                                evento.id
                            ),
                        evento
                    )
                }
            }

            batch.commit()
                .await()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /*
     * La cancelación de reserva sigue exactamente como estaba.
     * Por decisión de producto NO genera notificación por ahora.
     */
    suspend fun cancelarReserva(
        reservaId: String
    ) {
        try {
            reservasCollection
                .document(reservaId)
                .update(
                    "estado",
                    "Cancelada"
                )
                .await()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /*
     * Aprovechamos el outbox para las dos notificaciones que ya
     * habíamos planteado anteriormente:
     *
     * - Reserva confirmada.
     * - Reserva rechazada.
     */
    suspend fun actualizarEstadoReserva(
        reservaId: String,
        nuevoEstado: String
    ) {
        if (reservaId.isBlank()) {
            return
        }

        try {
            val reservaRef =
                reservasCollection.document(
                    reservaId
                )

            db.runTransaction { transaction ->

                val reserva =
                    transaction
                        .get(reservaRef)
                        .toObject(
                            Reserva::class.java
                        )
                        ?: return@runTransaction false

                val estadoAnterior =
                    reserva.estado.trim()

                if (
                    estadoAnterior.equals(
                        nuevoEstado.trim(),
                        ignoreCase = true
                    )
                ) {
                    return@runTransaction true
                }

                transaction.update(
                    reservaRef,
                    "estado",
                    nuevoEstado
                )

                val tipo =
                    when {
                        nuevoEstado.equals(
                            "Confirmada",
                            ignoreCase = true
                        ) -> {
                            NotificationEventType
                                .RESERVA_CONFIRMADA
                        }

                        nuevoEstado.equals(
                            "Rechazada",
                            ignoreCase = true
                        ) -> {
                            NotificationEventType
                                .RESERVA_RECHAZADA
                        }

                        else -> null
                    }

                if (tipo != null) {
                    val esConfirmada =
                        tipo ==
                                NotificationEventType
                                    .RESERVA_CONFIRMADA

                    val evento =
                        crearEvento(
                            tipo =
                                tipo,

                            destinatariosUids =
                                listOfNotBlank(
                                    reserva.usuarioUid
                                ),

                            destinatariosEmails =
                                listOfNotBlank(
                                    reserva.usuarioEmail
                                        .ifBlank {
                                            reserva.usuarioId
                                        }
                                ),

                            titulo =
                                if (esConfirmada) {
                                    "Reserva confirmada ✅"
                                } else {
                                    "Reserva rechazada"
                                },

                            mensaje =
                                if (esConfirmada) {
                                    "Tu reserva en ${reserva.canchaNombre} para el ${reserva.fecha} a las ${reserva.hora} fue confirmada."
                                } else {
                                    "Tu reserva en ${reserva.canchaNombre} para el ${reserva.fecha} a las ${reserva.hora} fue rechazada."
                                },

                            partidoId =
                                reserva.partidoId,

                            canchaId =
                                reserva.canchaId,

                            reservaId =
                                reserva.id
                                    .ifBlank {
                                        reservaId
                                    }
                        )

                    if (evento != null) {
                        transaction.set(
                            notificationEventsCollection
                                .document(
                                    evento.id
                                ),
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

    private fun crearEvento(
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
    ): NotificationEvent? {

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
            return null
        }

        val eventoRef =
            notificationEventsCollection
                .document()

        return NotificationEvent(
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
}