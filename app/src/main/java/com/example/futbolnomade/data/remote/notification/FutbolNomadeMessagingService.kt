package com.example.futbolnomade.data.remote.notification

import android.util.Log
import com.example.futbolnomade.data.repository.NotificationRepositoryImpl
import com.example.futbolnomade.presentation.notification.AppNotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FutbolNomadeMessagingService :
    FirebaseMessagingService() {

    private val notificationRepository =
        NotificationRepositoryImpl()

    override fun onNewToken(
        token: String
    ) {
        super.onNewToken(token)

        Log.d(
            "FutbolNomadeFCM",
            "Se generó un nuevo token FCM."
        )

        val uid =
            FirebaseAuth
                .getInstance()
                .currentUser
                ?.uid
                ?: return

        notificationRepository
            .actualizarToken(
                uid = uid,
                token = token
            )
    }

    override fun onMessageReceived(
        message: RemoteMessage
    ) {
        super.onMessageReceived(message)
        
        Log.i("NOTIFICACION_CHECK", "📩 ¡MENSAJE RECIBIDO! De: ${message.from}")

        val data =
            message.data

        val title =
            message.notification?.title
                ?: data["titulo"]
                ?: "Fútbol Nómade"

        val body =
            message.notification?.body
                ?: data["mensaje"]
                ?: "Tenés una nueva actualización."

        val tipo =
            data["tipo"]

        val partidoId =
            data["partidoId"]

        val canchaId =
            data["canchaId"]

        val reservaId =
            data["reservaId"]

        AppNotificationHelper
            .showNotification(
                context = this,
                title = title,
                message = body,
                tipo = tipo,
                partidoId = partidoId,
                canchaId = canchaId,
                reservaId = reservaId
            )
    }
}