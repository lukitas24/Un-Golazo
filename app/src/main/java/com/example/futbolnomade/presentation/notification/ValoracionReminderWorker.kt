package com.example.futbolnomade.presentation.notification

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.futbolnomade.domain.model.EstadoPartido
import com.example.futbolnomade.domain.model.Partido
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class ValoracionReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(
    appContext,
    workerParams
) {

    override suspend fun doWork(): Result {

        Log.d(
            TAG,
            "========== WORKER EJECUTADO =========="
        )

        val partidoId =
            inputData.getString(
                KEY_PARTIDO_ID
            )

        val usuarioEmail =
            inputData.getString(
                KEY_USUARIO_EMAIL
            )

        val modoPrueba =
            inputData.getBoolean(
                KEY_MODO_PRUEBA,
                false
            )

        Log.d(
            TAG,
            "partidoId=$partidoId usuarioEmail=$usuarioEmail modoPrueba=$modoPrueba"
        )

        if (partidoId.isNullOrBlank()) {
            Log.e(
                TAG,
                "Fin: partidoId vacío."
            )

            return Result.success()
        }

        if (usuarioEmail.isNullOrBlank()) {
            Log.e(
                TAG,
                "Fin: usuarioEmail vacío."
            )

            return Result.success()
        }

        val firebaseUser =
            FirebaseAuth
                .getInstance()
                .currentUser

        val emailSesionActual =
            firebaseUser?.email

        Log.d(
            TAG,
            "Firebase usuario uid=${firebaseUser?.uid} email=$emailSesionActual"
        )

        if (
            emailSesionActual.isNullOrBlank() ||
            !emailSesionActual
                .trim()
                .equals(
                    usuarioEmail.trim(),
                    ignoreCase = true
                )
        ) {
            Log.w(
                TAG,
                "Fin: la cuenta activa no coincide con la cuenta que programó el recordatorio."
            )

            return Result.success()
        }

        return try {
            val db =
                FirebaseFirestore
                    .getInstance()

            Log.d(
                TAG,
                "Leyendo partidos/$partidoId..."
            )

            val snapshot =
                db.collection("partidos")
                    .document(partidoId)
                    .get()
                    .await()

            if (!snapshot.exists()) {
                Log.w(
                    TAG,
                    "Fin: el partido ya no existe."
                )

                return Result.success()
            }

            val partido =
                snapshot.toObject(
                    Partido::class.java
                )

            if (partido == null) {
                Log.e(
                    TAG,
                    "Fin: no se pudo convertir el documento a Partido."
                )

                return Result.success()
            }

            val sigueParticipando =
                partido.usuariosAnotados.any {
                    it.trim().equals(
                        usuarioEmail.trim(),
                        ignoreCase = true
                    )
                }

            val partidoValido =
                partido.estado ==
                        EstadoPartido.PUBLICADO ||
                        partido.estado ==
                        EstadoPartido.RESERVA_APROBADA

            Log.d(
                TAG,
                """
                Partido cargado:
                titulo=${partido.titulo}
                estado=${partido.estado}
                participantes=${partido.usuariosAnotados}
                sigueParticipando=$sigueParticipando
                partidoValido=$partidoValido
                inicio=${formatearFecha(partido.fechaHoraInicio)}
                """.trimIndent()
            )

            if (!sigueParticipando) {
                Log.w(
                    TAG,
                    "Fin: el usuario ya no participa."
                )

                return Result.success()
            }

            if (!partidoValido) {
                Log.w(
                    TAG,
                    "Fin: estado no válido para valorar: ${partido.estado}"
                )

                return Result.success()
            }

            if (partido.fechaHoraInicio <= 0L) {
                Log.w(
                    TAG,
                    "Fin: fechaHoraInicio inválida."
                )

                return Result.success()
            }

            val momentoHabilitacion =
                partido.fechaHoraInicio +
                        DOS_HORAS_MILLIS

            val ahora =
                System.currentTimeMillis()

            Log.d(
                TAG,
                """
                ahora=${formatearFecha(ahora)}
                habilitacion=${formatearFecha(momentoHabilitacion)}
                diferenciaMin=${TimeUnit.MILLISECONDS.toMinutes(momentoHabilitacion - ahora)}
                """.trimIndent()
            )

            /*
             * En modo normal respetamos estrictamente el mismo momento
             * que usa la lógica de valoración: inicio + 2 horas.
             *
             * El modo prueba solamente omite este chequeo temporal.
             */
            if (
                !modoPrueba &&
                momentoHabilitacion >
                ahora +
                TOLERANCIA_REPROGRAMACION_MILLIS
            ) {
                Log.w(
                    TAG,
                    "El horario cambió o el Worker llegó demasiado temprano. Reprogramando..."
                )

                ValoracionReminderScheduler
                    .programarRecordatorio(
                        context =
                            applicationContext,

                        partido =
                            partido,

                        usuarioEmail =
                            usuarioEmail
                    )

                return Result.success()
            }

            val valoracionId =
                crearValoracionId(
                    partidoId =
                        partidoId,

                    autorEmail =
                        usuarioEmail
                )

            Log.d(
                TAG,
                "Buscando valoraciones/$valoracionId..."
            )

            val yaValoro =
                db.collection("valoraciones")
                    .document(valoracionId)
                    .get()
                    .await()
                    .exists()

            Log.d(
                TAG,
                "yaValoro=$yaValoro"
            )

            if (yaValoro) {
                Log.d(
                    TAG,
                    "Fin: el usuario ya valoró."
                )

                ValoracionReminderScheduler
                    .marcarComoEjecutado(
                        context =
                            applicationContext,

                        partidoId =
                            partidoId
                    )

                return Result.success()
            }

            Log.d(
                TAG,
                ">>> MOSTRANDO NOTIFICACIÓN <<<"
            )

            AppNotificationHelper
                .showNotification(
                    context =
                        applicationContext,

                    title =
                        "¿Cómo estuvo el partido? ⭐",

                    message =
                        "Ya podés valorar tu experiencia en ${partido.titulo}.",

                    tipo =
                        TIPO_VALORAR_PARTIDO,

                    partidoId =
                        partidoId
                )

            ValoracionReminderScheduler
                .marcarComoEjecutado(
                    context =
                        applicationContext,

                    partidoId =
                        partidoId
                )

            Log.d(
                TAG,
                "Worker terminado correctamente."
            )

            Result.success()

        } catch (exception: Exception) {

            Log.e(
                TAG,
                "ERROR en Worker. WorkManager volverá a intentar.",
                exception
            )

            Result.retry()
        }
    }

    private fun crearValoracionId(
        partidoId: String,
        autorEmail: String
    ): String {

        val autorNormalizado =
            autorEmail
                .trim()
                .lowercase()

        val emailCodificado =
            Base64.encodeToString(
                autorNormalizado
                    .toByteArray(),

                Base64.URL_SAFE or
                        Base64.NO_WRAP or
                        Base64.NO_PADDING
            )

        return "${partidoId}_$emailCodificado"
    }

    private fun formatearFecha(
        millis: Long
    ): String {

        if (millis <= 0L) {
            return "INVALIDA($millis)"
        }

        return SimpleDateFormat(
            "dd/MM/yyyy HH:mm:ss",
            Locale.getDefault()
        ).format(
            Date(millis)
        )
    }

    companion object {

        private const val TAG =
            "VALORACION_REMINDER"

        const val KEY_PARTIDO_ID =
            "partido_id"

        const val KEY_USUARIO_EMAIL =
            "usuario_email"

        const val KEY_MODO_PRUEBA =
            "modo_prueba"

        const val TIPO_VALORAR_PARTIDO =
            "VALORAR_PARTIDO"

        private val DOS_HORAS_MILLIS =
            TimeUnit.HOURS.toMillis(2)

        private val TOLERANCIA_REPROGRAMACION_MILLIS =
            TimeUnit.MINUTES.toMillis(5)
    }
}