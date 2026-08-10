package com.example.futbolnomade.data.remote.notification

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest

class NotificationTokenRemoteDataSource(
    private val db: FirebaseFirestore =
        FirebaseFirestore.getInstance(),

    private val messaging: FirebaseMessaging =
        FirebaseMessaging.getInstance()
) {

    suspend fun registrarDispositivo(
        uid: String
    ) {
        if (uid.isBlank()) {
            return
        }

        val token =
            messaging.token.await()

        Log.d("FutbolNomadeFCM", "Registrando dispositivo. UID: $uid, Token: $token")

        guardarToken(
            uid = uid,
            token = token
        ).await()
    }

    fun actualizarToken(
        uid: String,
        token: String
    ) {
        if (
            uid.isBlank() ||
            token.isBlank()
        ) {
            return
        }

        guardarToken(
            uid = uid,
            token = token
        ).addOnFailureListener { exception ->
            Log.e(
                "FutbolNomadeFCM",
                "No se pudo actualizar el token FCM.",
                exception
            )
        }
    }

    suspend fun eliminarDispositivo(
        uid: String
    ) {
        if (uid.isBlank()) {
            return
        }

        val token =
            messaging.token.await()

        obtenerDocumentoDispositivo(
            uid = uid,
            token = token
        ).delete().await()
    }

    private fun guardarToken(
        uid: String,
        token: String
    ) = obtenerDocumentoDispositivo(
        uid = uid,
        token = token
    ).set(
        mapOf(
            "token" to token,
            "plataforma" to "android",
            "activo" to true,
            "actualizadoEn" to
                    FieldValue.serverTimestamp()
        ),
        SetOptions.merge()
    )

    private fun obtenerDocumentoDispositivo(
        uid: String,
        token: String
    ) = db
        .collection("jugadores")
        .document(uid)
        .collection("dispositivos")
        .document(crearIdSeguro(token))

    /*
     * No usamos el token completo como ID del documento.
     * Guardamos un hash SHA-256 para generar un ID seguro.
     */
    private fun crearIdSeguro(
        token: String
    ): String {
        val bytes =
            MessageDigest
                .getInstance("SHA-256")
                .digest(token.toByteArray())

        return bytes.joinToString("") { byte ->
            "%02x".format(
                byte.toInt() and 0xff
            )
        }
    }
}