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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val ColorFondo = Color(0xFF1A1A1A)
private val ColorCampo = Color(0xFF2A2A2A)
private val ColorBorde = Color(0xFF2E2E2E)
private val ColorVerde = Color(0xFF8BC34A)
private val ColorTexto = Color(0xFFEEEEEE)
private val ColorSub = Color(0xFF999999)

@Composable
fun DetalleCanchaScreen(
    cancha: Cancha?,
    turnosReservados: List<String> = emptyList(),
    onReservarTurno: (canchaId: String, hora: String) -> Unit,
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
            Text("No se encontró la cancha", color = ColorTexto)
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

    val coordenadasCancha = remember(cancha.ubicacion) {
        try {
            val partes = cancha.ubicacion.split(",")
            if (partes.size == 2) {
                LatLng(partes[0].trim().toDouble(), partes[1].trim().toDouble())
            } else {
                LatLng(-42.7692, -65.0385)
            }
        } catch (e: Exception) {
            LatLng(-42.7692, -65.0385)
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(coordenadasCancha, 15f)
    }

    var diasAdelante by remember { mutableStateOf(0) }
    var turnoSeleccionado by remember { mutableStateOf<String?>(null) }

    val fechaSeleccionada = remember(diasAdelante) {
        Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, diasAdelante)
        }
    }

    val ahora = Calendar.getInstance()
    val esHoy = diasAdelante == 0

    val diaSeleccionado = remember(diasAdelante) {
        SimpleDateFormat("EEEE", Locale("es", "ES")).format(fechaSeleccionada.time)
            .replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
    }

    val fechaTexto = remember(diasAdelante) {
        SimpleDateFormat("dd/MM", Locale("es", "ES")).format(fechaSeleccionada.time)
    }

    val tituloDia = "$diaSeleccionado $fechaTexto"

    val listadoTurnosDia = remember(
        cancha.horarios,
        cancha.horarioApertura,
        cancha.horarioCierre,
        diasAdelante
    ) {
        val turnos = mutableListOf<String>()

        try {
            val horarioEspecifico = cancha.horarios.find {
                it.dia.lowercase() == diaSeleccionado.lowercase()
            }

            if (horarioEspecifico == null) {
                return@remember emptyList<String>()
            }

            val aperturaString = horarioEspecifico.horaApertura
            val cierreString = horarioEspecifico.horaCierre

            val aperturaItem = aperturaString.split(":")
            val cierreItem = cierreString.split(":")

            var horaActual = aperturaItem[0].toInt()
            val horaFin = cierreItem[0].toInt()

            val horaLimite = if (horaFin <= horaActual) horaFin + 24 else horaFin

            while (horaActual < horaLimite) {
                val horaNormalizada = horaActual % 24
                val stringHora = String.format(Locale.US, "%02d:00", horaNormalizada)
                turnos.add(stringHora)
                horaActual++
            }
        } catch (e: Exception) {
            emptyList<String>()
        }

        turnos
    }

    val scrollState = rememberScrollState()
    var scrollHabilitado by remember { mutableStateOf(true) }

    LaunchedEffect(cameraPositionState.isMoving) {
        scrollHabilitado = !cameraPositionState.isMoving
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorFondo)
            .verticalScroll(scrollState, enabled = scrollHabilitado)
    ) {
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

            Text(
                cancha.nombre,
                color = ColorTexto,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ColorCampo, shape = RoundedCornerShape(12.dp))
                    .border(1.dp, ColorBorde, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("💵 Precio por Hora:", color = ColorSub, fontSize = 14.sp)
                    Text(
                        "\$${cancha.precio}",
                        color = ColorTexto,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                HorizontalDivider(color = ColorBorde, thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("📞 Teléfono:", color = ColorSub, fontSize = 14.sp)
                    Text(cancha.telefono, color = ColorTexto, fontSize = 14.sp)
                }

                HorizontalDivider(color = ColorBorde, thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("🕒 Horario General:", color = ColorSub, fontSize = 14.sp)
                    Text(
                        "${cancha.horarioApertura} a ${cancha.horarioCierre} hs",
                        color = ColorVerde,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }

            if (cancha.descripcion.isNotBlank()) {
                Text(
                    "Sobre el complejo",
                    color = ColorVerde,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

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

            Text(
                "Ubicación exacta",
                color = ColorVerde,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(ColorCampo, shape = RoundedCornerShape(10.dp))
                    .border(1.dp, ColorBorde, RoundedCornerShape(10.dp))
                    .padding(4.dp)
            ) {
                val markerState = remember(cancha.id) { MarkerState(position = coordenadasCancha) }
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
                        state = markerState,
                        title = cancha.nombre
                    )
                }
            }

            Text(
                "Selecciona un horario",
                color = ColorVerde,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ColorCampo, RoundedCornerShape(10.dp))
                    .border(1.dp, ColorBorde, RoundedCornerShape(10.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = {
                        if (diasAdelante > 0) {
                            diasAdelante--
                            turnoSeleccionado = null
                        }
                    },
                    enabled = diasAdelante > 0,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorTexto)
                ) {
                    Text("←")
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (esHoy) "Hoy $fechaTexto" else tituloDia,
                        color = ColorTexto,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    if (!esHoy) {
                        Text(
                            text = "Volver al día actual",
                            color = ColorVerde,
                            fontSize = 11.sp,
                            modifier = Modifier.clickable {
                                diasAdelante = 0
                                turnoSeleccionado = null
                            }
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        diasAdelante++
                        turnoSeleccionado = null
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorVerde)
                ) {
                    Text("→")
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (listadoTurnosDia.isEmpty()) {
                    Text(
                        text = "El complejo está cerrado este día.",
                        color = ColorSub,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    val filas = listadoTurnosDia.chunked(3)
                    filas.forEach { fila ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            fila.forEach { hora ->
                                val partesHora = hora.split(":")
                                val horaTurno = partesHora.getOrNull(0)?.toIntOrNull() ?: 0
                                val minutoTurno = partesHora.getOrNull(1)?.toIntOrNull() ?: 0

                                val turnoYaPaso = esHoy && (
                                        horaTurno < ahora.get(Calendar.HOUR_OF_DAY) ||
                                                (
                                                        horaTurno == ahora.get(Calendar.HOUR_OF_DAY) &&
                                                                minutoTurno <= ahora.get(Calendar.MINUTE)
                                                        )
                                        )

                                val yaReservado = turnosReservados.contains(hora)
                                val deshabilitado = yaReservado || turnoYaPaso
                                val esElSeleccionado = turnoSeleccionado == hora

                                val colorFondoCaja = when {
                                    deshabilitado -> ColorCampo.copy(alpha = 0.4f)
                                    esElSeleccionado -> ColorVerde
                                    else -> ColorCampo
                                }

                                val colorTextoCaja = when {
                                    deshabilitado -> ColorSub.copy(alpha = 0.4f)
                                    esElSeleccionado -> Color.Black
                                    else -> ColorTexto
                                }

                                val colorBordeCaja = when {
                                    esElSeleccionado -> ColorVerde
                                    deshabilitado -> ColorBorde.copy(alpha = 0.3f)
                                    else -> ColorBorde
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(colorFondoCaja, shape = RoundedCornerShape(8.dp))
                                        .border(1.dp, colorBordeCaja, RoundedCornerShape(8.dp))
                                        .clickable(enabled = !deshabilitado) {
                                            turnoSeleccionado =
                                                if (esElSeleccionado) null else hora
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
                                            text = when {
                                                yaReservado -> "Ocupado"
                                                turnoYaPaso -> "Pasó"
                                                else -> "Disponible"
                                            },
                                            color = if (esElSeleccionado) {
                                                Color.Black.copy(alpha = 0.7f)
                                            } else {
                                                ColorSub.copy(alpha = 0.8f)
                                            },
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                            repeat(3 - fila.size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

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
                    text = if (turnoSeleccionado != null) {
                        "Reservar $tituloDia a las $turnoSeleccionado hs"
                    } else {
                        "Selecciona un turno"
                    },
                    color = if (turnoSeleccionado != null) Color.Black else ColorSub,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}