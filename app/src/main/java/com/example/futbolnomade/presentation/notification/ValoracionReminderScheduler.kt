package com.example.futbolnomade.presentation.notification

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.futbolnomade.domain.model.EstadoPartido
import com.example.futbolnomade.domain.model.Partido
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object ValoracionReminderScheduler {

    private const val TAG =
        "VALORACION_REMINDER"

    private const val PREFS_NAME =
        "valoracion_reminders"

    private const val KEY_PARTIDOS_PROGRAMADOS =
        "partidos_programados"

    private const val WORK_PREFIX =
        "recordatorio_valorar_partido_"

    private const val TAG_RECORDATORIOS =
        "recordatorios_valoracion"

    private val DOS_HORAS_MILLIS =
        TimeUnit.HOURS.toMillis(2)

    fun sincronizarRecordatorios(
        context: Context,
        partidos: List<Partido>,
        usuarioEmail: String,
        partidosYaValorados: Set<String>
    ) {
        Log.d(
            TAG,
            "=== SINCRONIZAR === usuario=$usuarioEmail partidos=${partidos.size}"
        )

        if (usuarioEmail.isBlank()) {
            Log.d(
                TAG,
                "Email vacío -> cancelo todos los recordatorios."
            )

            cancelarTodos(context)
            return
        }

        val ahora =
            System.currentTimeMillis()

        Log.d(
            TAG,
            "Ahora: ${formatearFecha(ahora)} ($ahora)"
        )

        val partidosProgramables =
            partidos.filter { partido ->

                val participa =
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

                val todaviaNoValoro =
                    partido.id !in
                            partidosYaValorados

                val momentoHabilitacion =
                    partido.fechaHoraInicio +
                            DOS_HORAS_MILLIS

                val avisoEsFuturo =
                    partido.fechaHoraInicio > 0L &&
                            momentoHabilitacion > ahora

                Log.d(
                    TAG,
                    """
                    Partido ${partido.id} - ${partido.titulo}
                    inicio=${formatearFecha(partido.fechaHoraInicio)}
                    habilitacion=${formatearFecha(momentoHabilitacion)}
                    participa=$participa
                    estado=${partido.estado}
                    partidoValido=$partidoValido
                    todaviaNoValoro=$todaviaNoValoro
                    avisoEsFuturo=$avisoEsFuturo
                    """.trimIndent()
                )

                participa &&
                        partidoValido &&
                        todaviaNoValoro &&
                        avisoEsFuturo
            }

        Log.d(
            TAG,
            "Partidos programables=${partidosProgramables.size}"
        )

        val idsNuevos =
            partidosProgramables
                .map { it.id }
                .filter { it.isNotBlank() }
                .toSet()

        val idsAnteriores =
            obtenerIdsProgramados(context)

        (idsAnteriores - idsNuevos)
            .forEach { partidoId ->

                Log.d(
                    TAG,
                    "Cancelo work que ya no corresponde: $partidoId"
                )

                cancelarWork(
                    context = context,
                    partidoId = partidoId
                )
            }

        partidosProgramables
            .forEach { partido ->

                programarRecordatorio(
                    context = context,
                    partido = partido,
                    usuarioEmail = usuarioEmail,
                    guardarEnPreferencias = false
                )
            }

        guardarIdsProgramados(
            context = context,
            ids = idsNuevos
        )

        Log.d(
            TAG,
            "=== FIN SINCRONIZAR === ids=$idsNuevos"
        )
    }

    fun programarRecordatorio(
        context: Context,
        partido: Partido,
        usuarioEmail: String,
        guardarEnPreferencias: Boolean = true
    ) {
        if (
            partido.id.isBlank() ||
            usuarioEmail.isBlank() ||
            partido.fechaHoraInicio <= 0L
        ) {
            Log.w(
                TAG,
                "NO programado: datos inválidos. id=${partido.id}, email=$usuarioEmail, inicio=${partido.fechaHoraInicio}"
            )

            return
        }

        val momentoHabilitacion =
            partido.fechaHoraInicio +
                    DOS_HORAS_MILLIS

        val ahora =
            System.currentTimeMillis()

        val demora =
            momentoHabilitacion -
                    ahora

        Log.d(
            TAG,
            """
            PROGRAMAR ${partido.id}
            titulo=${partido.titulo}
            ahora=${formatearFecha(ahora)}
            inicio=${formatearFecha(partido.fechaHoraInicio)}
            objetivo=${formatearFecha(momentoHabilitacion)}
            demoraMs=$demora
            demoraMin=${TimeUnit.MILLISECONDS.toMinutes(demora)}
            """.trimIndent()
        )

        if (demora <= 0L) {
            Log.w(
                TAG,
                "NO programado: el momento de valoración ya pasó."
            )

            return
        }

        val datos =
            Data.Builder()
                .putString(
                    ValoracionReminderWorker.KEY_PARTIDO_ID,
                    partido.id
                )
                .putString(
                    ValoracionReminderWorker.KEY_USUARIO_EMAIL,
                    usuarioEmail
                )
                .putBoolean(
                    ValoracionReminderWorker.KEY_MODO_PRUEBA,
                    false
                )
                .build()

        val trabajo =
            OneTimeWorkRequestBuilder<
                    ValoracionReminderWorker
                    >()
                .setInitialDelay(
                    demora,
                    TimeUnit.MILLISECONDS
                )
                .setInputData(datos)
                .addTag(TAG_RECORDATORIOS)
                .build()

        WorkManager
            .getInstance(context)
            .enqueueUniqueWork(
                nombreWork(partido.id),
                ExistingWorkPolicy.REPLACE,
                trabajo
            )

        Log.d(
            TAG,
            "Work ENCOLADO. workId=${trabajo.id} uniqueName=${nombreWork(partido.id)}"
        )

        if (guardarEnPreferencias) {
            val ids =
                obtenerIdsProgramados(context)
                    .toMutableSet()

            ids.add(partido.id)

            guardarIdsProgramados(
                context = context,
                ids = ids
            )
        }
    }

    /*
     * SOLO PARA DIAGNÓSTICO.
     *
     * Programa el mismo Worker a 1 minuto, pero omite únicamente
     * la espera fechaHoraInicio + 2h. Sigue comprobando:
     * - usuario logueado correcto,
     * - participación,
     * - estado del partido,
     * - si ya valoró.
     *
     * Podés llamarlo temporalmente desde un botón de prueba.
     */
    fun programarPruebaEnUnMinuto(
        context: Context,
        partido: Partido,
        usuarioEmail: String
    ) {
        if (
            partido.id.isBlank() ||
            usuarioEmail.isBlank()
        ) {
            Log.w(
                TAG,
                "No se pudo programar prueba: datos inválidos."
            )
            return
        }

        val datos =
            Data.Builder()
                .putString(
                    ValoracionReminderWorker.KEY_PARTIDO_ID,
                    partido.id
                )
                .putString(
                    ValoracionReminderWorker.KEY_USUARIO_EMAIL,
                    usuarioEmail
                )
                .putBoolean(
                    ValoracionReminderWorker.KEY_MODO_PRUEBA,
                    true
                )
                .build()

        val trabajo =
            OneTimeWorkRequestBuilder<
                    ValoracionReminderWorker
                    >()
                .setInitialDelay(
                    1,
                    TimeUnit.MINUTES
                )
                .setInputData(datos)
                .addTag(TAG_RECORDATORIOS)
                .build()

        WorkManager
            .getInstance(context)
            .enqueueUniqueWork(
                "${WORK_PREFIX}TEST_${partido.id}",
                ExistingWorkPolicy.REPLACE,
                trabajo
            )

        Log.d(
            TAG,
            "PRUEBA programada para ~1 minuto. partido=${partido.id} workId=${trabajo.id}"
        )
    }

    fun cancelarRecordatorio(
        context: Context,
        partidoId: String
    ) {
        Log.d(
            TAG,
            "Cancelar recordatorio partido=$partidoId"
        )

        cancelarWork(
            context = context,
            partidoId = partidoId
        )

        val ids =
            obtenerIdsProgramados(context)
                .toMutableSet()

        ids.remove(partidoId)

        guardarIdsProgramados(
            context = context,
            ids = ids
        )
    }

    fun cancelarTodos(
        context: Context
    ) {
        Log.d(
            TAG,
            "Cancelar TODOS los recordatorios de valoración."
        )

        WorkManager
            .getInstance(context)
            .cancelAllWorkByTag(
                TAG_RECORDATORIOS
            )

        guardarIdsProgramados(
            context = context,
            ids = emptySet()
        )
    }

    internal fun marcarComoEjecutado(
        context: Context,
        partidoId: String
    ) {
        Log.d(
            TAG,
            "Marcar ejecutado partido=$partidoId"
        )

        val ids =
            obtenerIdsProgramados(context)
                .toMutableSet()

        ids.remove(partidoId)

        guardarIdsProgramados(
            context = context,
            ids = ids
        )
    }

    private fun cancelarWork(
        context: Context,
        partidoId: String
    ) {
        WorkManager
            .getInstance(context)
            .cancelUniqueWork(
                nombreWork(partidoId)
            )
    }

    private fun nombreWork(
        partidoId: String
    ): String {
        return "$WORK_PREFIX$partidoId"
    }

    private fun obtenerIdsProgramados(
        context: Context
    ): Set<String> {
        return context
            .getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
            .getStringSet(
                KEY_PARTIDOS_PROGRAMADOS,
                emptySet()
            )
            ?.toSet()
            ?: emptySet()
    }

    private fun guardarIdsProgramados(
        context: Context,
        ids: Set<String>
    ) {
        context
            .getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
            .edit()
            .putStringSet(
                KEY_PARTIDOS_PROGRAMADOS,
                ids
            )
            .apply()
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
}