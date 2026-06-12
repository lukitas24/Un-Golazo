package com.example.futbolnomade.data.remote

import com.example.futbolnomade.domain.model.Reserva
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose

class ReservaRemoteDataSource {
    private val db = FirebaseFirestore.getInstance()
    private val reservasCollection = db.collection("reservas")

    fun obtenerReservasPorUsuario(usuarioId: String): Flow<List<Reserva>> = callbackFlow {
        val listener = reservasCollection
            .whereEqualTo("usuarioId", usuarioId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val lista = snapshot?.documents?.mapNotNull {
                    it.toObject(Reserva::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(lista)
            }
        awaitClose { listener.remove() }
    }

    suspend fun crearReserva(reserva: Reserva) {
        try {
            val docRef = reservasCollection.document()
            reservasCollection.document(docRef.id).set(reserva.copy(id = docRef.id)).await()
        } catch (e: Exception) {
            // Manejar error
        }
    }

    suspend fun cancelarReserva(reservaId: String) {
        try {
            reservasCollection.document(reservaId).update("estado", "Cancelada").await()
        } catch (e: Exception) {
            // Manejar error
        }
    }
}
