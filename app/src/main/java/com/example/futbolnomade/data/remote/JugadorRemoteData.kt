package com.example.futbolnomade.data.remote

import com.example.futbolnomade.domain.model.Jugador
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class JugadorRemoteDataSource {
    private val db = FirebaseFirestore.getInstance()
    private val jugadoresCollection = db.collection("jugadores")

    suspend fun obtenerJugador(id: String): Jugador? {
        return try {
            val document = jugadoresCollection.document(id).get().await()
            document.toObject(Jugador::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun guardarJugador(jugador: Jugador) {
        try {
            jugadoresCollection.document(jugador.id).set(jugador).await()
        } catch (e: Exception) {
            // Manejar error
        }
    }

    suspend fun obtenerTodos(): List<Jugador> {
        return try {
            val snapshot = jugadoresCollection.get().await()
            snapshot.toObjects(Jugador::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
