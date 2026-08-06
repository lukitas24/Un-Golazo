package com.example.futbolnomade.presentation.notification

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun NotificationPermissionRequester(
    enabled: Boolean
) {
    if (
        Build.VERSION.SDK_INT <
        Build.VERSION_CODES.TIRAMISU
    ) {
        return
    }

    val context =
        LocalContext.current

    var requestedInThisExecution
            by rememberSaveable {
                mutableStateOf(false)
            }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts
                    .RequestPermission()
        ) {
            // Más adelante se puede guardar
            // si el usuario aceptó o rechazó.
        }

    LaunchedEffect(enabled) {
        if (
            enabled &&
            !requestedInThisExecution &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestedInThisExecution = true

            permissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
    }
}