package com.example.futbolnomade.data.remote

import android.util.Base64
import com.example.futbolnomade.domain.model.Cancha
import com.example.futbolnomade.domain.model.Partido
import com.example.futbolnomade.domain.model.ValoracionJugador
import com.example.futbolnomade.domain.model.ValoracionPartido
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ValoracionRemoteDataSource {

    private val db =
        FirebaseFirestore.getInstance()

    private val valoracionesCollection =
        db.collection("valoraciones")

    private val canchasCollection =
        db.collection("canchas")

    private val partidosCollection =
        db.collection("partidos")

    suspend fun guardarValoracion(
        valoracion: ValoracionPartido
    ): Boolean {

        if (
            valoracion.partidoId.isBlank() ||
            valoracion.autorEmail.isBlank()
        ) {
            return false
        }

        val autorNormalizado =
            valoracion.autorEmail
                .trim()
                .lowercase()

        return try {
            val valoracionId =
                crearValoracionId(
                    partidoId =
                        valoracion.partidoId,

                    autorEmail =
                        autorNormalizado
                )

            val valoracionRef =
                valoracionesCollection
                    .document(valoracionId)

            val partidoRef =
                partidosCollection
                    .document(valoracion.partidoId)

            db.runTransaction { transaction ->

                /*
                 * Todas las lecturas se realizan antes
                 * de comenzar las escrituras.
                 */
                val valoracionExistente =
                    transaction.get(
                        valoracionRef
                    )

                val partidoActual =
                    transaction
                        .get(partidoRef)
                        .toObject(
                            Partido::class.java
                        )

                if (valoracionExistente.exists()) {
                    return@runTransaction false
                }

                if (partidoActual == null) {
                    return@runTransaction false
                }

                /*
                 * El usuario solamente puede valorar si estaba
                 * anotado en el partido.
                 */
                val indiceAutor =
                    partidoActual
                        .usuariosAnotados
                        .indexOfFirst { usuario ->
                            usuario
                                .trim()
                                .equals(
                                    autorNormalizado,
                                    ignoreCase = true
                                )
                        }

                if (indiceAutor < 0) {
                    return@runTransaction false
                }

                /*
                 * El partido debe tener una fecha válida y
                 * ya debe haber comenzado.
                 */
                val momentoActual =
                    System.currentTimeMillis()

                val partidoYaOcurrio =
                    partidoActual.fechaHoraInicio > 0L &&
                            partidoActual.fechaHoraInicio <=
                            momentoActual

                if (!partidoYaOcurrio) {
                    return@runTransaction false
                }

                /*
                 * Emails válidos de los participantes.
                 */
                val participantesDelPartido =
                    partidoActual
                        .usuariosAnotados
                        .map {
                            it.trim().lowercase()
                        }
                        .toSet()

                val jugadoresValidos =
                    valoracion
                        .valoracionesJugadores
                        .all {
                            val emailJugador =
                                it.jugadorEmail
                                    .trim()
                                    .lowercase()

                            emailJugador in
                                    participantesDelPartido &&
                                    it.puntuacion in 1..5
                        }

                if (!jugadoresValidos) {
                    return@runTransaction false
                }

                val canchaRef =
                    valoracion.canchaId
                        ?.takeIf {
                            valoracion.puntuacionCancha in
                                    1..5
                        }
                        ?.let { canchaId ->
                            canchasCollection
                                .document(canchaId)
                        }

                val canchaActual: Cancha? =
                    canchaRef?.let { referencia ->
                        transaction
                            .get(referencia)
                            .toObject(
                                Cancha::class.java
                            )
                    }

                /*
                 * Armamos email -> UID usando los datos del partido.
                 * Para partidos viejos, algún UID puede quedar vacío.
                 */
                fun uidDeParticipante(
                    email: String
                ): String {
                    val indice =
                        partidoActual
                            .usuariosAnotados
                            .indexOfFirst {
                                it.trim().equals(
                                    email.trim(),
                                    ignoreCase = true
                                )
                            }

                    if (indice < 0) {
                        return ""
                    }

                    return partidoActual
                        .usuariosAnotadosUids
                        .getOrNull(indice)
                        .orEmpty()
                }

                /*
                 * Normalizamos las valoraciones individuales y,
                 * si la pantalla no mandó UID, intentamos recuperarlo
                 * desde el partido.
                 */
                val jugadoresNormalizados:
                        List<ValoracionJugador> =
                    valoracion
                        .valoracionesJugadores
                        .map { jugador ->

                            val emailNormalizado =
                                jugador.jugadorEmail
                                    .trim()
                                    .lowercase()

                            jugador.copy(
                                jugadorEmail =
                                    emailNormalizado,

                                jugadorUid =
                                    jugador.jugadorUid
                                        .ifBlank {
                                            uidDeParticipante(
                                                emailNormalizado
                                            )
                                        }
                            )
                        }

                /*
                 * UID del autor. Priorizamos lo que ya quedó asociado
                 * al participante dentro del partido.
                 */
                val autorUidNormalizado =
                    partidoActual
                        .usuariosAnotadosUids
                        .getOrNull(indiceAutor)
                        .orEmpty()
                        .ifBlank {
                            valoracion.autorUid
                        }

                val organizadorEmailNormalizado =
                    valoracion.organizadorEmail
                        ?.trim()
                        ?.lowercase()

                val organizadorUidNormalizado =
                    if (
                        organizadorEmailNormalizado
                            .isNullOrBlank()
                    ) {
                        null
                    } else {
                        partidoActual.creadorUid
                            .ifBlank {
                                uidDeParticipante(
                                    organizadorEmailNormalizado
                                )
                            }
                            .ifBlank {
                                valoracion
                                    .organizadorUid
                                    .orEmpty()
                            }
                            .takeIf {
                                it.isNotBlank()
                            }
                    }

                /*
                 * Una misma persona puede aparecer como jugador y
                 * organizador. Usamos distinct() para que reciba UNA
                 * sola notificación por valoración guardada.
                 */
                val destinatariosUids =
                    buildList {
                        addAll(
                            jugadoresNormalizados
                                .map {
                                    it.jugadorUid
                                }
                        )

                        organizadorUidNormalizado
                            ?.let {
                                add(it)
                            }
                    }
                        .filter {
                            it.isNotBlank() &&
                                    it != autorUidNormalizado
                        }
                        .distinct()

                /*
                 * A partir de acá comienzan las escrituras.
                 */
                val valoracionNormalizada =
                    valoracion.copy(
                        id =
                            valoracionId,

                        autorEmail =
                            autorNormalizado,

                        autorUid =
                            autorUidNormalizado,

                        valoracionesJugadores =
                            jugadoresNormalizados,

                        organizadorEmail =
                            organizadorEmailNormalizado,

                        organizadorUid =
                            organizadorUidNormalizado,

                        destinatariosUids =
                            destinatariosUids,

                        fechaCreacion =
                            momentoActual
                    )

                transaction.set(
                    valoracionRef,
                    valoracionNormalizada
                )

                if (
                    canchaRef != null &&
                    canchaActual != null
                ) {
                    val cantidadAnterior =
                        canchaActual
                            .cantidadValoraciones

                    val cantidadNueva =
                        cantidadAnterior + 1

                    val sumaAnterior =
                        canchaActual.calificacion *
                                cantidadAnterior

                    val nuevoPromedio =
                        (
                                sumaAnterior +
                                        valoracion
                                            .puntuacionCancha
                                ) / cantidadNueva

                    transaction.update(
                        canchaRef,
                        mapOf(
                            "calificacion" to
                                    nuevoPromedio,

                            "cantidadValoraciones" to
                                    cantidadNueva
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
            val emailNormalizado =
                emailUsuario
                    .trim()
                    .lowercase()

            valoracionesCollection
                .whereEqualTo(
                    "autorEmail",
                    emailNormalizado
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

    suspend fun obtenerValoracionesSobreUsuario(
        emailUsuario: String
    ): List<ValoracionPartido> {
        return try {
            val emailNormalizado =
                emailUsuario
                    .trim()
                    .lowercase()

            valoracionesCollection
                .get()
                .await()
                .toObjects(
                    ValoracionPartido::class.java
                )
                .filter { valoracion ->

                    val valoradoComoOrganizador =
                        valoracion
                            .organizadorEmail
                            ?.trim()
                            ?.equals(
                                emailNormalizado,
                                ignoreCase = true
                            ) == true

                    val valoradoComoJugador =
                        valoracion
                            .valoracionesJugadores
                            .any {
                                it.jugadorEmail
                                    .trim()
                                    .equals(
                                        emailNormalizado,
                                        ignoreCase = true
                                    )
                            }

                    valoradoComoOrganizador ||
                            valoradoComoJugador
                }
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
            val emailNormalizado =
                autorEmail
                    .trim()
                    .lowercase()

            val id =
                crearValoracionId(
                    partidoId =
                        partidoId,

                    autorEmail =
                        emailNormalizado
                )

            valoracionesCollection
                .document(id)
                .get()
                .await()
                .exists()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun crearValoracionId(
        partidoId: String,
        autorEmail: String
    ): String {
        val emailCodificado =
            Base64.encodeToString(
                autorEmail
                    .trim()
                    .lowercase()
                    .toByteArray(),

                Base64.URL_SAFE or
                        Base64.NO_WRAP or
                        Base64.NO_PADDING
            )

        return "${partidoId}_$emailCodificado"
    }
}