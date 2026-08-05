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

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT
        ) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences ?: emptyList()

            triggeringGeofences.forEach { geofence ->
                val requestId = geofence.requestId
                val transitionType = if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) "ENTERED" else "EXITED"
                
                Log.d("GeofenceReceiver", "Geofence ID: $requestId - Transition: $transitionType")
                
                val title = if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) "¡Llegaste al partido!" else "¡Hasta la próxima!"
                val message = if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) 
                    "Has entrado al área del partido: $requestId" else 
                    "Has salido del área del partido: $requestId"

                NotificationHelper.showNotification(context, title, message)
            }
        } else {
            Log.e("GeofenceReceiver", "Invalid transition type: $geofenceTransition")
        }
    }
}
