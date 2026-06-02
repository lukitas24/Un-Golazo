package com.example.futbolnomade.presentation.ui.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.futbolnomade.presentation.ui.components.SettingButton
import com.example.futbolnomade.presentation.ui.components.SettingToggle

private val FondoOscuro = Color(0xFF202020)
private val Verde       = Color(0xFF82A820)

@Composable
fun PerfilScreen(
    nombre: String,
    email: String,
    imageUri: String?,
    onEditarPerfil: () -> Unit,
    onAcercaDe: () -> Unit,
    onTerminos: () -> Unit,
    onCalificar: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    var darkMode       by remember { mutableStateOf(false) }
    var notificaciones by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoOscuro)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(8.dp))

        // ── Avatar ────────────────────────────────────────────────────────
        if (imageUri != null) {
            AsyncImage(
                model             = imageUri,
                contentDescription = "Foto de perfil",
                modifier          = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
            )
        } else {
            // Muestra la inicial del nombre en lugar del emoji genérico
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Verde),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = nombre.firstOrNull()?.uppercase() ?: "?",
                    color      = Color.White,
                    fontSize   = 36.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text       = nombre.ifBlank { "Usuario" },
            color      = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize   = 18.sp
        )
        Text(
            text  = email.ifBlank { "" },
            color = Color.LightGray,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onEditarPerfil,
            colors  = ButtonDefaults.buttonColors(containerColor = Verde)
        ) {
            Text("Editar perfil", color = Color.Black)
        }

        Spacer(Modifier.height(24.dp))

        HorizontalDivider(color = Color.Gray)

        Spacer(Modifier.height(16.dp))

        Text(
            text       = "Configuración",
            color      = Verde,
            fontWeight = FontWeight.Bold,
            modifier   = Modifier.align(Alignment.Start)
        )

        Spacer(Modifier.height(12.dp))

        SettingToggle(
            title          = "Modo oscuro",
            checked        = darkMode,
            onCheckedChange = { darkMode = it }
        )

        SettingToggle(
            title          = "Notificaciones",
            checked        = notificaciones,
            onCheckedChange = { notificaciones = it }
        )

        SettingButton("Acerca de", onAcercaDe)
        SettingButton("Términos y condiciones", onTerminos)
        SettingButton("Calificar la app", onCalificar)

        Spacer(Modifier.weight(1f))

        Spacer(Modifier.height(24.dp))

        Button(
            onClick  = onCerrarSesion,
            colors   = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión", color = Color.White)
        }
    }
}