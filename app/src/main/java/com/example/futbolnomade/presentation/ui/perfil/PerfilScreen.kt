package com.example.futbolnomade.presentation.ui.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.futbolnomade.presentation.ui.components.SettingButton
import com.example.futbolnomade.presentation.ui.components.SettingToggle

private val FondoOscuro = Color(0xFF202020)
private val Verde = Color(0xFF82A820)

@Composable
fun PerfilScreen(
    nombre: String,
    email: String,
    imageUri: String?, // 👈 IMPORTANTE: viene del ViewModel
    onEditarPerfil: () -> Unit,
    onAcercaDe: () -> Unit,
    onTerminos: () -> Unit,
    onCalificar: () -> Unit,
    onCerrarSesion: () -> Unit
) {

    var darkMode by remember { mutableStateOf(false) }
    var notificaciones by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoOscuro)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 👤 FOTO PERFIL (DINÁMICA)
        Box(contentAlignment = Alignment.Center) {

            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                )
            } else {
                Surface(
                    modifier = Modifier.size(90.dp),
                    shape = CircleShape,
                    color = Verde
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("👤")
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(nombre, color = Color.White, fontWeight = FontWeight.Bold)
        Text(email, color = Color.LightGray)

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onEditarPerfil,
            colors = ButtonDefaults.buttonColors(containerColor = Verde)
        ) {
            Text("Editar perfil", color = Color.Black)
        }

        Spacer(Modifier.height(24.dp))

        Divider(color = Color.Gray)

        Spacer(Modifier.height(16.dp))

        Text("Configuración", color = Verde, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(12.dp))

        SettingToggle(
            title = "Modo oscuro",
            checked = darkMode,
            onCheckedChange = { darkMode = it }
        )

        SettingToggle(
            title = "Notificaciones",
            checked = notificaciones,
            onCheckedChange = { notificaciones = it }
        )

        SettingButton("Acerca de", onAcercaDe)
        SettingButton("Términos y condiciones", onTerminos)
        SettingButton("Calificar la app", onCalificar)

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onCerrarSesion,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión", color = Color.White)
        }
    }
}