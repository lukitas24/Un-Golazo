package com.example.futbolnomade.presentation.ui.partidos

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.futbolnomade.domain.model.Partido

@Composable
fun DetallePartidoScreen(
    partido: Partido?,
    usuarioActual: String,
    onAnotarse: (String, String) -> Unit,
    onCancelarInscripcion: (String, String) -> Unit,
    onVolver: () -> Unit
)
{
    if (partido == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text("No se encontró el partido")

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onVolver) {
                Text("Volver")
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = partido.titulo,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Horario: ${partido.horario} hs")
        Text("Fecha: ${partido.fecha}")
        Text("Ubicación: ${partido.ubicacion}")
        Text("Dificultad: ${partido.dificultad}")
        Text("Participantes: ${partido.participantesActuales}/${partido.participantesMaximos}")
        Text("Creado por: ${partido.creador}")
        Text("Calificación creador: ⭐ ${partido.calificacionCreador}")

        if (partido.descripcion.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Descripción:")
            Text(partido.descripcion)
        }

        Spacer(modifier = Modifier.height(24.dp))

        val yaEstaAnotado = partido.usuariosAnotados.contains(usuarioActual)
        val partidoLleno = partido.participantesActuales >= partido.participantesMaximos

        Button(
            onClick = {
                if (yaEstaAnotado) {
                    onCancelarInscripcion(partido.id, usuarioActual)
                } else {
                    onAnotarse(partido.id, usuarioActual)
                }
            },
            enabled = !partidoLleno || yaEstaAnotado,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                when {
                    yaEstaAnotado -> "Cancelar inscripción"
                    partidoLleno -> "Partido lleno"
                    else -> "Anotarme"
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Participantes",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (partido.usuariosAnotados.isEmpty()) {
            Text("Todavía no hay participantes anotados")
        } else {
            partido.usuariosAnotados.forEach { usuario ->
                Text("• $usuario")
            }
        }
    }
}