package com.example.futbolnomade.presentation.ui.canchas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.futbolnomade.domain.model.Cancha
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

// PALETA DE COLORES OFICIAL DE FÚTBOL NÓMADE
private val ColorFondo   = Color(0xFF1A1A1A)
private val ColorCampo   = Color(0xFF2A2A2A)
private val ColorBorde   = Color(0xFF2E2E2E)
private val ColorVerde   = Color(0xFF8BC34A)
private val ColorTexto   = Color(0xFFEEEEEE)
private val ColorSub     = Color(0xFF999999)

@Composable
fun DetalleCanchaScreen(
    cancha: Cancha?,
    // Lista de strings con los horarios ya reservados hoy, ej: listOf("18:00", "21:00")
    turnosReservados: List<String> = emptyList(),
    onReservarTurno: (canchaId: Int, hora: String) -> Unit,
    onVolver: () -> Unit
) {
    if (cancha == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorFondo)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("No se encontró la cancha", color = ColorTexto, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onVolver,
                colors = ButtonDefaults.buttonColors(containerColor = ColorVerde),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Volver", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    // --- PARSEO INTELIGENTE DE COORDENADAS ---
    val coordenadasCancha = remember(cancha.ubicacion) {
        try {
            val partes = cancha.ubicacion.split(",")
            if (partes.size == 2) {
                LatLng(partes[0].trim().toDouble(), partes[1].trim().toDouble())
            } else {
                LatLng(-42.7692, -65.0385) // Fallback Puerto Madryn
            }
        } catch (e: Exception) {
            LatLng(-42.7692, -65.0385)
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(coordenadasCancha, 15f)
    }

    // --- GENERACIÓN DINÁMICA DE INTERVALOS HORARIOS (De 1 en 1 hora) ---
    val listadoTurnosDía = remember(cancha.horarioApertura, cancha.horarioCierre) {
        val turnos = mutableListOf<String>()
        try {
            val aperturaItem = cancha.horarioApertura.split(":")
            val cierreItem = cancha.horarioCierre.split(":")

            var horaActual = aperturaItem[0].toInt()
            val horaFin = cierreItem[0].toInt()

            // Manejo por si cierra pasada la medianoche (ej: abre 08, cierra 02)
            val horaLimite = if (horaFin < horaActual) horaFin + 24 else horaFin

            while (horaActual < horaLimite) {
                val horaNormalizada = horaActual % 24
                val stringHora = String.format(java.util.Locale.US, "%02d:00", horaNormalizada)
                turnos.add(stringHora)
                horaActual++
            }
        } catch (e: Exception) {
            // Fallback genérico en caso de error de formato en el modelo de datos
            for (h in 17..23) turnos.add("$h:00")
        }
        turnos
    }

    var turnoSeleccionado by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()
    var scrollHabilitado by remember { mutableStateOf(true) }

    // Congela el scroll principal cuando se arrastra el mapa
    LaunchedEffect(cameraPositionState.isMoving) {
        scrollHabilitado = !cameraPositionState.isMoving
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorFondo)
            .verticalScroll(scrollState, enabled = scrollHabilitado)
    ) {
        // ── TopBar ─────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = ColorTexto)
            }
            Text(cancha.nombre, color = ColorTexto, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // PROPIETARIO & CALIFICACIÓN
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Por: ${cancha.propietario}",
                    color = ColorSub,
                    fontSize = 14.sp
                )
                Text(
                    text = "⭐ ${cancha.calificacion}",
                    color = ColorVerde,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            // FICHA RESUMEN DE DATOS
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ColorCampo, shape = RoundedCornerShape(12.dp))
                    .border(1.dp, ColorBorde, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("💵 Precio por Hora:", color = ColorSub, fontSize = 14.sp)
                    Text("\$${cancha.precio}", color = ColorTexto, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                HorizontalDivider(color = ColorBorde, thickness = 1.dp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("📞 Teléfono:", color = ColorSub, fontSize = 14.sp)
                    Text(cancha.telefono, color = ColorTexto, fontSize = 14.sp)
                }
                HorizontalDivider(color = ColorBorde, thickness = 1.dp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("🕒 Horario General:", color = ColorSub, fontSize = 14.sp)
                    Text("${cancha.horarioApertura} a ${cancha.horarioCierre} hs", color = ColorVerde, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }

            // DESCRIPCIÓN
            if (cancha.descripcion.isNotBlank()) {
                Text("Sobre el complejo", color = ColorVerde, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ColorCampo, shape = RoundedCornerShape(10.dp))
                        .border(1.dp, ColorBorde, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(cancha.descripcion, color = ColorTexto, fontSize = 14.sp)
                }
            }

            // SECCIÓN: MAPA
            Text("Ubicación exacta", color = ColorVerde, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(ColorCampo, shape = RoundedCornerShape(10.dp))
                    .border(1.dp, ColorBorde, RoundedCornerShape(10.dp))
                    .padding(4.dp)
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
                        state = MarkerState(position = coordenadasCancha),
                        title = cancha.nombre
                    )
                }
            }

            // SECCIÓN: HORARIOS Y RESERVAS
            Text("Selecciona un horario para hoy", color = ColorVerde, fontSize = 12.sp, fontWeight = FontWeight.Bold)

            // Grid de turnos en altura controlada para no chocar con el scroll global
            Box(modifier = Modifier.heightIn(max = 280.dp)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(listadoTurnosDía) { hora ->
                        val yaReservado = turnosReservados.contains(hora)
                        val esElSeleccionado = turnoSeleccionado == hora

                        // Estética condicional para los estados del botón
                        val colorFondoCaja = when {
                            yaReservado -> ColorCampo.copy(alpha = 0.4f) // Más oscuro / apagado
                            esElSeleccionado -> ColorVerde
                            else -> ColorCampo
                        }

                        val colorTextoCaja = when {
                            yaReservado -> ColorSub.copy(alpha = 0.4f)
                            esElSeleccionado -> Color.Black
                            else -> ColorTexto
                        }

                        val colorBordeCaja = when {
                            esElSeleccionado -> ColorVerde
                            yaReservado -> ColorBorde.copy(alpha = 0.3f)
                            else -> ColorBorde
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colorFondoCaja, shape = RoundedCornerShape(8.dp))
                                .border(1.dp, colorBordeCaja, RoundedCornerShape(8.dp))
                                .clickable(enabled = !yaReservado) {
                                    turnoSeleccionado = if (esElSeleccionado) null else hora
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = hora,
                                    color = colorTextoCaja,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (yaReservado) "Ocupado" else "Disponible",
                                    color = if (esElSeleccionado) Color.Black.copy(alpha = 0.7f) else ColorSub.copy(alpha = 0.8f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // BOTÓN DE ACCIÓN PRINCIPAL
            Button(
                onClick = {
                    turnoSeleccionado?.let { hora ->
                        onReservarTurno(cancha.id, hora)
                    }
                },
                enabled = turnoSeleccionado != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorVerde,
                    disabledContainerColor = ColorCampo
                )
            ) {
                Text(
                    text = if (turnoSeleccionado != null) "Reservar a las ${turnoSeleccionado} hs" else "Selecciona un turno",
                    color = if (turnoSeleccionado != null) Color.Black else ColorSub,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}