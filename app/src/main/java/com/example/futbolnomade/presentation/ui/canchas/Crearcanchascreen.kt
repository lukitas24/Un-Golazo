package com.example.futbolnomade.presentation.ui.canchas


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CrearCanchaScreen(
    onCrearCancha: (
        nombre: String,
        ubicacion: String,
        descripcion: String,
        precio: String,
        telefono: String,
        horarioApertura: String,
        horarioCierre: String
    ) -> Unit,
    onVolver: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var horarioApertura by remember { mutableStateOf("") }
    var horarioCierre by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Subir mi cancha",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre de la cancha") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = ubicacion,
            onValueChange = { ubicacion = it },
            label = { Text("Ubicación") },
            placeholder = { Text("Ej: Puerto Madryn, Mitre 423") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = telefono,
            onValueChange = { telefono = it },
            label = { Text("Teléfono de contacto") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = precio,
            onValueChange = { precio = it },
            label = { Text("Precio por hora ($)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = horarioApertura,
                onValueChange = { horarioApertura = it },
                label = { Text("Apertura") },
                placeholder = { Text("HH:MM") },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = horarioCierre,
                onValueChange = { horarioCierre = it },
                label = { Text("Cierre") },
                placeholder = { Text("HH:MM") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción opcional") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (error != null) {
            Text(
                text = error!!,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = {
                val precioDouble = precio.toDoubleOrNull()

                when {
                    nombre.isBlank() -> error = "El nombre es obligatorio"
                    ubicacion.isBlank() -> error = "La ubicación es obligatoria"
                    telefono.isBlank() -> error = "El teléfono es obligatorio"
                    precioDouble == null || precioDouble <= 0 -> error = "El precio debe ser mayor a 0"
                    horarioApertura.isBlank() -> error = "El horario de apertura es obligatorio"
                    horarioCierre.isBlank() -> error = "El horario de cierre es obligatorio"
                    else -> {
                        error = null
                        onCrearCancha(
                            nombre.trim(),
                            ubicacion.trim(),
                            descripcion.trim(),
                            precio.trim(),
                            telefono.trim(),
                            horarioApertura.trim(),
                            horarioCierre.trim()
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Publicar cancha")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}