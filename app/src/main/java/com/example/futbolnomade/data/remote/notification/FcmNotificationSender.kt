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
        val inputStream: InputStream = context.resources.openRawResource(R.raw.service_account)
        val json = JSONObject(inputStream.bufferedReader().use { it.readText() })
        val projectId = json.getString("project_id")
        
        val credentials = GoogleCredentials.fromStream(context.resources.openRawResource(R.raw.service_account))
            .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
        
        credentials.refreshIfExpired()
        Pair(credentials.accessToken.tokenValue, projectId)
    }

    suspend fun enviarNotificacionAUsuario(context: Context, uid: String, titulo: String, mensaje: String, data: Map<String, String> = emptyMap()) {
        withContext(Dispatchers.IO) {
            try {
                Log.d("FCM_SENDER", "Preparando notificación para UID: $uid")
                
                val snapshot = db.collection("jugadores")
                    .document(uid)
                    .collection("dispositivos")
                    .whereEqualTo("activo", true)
                    .get()
                    .await()

                val tokens = snapshot.documents.mapNotNull { it.getString("token") }
                Log.d("FCM_SENDER", "Tokens activos para $uid: ${tokens.size}")
                
                if (tokens.isEmpty()) {
                    Log.w("FCM_SENDER", "El usuario $uid no tiene dispositivos vinculados.")
                    return@withContext
                }

                val (accessToken, projectId) = getAccessToken(context)
                val fcmUrl = "https://fcm.googleapis.com/v1/projects/$projectId/messages:send"
                
                tokens.forEach { token ->
                    enviarAFcmV1(fcmUrl, token, titulo, mensaje, data, accessToken)
                }
            } catch (e: Exception) {
                Log.e("FCM_SENDER", "Falla en el proceso de notificación", e)
            }
        }
    }

    private fun enviarAFcmV1(url: String, token: String, titulo: String, mensaje: String, dataMap: Map<String, String>, accessToken: String) {
        val json = JSONObject()
        val message = JSONObject()
        
        val notification = JSONObject()
        notification.put("title", titulo)
        notification.put("body", mensaje)
        
        val data = JSONObject()
        dataMap.forEach { (k, v) -> data.put(k, v) }

        val android = JSONObject()
        val androidNotification = JSONObject()
        androidNotification.put("channel_id", "activity_updates")
        androidNotification.put("priority", "high")
        android.put("notification", androidNotification)

        message.put("token", token)
        message.put("notification", notification)
        message.put("data", data)
        message.put("android", android)
        
        json.put("message", message)

        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                Log.e("FCM_SENDER", "Falla de red enviando a $token", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful) {
                    Log.d("FCM_SENDER", "Notificación enviada con éxito a $token")
                } else {
                    Log.e("FCM_SENDER", "Error de Firebase ($token): $responseBody")
                }
                response.close()
            }
        })
    }
}
