package com.example.futbolnomade.presentation.ui.perfil

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage
@Composable
fun EditarPerfilScreen(
    nombreActual: String,
    emailActual: String,
    onGuardar: (String, String, String, Uri?) -> Unit,
    onVolver: () -> Unit
) {

    var nombre by remember { mutableStateOf(nombreActual) }
    var email by remember { mutableStateOf(emailActual) }
    var password by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // 📷 launcher de galería
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text("Editar perfil", style = MaterialTheme.typography.headlineMedium)

        // 🖼 FOTO PERFIL
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                )
            } else {
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    color = Color.Gray
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("👤")
                    }
                }
            }
        }

        Button(
            onClick = {
                imagePicker.launch("image/*")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cambiar foto")
        }

        // 👤 Nombre
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre de usuario") },
            modifier = Modifier.fillMaxWidth()
        )

        // 📧 Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth()
        )

        // 🔒 Password (beta)
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Nueva contraseña (opcional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                onGuardar(nombre, email, password, imageUri)
                onVolver()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar cambios")
        }

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancelar")
        }
    }
}