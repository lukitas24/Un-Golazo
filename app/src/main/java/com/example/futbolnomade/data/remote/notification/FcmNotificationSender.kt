package com.example.futbolnomade.data.remote.notification

import android.content.Context
import android.util.Log
import com.example.futbolnomade.R
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.InputStream

class FcmNotificationSender {

    private val db = FirebaseFirestore.getInstance()
    private val client = OkHttpClient()

    private suspend fun getAccessToken(context: Context): Pair<String, String> = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream = context.resources.openRawResource(R.raw.service_account)
            val jsonText = inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(jsonText)
            val projectId = json.getString("project_id")
            
            val credentials = GoogleCredentials.fromStream(context.resources.openRawResource(R.raw.service_account))
                .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
            
            credentials.refreshIfExpired()
            val token = credentials.accessToken.tokenValue
            Log.i("NOTIFICACION_CHECK", "✅ Access Token obtenido para: $projectId")
            Pair(token, projectId)
        } catch (e: Exception) {
            Log.e("NOTIFICACION_CHECK", "❌ Error al leer service_account.json: ${e.message}")
            throw e
        }
    }

    suspend fun enviarNotificacionAUsuario(context: Context, uid: String, titulo: String, mensaje: String, data: Map<String, String> = emptyMap(), fallbackEmail: String? = null) {
        withContext(Dispatchers.IO) {
            try {
                Log.i("NOTIFICACION_CHECK", "🚀 INICIO ENVÍO -> UID: $uid, Email: $fallbackEmail")
                
                var tokens: List<String> = emptyList()

                if (uid.isNotBlank()) {
                    val snapshot = db.collection("jugadores").document(uid).collection("dispositivos").whereEqualTo("activo", true).get().await()
                    tokens = snapshot.documents.mapNotNull { it.getString("token") }
                }

                if (tokens.isEmpty() && !fallbackEmail.isNullOrBlank()) {
                    val userQuery = db.collection("jugadores").whereEqualTo("email", fallbackEmail.trim().lowercase()).get().await()
                    val foundDoc = userQuery.documents.firstOrNull()
                    if (foundDoc != null) {
                        val snapshot = db.collection("jugadores").document(foundDoc.id).collection("dispositivos").whereEqualTo("activo", true).get().await()
                        tokens = snapshot.documents.mapNotNull { it.getString("token") }
                    }
                }

                if (tokens.isEmpty()) {
                    Log.w("NOTIFICACION_CHECK", "⚠️ No hay teléfonos registrados para el destino. Asegúrate que el destinatario haya abierto la app recientemente.")
                    return@withContext
                }

                val (accessToken, projectId) = getAccessToken(context)
                val url = "https://fcm.googleapis.com/v1/projects/$projectId/messages:send"
                
                tokens.forEach { token ->
                    enviarAFcmV1(url, token, titulo, mensaje, data, accessToken)
                }
            } catch (e: Exception) {
                Log.e("NOTIFICACION_CHECK", "❌ Error fatal enviando notificación", e)
            }
        }
    }

    private fun enviarAFcmV1(url: String, token: String, titulo: String, mensaje: String, dataMap: Map<String, String>, accessToken: String) {
        try {
            val json = JSONObject()
            val message = JSONObject()
            val notification = JSONObject()
            notification.put("title", titulo)
            notification.put("body", mensaje)
            
            val data = JSONObject()
            dataMap.forEach { (k, v) -> data.put(k, v) }

            val android = JSONObject()
            android.put("priority", "high")
            
            val androidNotification = JSONObject()
            androidNotification.put("channel_id", "activity_updates")
            android.put("notification", androidNotification)

            message.put("token", token)
            message.put("notification", notification)
            message.put("data", data)
            message.put("android", android)
            json.put("message", message)

            val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder().url(url).post(body).addHeader("Authorization", "Bearer $accessToken").build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: java.io.IOException) {
                    Log.e("NOTIFICACION_CHECK", "❌ Error de red al conectar con Google")
                }

                override fun onResponse(call: Call, response: Response) {
                    val rb = response.body?.string()
                    if (response.isSuccessful) {
                        Log.i("NOTIFICACION_CHECK", "✅ MENSAJE ENTREGADO A GOOGLE")
                    } else {
                        Log.e("NOTIFICACION_CHECK", "❌ FIREBASE RECHAZÓ EL MENSAJE: $rb")
                    }
                    response.close()
                }
            })
        } catch (e: Exception) {
            Log.e("NOTIFICACION_CHECK", "❌ Error al construir JSON", e)
        }
    }
}
