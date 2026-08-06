package com.example.futbolnomade.presentation.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.futbolnomade.MainActivity

object AppNotificationHelper {

    private const val CHANNEL_ID =
        "activity_updates"

    private const val CHANNEL_NAME =
        "Partidos y reservas"

    private const val CHANNEL_DESCRIPTION =
        "Cambios en partidos, reservas y canchas"

    fun showNotification(
        context: Context,
        title: String,
        message: String,
        tipo: String? = null,
        partidoId: String? = null,
        canchaId: String? = null,
        reservaId: String? = null
    ) {
        createNotificationChannel(
            context
        )

        val destinationIntent =
            Intent(
                context,
                MainActivity::class.java
            ).apply {
                flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP

                putExtra(
                    NotificationTarget.EXTRA_TIPO,
                    tipo
                )

                putExtra(
                    NotificationTarget.EXTRA_PARTIDO_ID,
                    partidoId
                )

                putExtra(
                    NotificationTarget.EXTRA_CANCHA_ID,
                    canchaId
                )

                putExtra(
                    NotificationTarget.EXTRA_RESERVA_ID,
                    reservaId
                )
            }

        val requestCode =
            (
                    partidoId
                        ?: canchaId
                        ?: reservaId
                        ?: System.currentTimeMillis()
                            .toString()
                    ).hashCode()

        val pendingIntent =
            PendingIntent.getActivity(
                context,
                requestCode,
                destinationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val notification =
            NotificationCompat.Builder(
                context,
                CHANNEL_ID
            )
                .setSmallIcon(
                    android.R.drawable.ic_dialog_info
                )
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(
                    NotificationCompat
                        .BigTextStyle()
                        .bigText(message)
                )
                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )
                .setCategory(
                    NotificationCompat.CATEGORY_EVENT
                )
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

        val permissionGranted =
            Build.VERSION.SDK_INT <
                    Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted) {
            return
        }

        val notificationId =
            (
                    System.currentTimeMillis() %
                            Int.MAX_VALUE
                    ).toInt()

        NotificationManagerCompat
            .from(context)
            .notify(
                notificationId,
                notification
            )
    }

    private fun createNotificationChannel(
        context: Context
    ) {
        if (
            Build.VERSION.SDK_INT <
            Build.VERSION_CODES.O
        ) {
            return
        }

        val channel =
            NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description =
                    CHANNEL_DESCRIPTION
            }

        val notificationManager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        notificationManager
            .createNotificationChannel(
                channel
            )
    }
}