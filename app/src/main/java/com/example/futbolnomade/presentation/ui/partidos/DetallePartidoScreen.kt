package com.example.futbolnomade.presentation.ui.partidos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.futbolnomade.domain.model.Partido
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

// PALETA DE COLORES OFICIAL DE FÚTBOL NÓMADE
private val FondoOscuro = Color(0xFF202020)
private val VerdeMetálico = Color(0xFF82A820)
private val GrisContenedor = Color(0xFF2E2E2E)

@Composable
fun DetallePartidoScreen(
    partido: Partido?,
    usuarioActual: String,
    onAnotarse: (Int, String) -> Boolean,
    onCancelarInscripcion: (Int, String) -> Boolean,
    onVolver: () -> Unit
) {
    if (partido == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(FondoOscuro)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("No se encontró el partido", color = Color.White, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onVolver,
                colors = ButtonDefaults.buttonColors(containerColor = VerdeMetálico),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Volver", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    // --- PARSEO INTELIGENTE DE COORDENADAS ---
    val coordenadasPartido = remember(partido.ubicacion) {
        try {
            val partes = partido.ubicacion.split(",")
            if (partes.size == 2) {
                LatLng(partes[0].trim().toDouble(), partes[1].trim().toDouble())
            } else {
                LatLng(-34.6037, -58.3816)
            }
        } catch (e: Exception) {
            LatLng(-34.6037, -58.3816)
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(coordenadasPartido, 15f)
    }

    val yaEstaAnotado = partido.usuariosAnotados.contains(usuarioActual)
    val partidoLleno = partido.participantesActuales >= partido.participantesMaximos

    val scrollState = rememberScrollState()
    var scrollHabilitado by remember { mutableStateOf(true) }

    // Apaga el scroll de la pantalla si el usuario arrastra el mapa
    LaunchedEffect(cameraPositionState.isMoving) {
        scrollHabilitado = !cameraPositionState.isMoving
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoOscuro)
            // SOLUCIÓN: El scroll condicional va AQUÍ, controlando el contenedor de toda la pantalla entera
            .verticalScroll(scrollState, enabled = scrollHabilitado)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        // TÍTULO DEL PARTIDO
        Text(
            text = partido.titulo,
            color = VerdeMetálico,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        // CREADOR INFO
        Text(
            text = "Organizado por: ${partido.creador}  ⭐ ${partido.calificacionCreador}",
            color = Color.LightGray,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        // CONTENEDOR ESTILO FICHA INTERNA
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(GrisContenedor, shape = RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Fila: Horario y Fecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    Text("📅 Fecha: ", color = VerdeMetálico, fontWeight = FontWeight.Bold)
                    Text(partido.fecha, color = Color.White)
                }
                Row {
                    Text("🕒 Horario: ", color = VerdeMetálico, fontWeight = FontWeight.Bold)
                    Text("${partido.horario} hs", color = Color.White)
                }
            }

            Divider(color = FondoOscuro, thickness = 1.dp)

            // Fila: Dificultad y Cupos
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("🔥 Dificultad: ", color = VerdeMetálico, fontWeight = FontWeight.Bold)
                Text(partido.dificultad, color = Color.White)
                Spacer(modifier = Modifier.weight(1f))
                Text("👥 Cupos: ", color = VerdeMetálico, fontWeight = FontWeight.Bold)
                Text("${partido.participantesActuales} / ${partido.participantesMaximos}", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // DESCRIPCIÓN
        if (partido.descripcion.isNotBlank()) {
            Text("Descripción", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GrisContenedor, shape = RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(partido.descripcion, color = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // SECCIÓN LUGAR DE ENCUENTRO Y MAPA
        Text("Punto de encuentro", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // SOLUCIÓN: El Box ahora queda limpio y libre de scrolls anidados redundantes
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(285.dp)
                .background(GrisContenedor, shape = RoundedCornerShape(8.dp))
                .border(1.dp, GrisContenedor, RoundedCornerShape(8.dp))
                .padding(vertical = 8.dp)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    scrollGesturesEnabled = true
                ),
                properties = MapProperties(isMyLocationEnabled = false)
            ) {
                Marker(
                    state = MarkerState(position = coordenadasPartido),
                    title = "Lugar del partido"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SECCIÓN PARTICIPANTES ANOTADOS
        Text("Lista de convocados", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(GrisContenedor, shape = RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (partido.usuariosAnotados.isEmpty()) {
                Text("Todavía no hay participantes anotados", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
            } else {
                partido.usuariosAnotados.forEach { usuario ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⚽", modifier = Modifier.padding(end = 8.dp))
                        Text(
                            text = usuario,
                            color = if (usuario == usuarioActual) VerdeMetálico else Color.White,
                            fontWeight = if (usuario == usuarioActual) FontWeight.Bold else FontWeight.Normal
                        )
                        if (usuario == usuarioActual) {
                            Text(" (Tú)", color = VerdeMetálico, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // BOTÓN PRINCIPAL ACCIÓN (Inscribirse / Cancelar)
        val containerColorBoton = if (yaEstaAnotado) Color(0xFFC62828) else VerdeMetálico
        val contentColorBoton = if (yaEstaAnotado) Color.White else Color.Black

        Button(
            onClick = {
                if (yaEstaAnotado) {
                    onCancelarInscripcion(partido.id, usuarioActual)
                } else {
                    onAnotarse(partido.id, usuarioActual)
                }
            },
            enabled = !partidoLleno || yaEstaAnotado,
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColorBoton,
                disabledContainerColor = Color.DarkGray
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = when {
                    yaEstaAnotado -> "Cancelar inscripción"
                    partidoLleno -> "Partido lleno"
                    else -> "Anotarme al partido"
                },
                color = if (!partidoLleno || yaEstaAnotado) contentColorBoton else Color.Gray,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // BOTÓN VOLVER
        OutlinedButton(
            onClick = onVolver,
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Volver", color = Color.White, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
        }
    }
}