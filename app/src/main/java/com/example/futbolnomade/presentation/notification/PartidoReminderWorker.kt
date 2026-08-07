package com.example.futbolnomade.presentation.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.futbolnomade.domain.model.EstadoPartido
import com.example.futbolnomade.domain.model.Partido
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class PartidoReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(
    appContext,
    workerParams
) {

    override suspend fun doWork(): Result {
        val partidoId =
            inputData.getString(KEY_PARTIDO_ID)
                ?: return Result.success()

        val usuarioEmail =
            inputData.getString(KEY_USUARIO_EMAIL)
                ?: return Result.success()

        return try {
            /*
             * Volvemos a leer el partido justo antes de notificar.
             *
             * Esto evita avisar si:
             * - el usuario se bajó;
             * - lo eliminaron;
             * - la reserva fue rechazada;
             * - el partido fue eliminado;
             * - cambiaron la fecha/hora.
             */
            val snapshot =
                FirebaseFirestore
                    .getInstance()
                    .collection("partidos")
                    .document(partidoId)
                    .get()
                    .await()

            val partido =
                snapshot.toObject(
                    Partido::class.java
                ) ?: return Result.success()

            val sigueParticipando =
                partido.usuariosAnotados.any {
                    it.trim().equals(
                        usuarioEmail.trim(),
                        ignoreCase = true
                    )
                }

            val partidoActivo =
                partido.estado ==
                        EstadoPartido.PUBLICADO ||
                        partido.estado ==
                        EstadoPartido.RESERVA_APROBADA

            if (
                !sigueParticipando ||
                !partidoActivo
            ) {
                PartidoReminderScheduler
                    .cancelarRecordatorio(
                        context = applicationContext,
                        partidoId = partidoId
                    )

                return Result.success()
            }

            val ahora =
                System.currentTimeMillis()

            if (
                partido.fechaHoraInicio <= ahora
            ) {
                return Result.success()
            }

            val momentoRecordatorio =
                partido.fechaHoraInicio -
                        DOS_HORAS_MILLIS

            /*
             * Si la fecha/hora del partido cambió y ahora el
             * recordatorio corresponde más adelante, lo reprogramamos.
             */
            if (
                momentoRecordatorio >
                ahora + TOLERANCIA_REPROGRAMACION_MILLIS
            ) {
                PartidoReminderScheduler
                    .programarRecordatorio(
                        context = applicationContext,
                        partido = partido,
                        usuarioEmail = usuarioEmail
                    )

                return Result.success()
            }

            val minutosRestantes =
                TimeUnit.MILLISECONDS
                    .toMinutes(
                        partido.fechaHoraInicio -
                                ahora
                    )
                    .coerceAtLeast(1L)

            val tituloNotificacion =
                if (minutosRestantes >= 90L) {
                    "Tu partido empieza en 2 horas ⚽"
                } else {
                    "Tu partido empieza pronto ⚽"
                }

            val lugar =
                partido.nombreCancha
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: partido.ubicacion
                        .takeIf {
                            it.isNotBlank()
                        }

            val mensaje =
                buildString {
                    append(partido.titulo)

                    if (!lugar.isNullOrBlank()) {
                        append(" en ")
                        append(lugar)
                    }

                    if (partido.horario.isNotBlank()) {
                        append(" comienza a las ")
                        append(partido.horario)
                    }

                    append(".")
                }

            AppNotificationHelper
                .showNotification(
                    context =
                        applicationContext,

                    title =
                        tituloNotificacion,

                    message =
                        mensaje,

                    tipo =
                        "RECORDATORIO_PARTIDO",

                    partidoId =
                        partido.id
                )

            PartidoReminderScheduler
                .marcarComoEjecutado(
                    context = applicationContext,
                    partidoId = partido.id
                )

            Result.success()

        } catch (exception: Exception) {
            /*
             * Si en ese momento no hay conexión o Firestore falla,
             * WorkManager podrá volver a intentarlo.
             */
            Result.retry()
        }
    }

    companion object {
        const val KEY_PARTIDO_ID =
            "partido_id"

        const val KEY_USUARIO_EMAIL =
            "usuario_email"

        private val DOS_HORAS_MILLIS =
            TimeUnit.HOURS.toMillis(2)

        private val TOLERANCIA_REPROGRAMACION_MILLIS =
            TimeUnit.MINUTES.toMillis(5)
    }
}