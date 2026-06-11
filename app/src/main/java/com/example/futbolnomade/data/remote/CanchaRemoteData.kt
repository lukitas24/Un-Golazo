package com.example.futbolnomade.data.remote

import com.example.futbolnomade.domain.model.Cancha
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CanchaRemoteDataSource {
    private val db = Firebase.firestore

    fun getCanchas(userId: String): Flow<List<Cancha>> = callbackFlow {
        val listener = db.collection("canchas")
            .whereEqualTo("propietarioId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val lista = snapshot?.documents?.mapNotNull {
                    it.toObject(Cancha::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(lista)
            }
        awaitClose { listener.remove() }
    }

    suspend fun guardarCancha(cancha: Cancha) {
        val ref = if (cancha.id.isEmpty())
            db.collection("canchas").document()
        else
            db.collection("canchas").document(cancha.id)

        ref.set(cancha.copy(id = ref.id)).await()
    }

    suspend fun eliminarCancha(id: String) {
        db.collection("canchas").document(id).delete().await()
    }
}
