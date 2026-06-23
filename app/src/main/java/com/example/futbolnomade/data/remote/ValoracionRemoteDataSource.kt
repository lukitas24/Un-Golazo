package com.example.futbolnomade.data.remote

import android.util.Base64
import com.example.futbolnomade.domain.model.Cancha
import com.example.futbolnomade.domain.model.ValoracionPartido
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ValoracionRemoteDataSource {

    private val db = FirebaseFirestore.getInstance()

    private val valoracionesCollection =
        db.collection("valoraciones")

    private val canchasCollection =
        db.collection("canchas")

    suspend fun guardarValoracion(
        valoracion: ValoracionPartido
    ): Boolean {
        return try {
            val valoracionId = crearValoracionId(
                partidoId = valoracion.partidoId,
                autorEmail = valoracion.autorEmail
            )

            val valoracionRef =
                valoracionesCollection.document(valoracionId)

            db.runTransaction { transaction ->

                /*
                 * Primero se realizan todas las lecturas.
                 */
                val valoracionExistente =
                    transaction.get(valoracionRef)

                if (valoracionExistente.exists()) {
                    return@runTransaction false
                }

                val canchaRef =
                    valoracion.canchaId
                        ?.takeIf {
                            valoracion.puntuacionCancha in 1..5
                        }
                        ?.let {
                            canchasCollection.document(it)
                        }

                val canchaActual: Cancha? =
                    canchaRef?.let { referencia ->
                        transaction
                            .get(referencia)
                            .toObject(Cancha::class.java)
                    }

                /*
                 * Después se realizan las escrituras.
                 */
                transaction.set(
                    valoracionRef,
                    valoracion.copy(
                        id = valoracionId,
                        fechaCreacion =
                            System.currentTimeMillis()
                    )
                )

                if (
                    canchaRef != null &&
                    canchaActual != null
                ) {
                    val cantidadAnterior =
                        canchaActual.cantidadValoraciones

                    val cantidadNueva =
                        cantidadAnterior + 1

                    val sumaAnterior =
                        canchaActual.calificacion *
                                cantidadAnterior

                    val nuevoPromedio =
                        (
                                sumaAnterior +
                                        valoracion.puntuacionCancha
                                ) / cantidadNueva

                    transaction.update(
                        canchaRef,
                        mapOf(
                            "calificacion" to nuevoPromedio,
                            "cantidadValoraciones" to cantidadNueva
                        )
                    )
                }

                true
            }.await()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun obtenerValoracionesDeUsuario(
        emailUsuario: String
    ): List<ValoracionPartido> {
        return try {
            valoracionesCollection
                .whereEqualTo(
                    "autorEmail",
                    emailUsuario
                )
                .get()
                .await()
                .toObjects(
                    ValoracionPartido::class.java
                )
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun yaValoro(
        partidoId: String,
        autorEmail: String
    ): Boolean {
        return try {
            val id = crearValoracionId(
                partidoId,
                autorEmail
            )

            valoracionesCollection
                .document(id)
                .get()
                .await()
                .exists()
        } catch (e: Exception) {
            false
        }
    }

    private fun crearValoracionId(
        partidoId: String,
        autorEmail: String
    ): String {
        val emailCodificado =
            Base64.encodeToString(
                autorEmail.toByteArray(),
                Base64.URL_SAFE or
                        Base64.NO_WRAP or
                        Base64.NO_PADDING
            )

        return "${partidoId}_$emailCodificado"
    }
}