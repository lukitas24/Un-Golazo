package com.example.futbolnomade.presentation.ui.partidos

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CrearPartidoScreen(
    onCrearPartido: (
        titulo: String,
        horario: String,
        fecha: String,
        ubicacion: String,
        dificultad: String,
        participantes: Int,
        descripcion: String
    ) -> Unit,
    onVolver: () -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var horario by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var dificultad by remember { mutableStateOf("Ninguna") }
    var participantes by remember { mutableStateOf("10") }
    var descripcion by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Crear partido",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            label = { Text("Título") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = horario,
            onValueChange = { horario = it },
            label = { Text("Horario") },
            placeholder = { Text("HH:MM") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = fecha,
            onValueChange = { fecha = it },
            label = { Text("Fecha") },
            placeholder = { Text("DD/MM/AAAA") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = ubicacion,
            onValueChange = { ubicacion = it },
            label = { Text("Ubicación") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = dificultad,
            onValueChange = { dificultad = it },
            label = { Text("Dificultad") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = participantes,
            onValueChange = { participantes = it },
            label = { Text("Participantes máximos") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción opcional") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onCrearPartido(
                    titulo,
                    horario,
                    fecha,
                    ubicacion,
                    dificultad,
                    participantes.toIntOrNull() ?: 10,
                    descripcion
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear partido")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}