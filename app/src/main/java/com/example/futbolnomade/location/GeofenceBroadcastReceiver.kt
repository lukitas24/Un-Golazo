package com.example.futbolnomade.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return

        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.e("GeofenceReceiver", "Error in geofencing event: $errorMessage")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition

        // Solo notificamos la ENTRADA para evitar spam cuando el sistema refresca geocercas
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences ?: emptyList()

            triggeringGeofences.forEach { geofence ->
                val requestId = geofence.requestId
                Log.d("GeofenceReceiver", "Entrando a geocerca: $requestId")
                
                val title = "¡Llegaste al partido! ⚽"
                val message = "Ya estás en el área de: $requestId. ¡A jugar!"

                NotificationHelper.showNotification(context, title, message)
            }
        }
else {
            Log.e("GeofenceReceiver", "Invalid transition type: $geofenceTransition")
        }
    }
}
