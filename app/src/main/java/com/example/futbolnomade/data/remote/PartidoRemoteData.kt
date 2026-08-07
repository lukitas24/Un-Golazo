package com.example.futbolnomade.data.remote

import com.example.futbolnomade.domain.model.EstadoPartido
import com.example.futbolnomade.domain.model.Partido
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class PartidoRemoteDataSource {

    private val db = FirebaseFirestore.getInstance()
    private val partidosCollection = db.collection("partidos")

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
                    partidosCollection.document(partido.id)
                }

            val partidoConId =
                partido.copy(
                    id = docRef.id
                )

            docRef
                .set(partidoConId)
                .await()

            println(
                "FIREBASE_DEBUG: Partido guardado con éxito: ${partido.titulo}"
            )
        } catch (e: Exception) {
            println(
                "FIREBASE_DEBUG: Error al crear partido: ${e.message}"
            )
            e.printStackTrace()
        }
    }

    /*
     * Actualiza los datos del partido sin borrar campos que pudieran
     * agregarse posteriormente desde el backend.
     *
     * La futura Cloud Function podrá comparar before/after y decidir si
     * corresponde enviar PARTIDO_MODIFICADO.
     */
    suspend fun actualizarPartido(
        partido: Partido
    ) {
        if (partido.id.isBlank()) {
            return
        }

        partidosCollection
            .document(partido.id)
            .set(
                partido,
                SetOptions.merge()
            )
            .await()
    }

    /*
     * El cambio de estado de la reserva tiene su propio método.
     * Así no reutilizamos crearPartido() para hacer una actualización.
     */
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

    suspend fun eliminarPartido(
        id: String
    ) {
        try {
            partidosCollection
                .document(id)
                .delete()
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

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

                /*
                 * Normalizamos la lista de UID para soportar partidos
                 * viejos que solo tenían usuariosAnotados (emails).
                 */
                val uidsNormalizados =
                    normalizarUids(partido)

                val indiceExistente =
                    partido.usuariosAnotados
                        .indexOfFirst {
                            it.trim().equals(
                                usuario.trim(),
                                ignoreCase = true
                            )
                        }

                /*
                 * Si ya estaba anotado por email, aprovechamos para
                 * completar su UID si el partido es antiguo.
                 */
                if (indiceExistente >= 0) {
                    if (
                        usuarioUid.isNotBlank() &&
                        uidsNormalizados[indiceExistente] != usuarioUid
                    ) {
                        uidsNormalizados[indiceExistente] =
                            usuarioUid

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

                /*
                 * Aunque usuarioUid llegara vacío, lo agregamos para
                 * conservar el mismo índice entre ambas listas.
                 */
                val nuevosUids =
                    uidsNormalizados.apply {
                        add(usuarioUid)
                    }

                transaction.update(
                    docRef,
                    mapOf(
                        "usuariosAnotados" to
                                nuevosUsuarios,

                        "usuariosAnotadosUids" to
                                nuevosUids,

                        "participantesActuales" to
                                partido.participantesActuales + 1
                    )
                )

                true
            }.await()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

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

                /*
                 * Evitamos que el creador se quite mediante esta acción.
                 * El comportamiento original de la UI puede seguir
                 * controlándolo también, pero acá queda protegido.
                 */
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
                            removeAt(indiceUsuario)
                        }

                val nuevosUids =
                    normalizarUids(partido)
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
                                        partido.participantesActuales - 1
                                        ).coerceAtLeast(1)
                    )
                )

                true
            }.await()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

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

                // Solo el creador puede expulsar jugadores.
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

                // El creador no puede eliminarse a sí mismo.
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

                val nuevosUsuarios =
                    partido.usuariosAnotados
                        .toMutableList()
                        .apply {
                            removeAt(indiceJugador)
                        }

                val nuevosUids =
                    normalizarUids(partido)
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
                            partido.participantesActuales - 1
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

                true
            }.await()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /*
     * Mantiene usuariosAnotados y usuariosAnotadosUids alineados por índice.
     *
     * Ejemplo:
     * usuariosAnotados[1]     -> maia@gmail.com
     * usuariosAnotadosUids[1] -> UID de Maia
     *
     * En partidos viejos se completan posiciones vacías con "".
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