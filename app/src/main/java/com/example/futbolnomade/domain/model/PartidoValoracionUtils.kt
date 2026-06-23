package com.example.futbolnomade.domain.model

import java.text.SimpleDateFormat
import java.util.Locale

private const val DOS_HORAS_MILLIS =
    2L * 60L * 60L * 1000L

fun calcularFechaHoraInicioMillis(
    fecha: String,
    horario: String
): Long {
    return try {
        val formato = SimpleDateFormat(
            "dd/MM/yyyy HH:mm",
            Locale.getDefault()
        )

        formato.isLenient = false

        val horarioLimpio = horario
            .trim()
            .substringBefore(" ")

        formato.parse(
            "${fecha.trim()} $horarioLimpio"
        )?.time ?: 0L
    } catch (_: Exception) {
        0L
    }
}

fun Partido.obtenerFechaHoraInicioMillis(): Long {
    return if (fechaHoraInicio > 0L) {
        fechaHoraInicio
    } else {
        calcularFechaHoraInicioMillis(
            fecha = fecha,
            horario = horario
        )
    }
}

fun Partido.puedeValorarse(
    ahora: Long = System.currentTimeMillis()
): Boolean {
    if (
        estado != EstadoPartido.PUBLICADO &&
        estado != EstadoPartido.RESERVA_APROBADA
    ) {
        return false
    }

    val inicio = obtenerFechaHoraInicioMillis()

    if (inicio <= 0L) {
        return false
    }

    return ahora >= inicio + DOS_HORAS_MILLIS
}