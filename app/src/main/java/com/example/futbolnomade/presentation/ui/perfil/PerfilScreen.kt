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
private val Verde = Color(0xFF82A820)

@Composable
fun PerfilScreen(
    nombre: String,
    email: String,
    imageUri: String?,
    biometricLinkedToCurrentAccount: Boolean,
    biometricLinkedToAnotherAccount: Boolean,
    onEditarPerfil: () -> Unit,
    onAcercaDe: () -> Unit,
    onTerminos: () -> Unit,
    onCalificar: () -> Unit,
    onUnlinkBiometric: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    var darkMode by remember {
        mutableStateOf(false)
    }
    var notificaciones by remember {
        mutableStateOf(true)
    }
    var showUnlinkConfirmation by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoOscuro)
            .verticalScroll(
                rememberScrollState()
            )
            .padding(24.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))

        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription =
                    "Foto de perfil",
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Verde),
                contentAlignment =
                    Alignment.Center
            ) {
                Text(
                    text =
                        nombre.firstOrNull()
                            ?.uppercase()
                            ?: "?",
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight =
                        FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text =
                nombre.ifBlank {
                    "Usuario"
                },
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )

        Text(
            text = email,
            color = Color.LightGray,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onEditarPerfil,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = Verde
                )
        ) {
            Text(
                "Editar perfil",
                color = Color.Black
            )
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider(color = Color.Gray)
        Spacer(Modifier.height(16.dp))

        Text(
            text = "Configuración",
            color = Verde,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(
                Alignment.Start
            )
        )

        Spacer(Modifier.height(12.dp))

        SettingToggle(
            title = "Modo oscuro",
            checked = darkMode,
            onCheckedChange = {
                darkMode = it
            }
        )

        SettingToggle(
            title = "Notificaciones",
            checked = notificaciones,
            onCheckedChange = {
                notificaciones = it
            }
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Seguridad",
            color = Verde,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(
                Alignment.Start
            )
        )

        Spacer(Modifier.height(10.dp))

        when {
            biometricLinkedToCurrentAccount -> {
                Text(
                    text =
                        "Esta cuenta está vinculada al ingreso biométrico de este dispositivo.",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        showUnlinkConfirmation = true
                    },
                    modifier =
                        Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Desvincular biometría"
                    )
                }
            }

            biometricLinkedToAnotherAccount -> {
                Text(
                    text =
                        "Otra cuenta ya está vinculada al ingreso biométrico de este dispositivo.",
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            else -> {
                Text(
                    text =
                        "No hay una cuenta vinculada. La próxima vez que ingreses con email y contraseña, la aplicación te ofrecerá activar la biometría.",
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        SettingButton(
            "Acerca de",
            onAcercaDe
        )
        SettingButton(
            "Términos y condiciones",
            onTerminos
        )
        SettingButton(
            "Calificar la app",
            onCalificar
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onCerrarSesion,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Cerrar sesión",
                color = Color.White
            )
        }
    }

    if (showUnlinkConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showUnlinkConfirmation = false
            },
            title = {
                Text(
                    "Desvincular biometría"
                )
            },
            text = {
                Text(
                    "Vas a eliminar el acceso biométrico guardado para esta cuenta. Para volver a activarlo tendrás que ingresar nuevamente con email y contraseña."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUnlinkConfirmation = false
                        onUnlinkBiometric()
                    }
                ) {
                    Text("Desvincular")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showUnlinkConfirmation = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}