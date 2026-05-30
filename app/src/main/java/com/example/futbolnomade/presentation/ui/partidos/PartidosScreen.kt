package com.example.futbolnomade.presentation.ui.partidos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.futbolnomade.domain.model.Partido
import com.example.futbolnomade.presentation.state.PartidoUiState

@Composable
fun PartidosScreen(
    uiState: PartidoUiState,
    onCrearPartido: () -> Unit,
    onVerDetalle: (Int) -> Unit,
    onVolver: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Partidos",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onCrearPartido,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear partido")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(uiState.partidos) { partido ->
                PartidoCard(
                    partido = partido,
                    onClick = {
                        onVerDetalle(partido.id)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}

@Composable
fun PartidoCard(
    partido: Partido,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = partido.titulo,
                style = MaterialTheme.typography.titleLarge
            )

            Text("${partido.horario} hs - ${partido.fecha}")
            Text(partido.dificultad)
            Text(partido.ubicacion)
            Text("${partido.participantesActuales}/${partido.participantesMaximos} participantes")
            Text("Creado por: ${partido.creador} ⭐ ${partido.calificacionCreador}")
        }
    }
}