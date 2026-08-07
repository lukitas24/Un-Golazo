package com.example.futbolnomade.presentation.notification

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.futbolnomade.domain.model.EstadoPartido
import com.example.futbolnomade.domain.model.Partido
import java.util.concurrent.TimeUnit

object PartidoReminderScheduler {

    private const val PREFS_NAME =
        "partido_reminders"

    private const val KEY_PARTIDOS_PROGRAMADOS =
        "partidos_programados"

    private const val WORK_PREFIX =
        "recordatorio_partido_"

    private const val TAG_RECORDATORIOS =
        "recordatorios_partidos"

    private val DOS_HORAS_MILLIS =
        TimeUnit.HOURS.toMillis(2)

    /*
     * Sincroniza todos los recordatorios correspondientes
     * al usuario actualmente logueado.
     *
     * Se puede llamar cada vez que cambia la lista de partidos.
     */
    fun sincronizarRecordatorios(
        context: Context,
        partidos: List<Partido>,
        usuarioEmail: String
    ) {
        if (usuarioEmail.isBlank()) {
            cancelarTodos(context)
            return
        }

        val ahora =
            System.currentTimeMillis()

        val partidosProgramables =
            partidos.filter { partido ->

                val participa =
                    partido.usuariosAnotados.any {
                        it.trim().equals(
                            usuarioEmail.trim(),
                            ignoreCase = true
                        )
                    }

                val activo =
                    partido.estado ==
                            EstadoPartido.PUBLICADO ||
                            partido.estado ==
                            EstadoPartido.RESERVA_APROBADA

                val recordatorioTodaviaNoPaso =
                    partido.fechaHoraInicio -
                            DOS_HORAS_MILLIS >
                            ahora

                participa &&
                        activo &&
                        recordatorioTodaviaNoPaso
            }

        val idsNuevos =
            partidosProgramables
                .map {
                    it.id
                }
                .filter {
                    it.isNotBlank()
                }
                .toSet()

        val idsAnteriores =
            obtenerIdsProgramados(
                context
            )

        /*
         * Cancelamos recordatorios de partidos que ya no
         * corresponden al usuario.
         */
        (idsAnteriores - idsNuevos)
            .forEach { partidoId ->
                cancelarWork(
                    context = context,
                    partidoId = partidoId
                )
            }

        /*
         * REPLACE hace que si cambió la fecha/hora se reemplace
         * automáticamente la programación anterior.
         */
        partidosProgramables
            .forEach { partido ->
                programarRecordatorio(
                    context = context,
                    partido = partido,
                    usuarioEmail =
                        usuarioEmail,
                    guardarEnPreferencias =
                        false
                )
            }

        guardarIdsProgramados(
            context = context,
            ids = idsNuevos
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
            usuarioEmail.isBlank()
        ) {
            return
        }

        val ahora =
            System.currentTimeMillis()

        val momentoRecordatorio =
            partido.fechaHoraInicio -
                    DOS_HORAS_MILLIS

        val demora =
            momentoRecordatorio - ahora

        /*
         * Si ya faltan menos de dos horas no generamos un aviso
         * diciendo incorrectamente que faltan dos horas.
         */
        if (demora <= 0L) {
            cancelarRecordatorio(
                context = context,
                partidoId = partido.id
            )
            return
        }

        val datos =
            Data.Builder()
                .putString(
                    PartidoReminderWorker
                        .KEY_PARTIDO_ID,
                    partido.id
                )
                .putString(
                    PartidoReminderWorker
                        .KEY_USUARIO_EMAIL,
                    usuarioEmail
                )
                .build()

        val trabajo =
            OneTimeWorkRequestBuilder<
                    PartidoReminderWorker
                    >()
                .setInitialDelay(
                    demora,
                    TimeUnit.MILLISECONDS
                )
                .setInputData(
                    datos
                )
                .addTag(
                    TAG_RECORDATORIOS
                )
                .build()

        WorkManager
            .getInstance(context)
            .enqueueUniqueWork(
                nombreWork(
                    partido.id
                ),
                ExistingWorkPolicy.REPLACE,
                trabajo
            )

        if (guardarEnPreferencias) {
            val ids =
                obtenerIdsProgramados(
                    context
                )
                    .toMutableSet()

            ids.add(
                partido.id
            )

            guardarIdsProgramados(
                context = context,
                ids = ids
            )
        }
    }

    fun cancelarRecordatorio(
        context: Context,
        partidoId: String
    ) {
        cancelarWork(
            context = context,
            partidoId = partidoId
        )

        val ids =
            obtenerIdsProgramados(
                context
            )
                .toMutableSet()

        ids.remove(
            partidoId
        )

        guardarIdsProgramados(
            context = context,
            ids = ids
        )
    }

    fun cancelarTodos(
        context: Context
    ) {
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
        val ids =
            obtenerIdsProgramados(
                context
            )
                .toMutableSet()

        ids.remove(
            partidoId
        )

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
                nombreWork(
                    partidoId
                )
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
}