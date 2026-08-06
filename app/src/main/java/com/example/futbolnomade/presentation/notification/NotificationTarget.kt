package com.example.futbolnomade.presentation.notification

import android.content.Intent

data class NotificationTarget(
    val tipo: String? = null,
    val partidoId: String? = null,
    val canchaId: String? = null,
    val reservaId: String? = null
) {

    companion object {

        const val EXTRA_TIPO =
            "notification_tipo"

        const val EXTRA_PARTIDO_ID =
            "notification_partido_id"

        const val EXTRA_CANCHA_ID =
            "notification_cancha_id"

        const val EXTRA_RESERVA_ID =
            "notification_reserva_id"

        fun fromIntent(
            intent: Intent?
        ): NotificationTarget? {
            intent ?: return null

            val tipo =
                intent.getStringExtra(
                    EXTRA_TIPO
                )

            val partidoId =
                intent.getStringExtra(
                    EXTRA_PARTIDO_ID
                )

            val canchaId =
                intent.getStringExtra(
                    EXTRA_CANCHA_ID
                )

            val reservaId =
                intent.getStringExtra(
                    EXTRA_RESERVA_ID
                )

            val tieneDatos =
                !tipo.isNullOrBlank() ||
                        !partidoId.isNullOrBlank() ||
                        !canchaId.isNullOrBlank() ||
                        !reservaId.isNullOrBlank()

            if (!tieneDatos) {
                return null
            }

            return NotificationTarget(
                tipo = tipo,
                partidoId = partidoId,
                canchaId = canchaId,
                reservaId = reservaId
            )
        }
    }
}