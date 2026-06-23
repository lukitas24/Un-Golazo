package com.example.futbolnomade.data.remote

import com.example.futbolnomade.domain.model.Partido
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PartidoRemoteDataSource {
    private val db = FirebaseFirestore.getInstance()
    private val partidosCollection = db.collection("partidos")

    suspend fun obtenerPartidos(): List<Partido> {
        return try {
            val snapshot = partidosCollection.get().await()
            snapshot.toObjects(Partido::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun obtenerPartido(id: String): Partido? {
        return try {
            val document = partidosCollection.document(id).get().await()
            document.toObject(Partido::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun crearPartido(partido: Partido) {
        try {
            val docRef = if (partido.id.isEmpty()) {
                partidosCollection.document()
            } else {
                partidosCollection.document(partido.id)
            }
            val partidoConId = partido.copy(id = docRef.id)
            docRef.set(partidoConId).await()
        } catch (e: Exception) {
            // Manejar error
        }
    }

    suspend fun eliminarPartido(id: String) {
        try {
            partidosCollection.document(id).delete().await()
        } catch (e: Exception) {
            // Manejar error
        }
    }

    suspend fun anotarseAPartido(partidoId: String, usuario: String): Boolean {
        return try {
            val docRef = partidosCollection.document(partidoId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val partido = snapshot.toObject(Partido::class.java) ?: return@runTransaction false
                
                if (usuario in partido.usuariosAnotados) return@runTransaction false
                if (partido.participantesActuales >= partido.participantesMaximos) return@runTransaction false
                
                val nuevosUsuarios = partido.usuariosAnotados + usuario
                transaction.update(docRef, "usuariosAnotados", nuevosUsuarios)
                transaction.update(docRef, "participantesActuales", partido.participantesActuales + 1)
                true
            }.await()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun cancelarInscripcion(partidoId: String, usuario: String): Boolean {
        return try {
            val docRef = partidosCollection.document(partidoId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val partido = snapshot.toObject(Partido::class.java) ?: return@runTransaction false
                
                if (usuario !in partido.usuariosAnotados) return@runTransaction false
                
                val nuevosUsuarios = partido.usuariosAnotados - usuario
                transaction.update(docRef, "usuariosAnotados", nuevosUsuarios)
                transaction.update(docRef, "participantesActuales", partido.participantesActuales - 1)
                true
            }.await()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun eliminarJugador(
        partidoId: String,
        jugadorAEliminar: String,
        usuarioSolicitante: String
    ): Boolean {
        return try {
            val docRef = partidosCollection.document(partidoId)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)

                val partido = snapshot.toObject(Partido::class.java)
                    ?: return@runTransaction false

                // Solo el creador puede expulsar jugadores
                if (partido.creador != usuarioSolicitante) {
                    return@runTransaction false
                }

                // El jugador debe estar anotado
                if (jugadorAEliminar !in partido.usuariosAnotados) {
                    return@runTransaction false
                }

                // El creador no puede eliminarse a sí mismo
                if (jugadorAEliminar == partido.creador) {
                    return@runTransaction false
                }

                val nuevosUsuarios =
                    partido.usuariosAnotados - jugadorAEliminar

                val nuevaCantidad =
                    (partido.participantesActuales - 1).coerceAtLeast(1)

                transaction.update(
                    docRef,
                    mapOf(
                        "usuariosAnotados" to nuevosUsuarios,
                        "participantesActuales" to nuevaCantidad
                    )
                )

                true
            }.await()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
