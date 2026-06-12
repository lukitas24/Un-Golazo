package com.example.futbolnomade.presentation.ui.canchas

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.futbolnomade.domain.model.HorarioDisponible
import java.util.Locale

// Google Maps imports
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

private val ColorFondo   = Color(0xFF1A1A1A)
private val ColorCampo   = Color(0xFF2A2A2A)
private val ColorBorde   = Color(0xFF2E2E2E)
private val ColorVerde   = Color(0xFF8BC34A)
private val ColorTexto   = Color(0xFFEEEEEE)
private val ColorSub     = Color(0xFF999999)

@Composable
fun CrearCanchaScreen(
    onCrearCancha: (
        nombre: String,
        ubicacion: String,
        descripcion: String,
        precio: String,
        telefono: String,
        horarioApertura: String,
        horarioCierre: String,
        horariosDetallados: List<HorarioDisponible>,
        latitud: Double,
        longitud: Double
    ) -> Unit,
    onVolver: () -> Unit
) {
    var nombre      by remember { mutableStateOf("") }
    var ubicacion   by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precio      by remember { mutableStateOf("") }
    var telefono    by remember { mutableStateOf("") }
    var error       by remember { mutableStateOf<String?>(null) }

    // ── GESTIÓN DE HORARIOS AVANZADOS POR DÍA ──────────────────
    val diasSemana = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
    var diaSeleccionadoPorPropietario by remember { mutableStateOf("Lunes") }
    var aplicarATodosLosDias by remember { mutableStateOf(false) }

    val horariosMap = remember {
        mutableStateMapOf<String, HorarioDisponible?>().apply {
            diasSemana.forEach { dia ->
                this[dia] = HorarioDisponible(dia, "08:00", "23:00")
            }
        }
    }

    var mostrarRelojApertura by remember { mutableStateOf(false) }
    var mostrarRelojCierre by remember { mutableStateOf(false) }

    // ── CONFIGURACIÓN DEL MAPA ──────────────────
    val ubicacionInicial = LatLng(-42.7692, -65.0385)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(ubicacionInicial, 14f)
    }
    var posicionSeleccionada by remember { mutableStateOf(ubicacionInicial) }

    val scrollState = rememberScrollState()
    var scrollHabilitado by remember { mutableStateOf(true) }

    LaunchedEffect(cameraPositionState.isMoving) {
        scrollHabilitado = !cameraPositionState.isMoving
    }

    LaunchedEffect(aplicarATodosLosDias) {
        if (aplicarATodosLosDias) {
            val horarioBase = horariosMap[diaSeleccionadoPorPropietario]
            if (horarioBase != null) {
                diasSemana.forEach { dia ->
                    horariosMap[dia] = horarioBase.copy(dia = dia)
                }
            }
        }
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
            Text("Subir mi cancha", color = ColorTexto, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CampoLabel("NOMBRE DE LA CANCHA")
            Campo(nombre, { nombre = it }, "Ej: Cancha Maracaná")

            CampoLabel("UBICACIÓN")
            Campo(ubicacion, { ubicacion = it }, "Ej: Puerto Madryn, Mitre 423")

            // CONTENEDOR DEL MAPA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(ColorCampo, shape = RoundedCornerShape(10.dp))
                    .border(1.dp, ColorBorde, RoundedCornerShape(10.dp))
                    .padding(4.dp)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(zoomControlsEnabled = true, scrollGesturesEnabled = true),
                    properties = MapProperties(isMyLocationEnabled = false),
                    onMapClick = { latLng ->
                        posicionSeleccionada = latLng
                        ubicacion = "${String.format(Locale.US, "%.5f", latLng.latitude)}, ${String.format(Locale.US, "%.5f", latLng.longitude)}"
                    }
                ) {
                    Marker(state = MarkerState(position = posicionSeleccionada), title = "Ubicación seleccionada")
                }
            }

            CampoLabel("TELÉFONO DE CONTACTO")
            Campo(telefono, { telefono = it }, "Ej: 2804001234")

            CampoLabel("PRECIO POR HORA (\$)")
            Campo(precio, { precio = it }, "Ej: 5000")

            // ── MÓDULO DE GESTIÓN DE HORARIOS ──────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ColorBorde, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = ColorCampo)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("GESTIÓN DE HORARIOS SEMANALES", color = ColorVerde, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))

                    // Chips de la semana
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        diasSemana.forEach { dia ->
                            val estaConfigurado = horariosMap[dia] != null
                            val esElDiaActivo = diaSeleccionadoPorPropietario == dia

                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        color = when {
                                            esElDiaActivo -> ColorVerde
                                            estaConfigurado -> ColorBorde
                                            else -> Color.Transparent
                                        },
                                        shape = CircleShape
                                    )
                                    .border(1.dp, if (estaConfigurado) Color.Transparent else ColorBorde, CircleShape)
                                    .clickable {
                                        diaSeleccionadoPorPropietario = dia
                                        if (aplicarATodosLosDias) aplicarATodosLosDias = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dia.take(1),
                                    color = when {
                                        esElDiaActivo -> Color.Black
                                        estaConfigurado -> ColorTexto
                                        else -> ColorSub
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Switch Replicador
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("¿Mismo horario toda la semana?", color = ColorTexto, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("Copia la franja actual al resto de los días", color = ColorSub, fontSize = 11.sp)
                        }
                        Switch(
                            checked = aplicarATodosLosDias,
                            onCheckedChange = { aplicarATodosLosDias = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = ColorVerde, checkedTrackColor = ColorVerde.copy(alpha = 0.4f))
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = ColorBorde, modifier = Modifier.padding(vertical = 4.dp))

                    Text("Configurando: $diaSeleccionadoPorPropietario", color = ColorTexto, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))

                    val horarioDelDia = horariosMap[diaSeleccionadoPorPropietario]

                    if (horarioDelDia != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { mostrarRelojApertura = true }
                                    .border(1.dp, ColorBorde, RoundedCornerShape(8.dp)),
                                colors = CardDefaults.cardColors(containerColor = ColorFondo)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Apertura", color = ColorSub, fontSize = 11.sp)
                                    Text(horarioDelDia.horaApertura, color = ColorTexto, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { mostrarRelojCierre = true }
                                    .border(1.dp, ColorBorde, RoundedCornerShape(8.dp)),
                                colors = CardDefaults.cardColors(containerColor = ColorFondo)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Cierre", color = ColorSub, fontSize = 11.sp)
                                    Text(horarioDelDia.horaCierre, color = ColorTexto, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            IconButton(
                                onClick = {
                                    horariosMap[diaSeleccionadoPorPropietario] = null
                                    if (aplicarATodosLosDias) aplicarATodosLosDias = false
                                },
                                modifier = Modifier.background(Color.Red.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Borrar día", tint = Color.Red)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Red.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                .border(1.dp, Color.Red.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Cerrado / No disponible este día", color = Color.Red.copy(alpha = 0.8f), fontSize = 13.sp)
                            Text(
                                "Reactivar",
                                color = ColorVerde,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    horariosMap[diaSeleccionadoPorPropietario] = HorarioDisponible(diaSeleccionadoPorPropietario, "08:00", "23:00")
                                }
                            )
                        }
                    }
                }
            }

            // DIÁLOGOS DE SELECCIÓN DE HORA (Versión limpia sin experimental warnings)
            if (mostrarRelojApertura) {
                RelojEstableDialog(
                    horaInicial = horariosMap[diaSeleccionadoPorPropietario]?.horaApertura ?: "08:00",
                    titulo = "Hora de Apertura",
                    onDismiss = { mostrarRelojApertura = false },
                    onHoraSeleccionada = { nuevaHora ->
                        val actual = horariosMap[diaSeleccionadoPorPropietario]
                        if (actual != null) {
                            horariosMap[diaSeleccionadoPorPropietario] = actual.copy(horaApertura = nuevaHora)
                            if (aplicarATodosLosDias) {
                                diasSemana.forEach { d -> horariosMap[d] = horariosMap[d]?.copy(horaApertura = nuevaHora) }
                            }
                        }
                        mostrarRelojApertura = false
                    }
                )
            }

            if (mostrarRelojCierre) {
                RelojEstableDialog(
                    horaInicial = horariosMap[diaSeleccionadoPorPropietario]?.horaCierre ?: "23:00",
                    titulo = "Hora de Cierre",
                    onDismiss = { mostrarRelojCierre = false },
                    onHoraSeleccionada = { nuevaHora ->
                        val actual = horariosMap[diaSeleccionadoPorPropietario]
                        if (actual != null) {
                            horariosMap[diaSeleccionadoPorPropietario] = actual.copy(horaCierre = nuevaHora)
                            if (aplicarATodosLosDias) {
                                diasSemana.forEach { d -> horariosMap[d] = horariosMap[d]?.copy(horaCierre = nuevaHora) }
                            }
                        }
                        mostrarRelojCierre = false
                    }
                )
            }

            CampoLabel("DESCRIPCIÓN (OPCIONAL)")
            Campo(descripcion, { descripcion = it }, "Césped sintético, techada, vestuarios...", minLines = 3)

            error?.let {
                Text(it, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    val listadoFinalHorarios = horariosMap.values.filterNotNull()
                    val primerHorarioValido = listadoFinalHorarios.firstOrNull()
                    val aperturaFiltro = primerHorarioValido?.horaApertura ?: "00:00"
                    val cierreFiltro = primerHorarioValido?.horaCierre ?: "00:00"

                    when {
                        nombre.isBlank()    -> error = "El nombre es obligatorio"
                        ubicacion.isBlank() -> error = "La ubicación es obligatoria"
                        telefono.isBlank()  -> error = "El teléfono es obligatorio"
                        precio.toDoubleOrNull().let { it == null || it <= 0 } -> error = "El precio debe ser mayor a 0"
                        listadoFinalHorarios.isEmpty() -> error = "Debes asignar al menos un día disponible con horarios"
                        else -> {
                            error = null
                            onCrearCancha(
                                nombre.trim(),
                                ubicacion.trim(),
                                descripcion.trim(),
                                precio.trim(),
                                telefono.trim(),
                                aperturaFiltro,
                                cierreFiltro,
                                listadoFinalHorarios,
                                posicionSeleccionada.latitude,
                                posicionSeleccionada.longitude
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape    = RoundedCornerShape(10.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = ColorVerde)
            ) {
                Text("Publicar cancha", color = Color.Black, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick  = onVolver,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape    = RoundedCornerShape(10.dp),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = ColorTexto)
            ) { Text("Cancelar") }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── DIÁLOGO ESTABLE DE ENTRADA DE TEXTO PARA HORAS (COMPATIBLE CON CUALQUIER VERSIÓN) ──
@Composable
private fun RelojEstableDialog(
    horaInicial: String,
    titulo: String,
    onDismiss: () -> Unit,
    onHoraSeleccionada: (String) -> Unit
) {
    val partes = horaInicial.split(":")
    var horas by remember { mutableStateOf(partes.getOrNull(0) ?: "08") }
    var minutos by remember { mutableStateOf(partes.getOrNull(1) ?: "00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ColorCampo,
        shape = RoundedCornerShape(16.dp),
        title = { Text(titulo, color = ColorVerde, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Input de Horas
                OutlinedTextField(
                    value = horas,
                    onValueChange = { if (it.length <= 2) horas = it.filter { c -> c.isDigit() } },
                    modifier = Modifier.width(64.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ColorFondo,
                        unfocusedContainerColor = ColorFondo,
                        focusedTextColor = ColorTexto,
                        unfocusedTextColor = ColorTexto,
                        focusedBorderColor = ColorVerde,
                        unfocusedBorderColor = ColorBorde
                    )
                )
                Text(" : ", color = ColorTexto, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                // Input de Minutos
                OutlinedTextField(
                    value = minutos,
                    onValueChange = { if (it.length <= 2) minutos = it.filter { c -> c.isDigit() } },
                    modifier = Modifier.width(64.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ColorFondo,
                        unfocusedContainerColor = ColorFondo,
                        focusedTextColor = ColorTexto,
                        unfocusedTextColor = ColorTexto,
                        focusedBorderColor = ColorVerde,
                        unfocusedBorderColor = ColorBorde
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val hInt = horas.toIntOrNull() ?: 0
                val mInt = minutos.toIntOrNull() ?: 0
                // Validamos rangos correctos de reloj de 24 horas
                val hValida = hInt.coerceIn(0, 23)
                val mValida = mInt.coerceIn(0, 59)
                onHoraSeleccionada(String.format(Locale.US, "%02d:%02d", hValida, mValida))
            }) {
                Text("ACEPTAR", color = ColorVerde, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", color = ColorTexto)
            }
        }
    )
}

@Composable
private fun CampoLabel(texto: String) {
    Text(texto, color = ColorVerde, fontSize = 11.sp, fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 2.dp, top = 4.dp))
}

@Composable
private fun Campo(valor: String, onChange: (String) -> Unit, placeholder: String, minLines: Int = 1) {
    OutlinedTextField(
        value         = valor,
        onValueChange = onChange,
        placeholder   = { Text(placeholder, color = ColorSub, fontSize = 13.sp) },
        minLines      = minLines,
        modifier      = Modifier.fillMaxWidth(),
        shape         = RoundedCornerShape(10.dp),
        colors        = OutlinedTextFieldDefaults.colors(
            focusedContainerColor   = ColorCampo,
            unfocusedContainerColor = ColorCampo,
            focusedBorderColor      = ColorVerde,
            unfocusedBorderColor    = ColorBorde,
            focusedTextColor        = ColorTexto,
            unfocusedTextColor      = ColorTexto,
            cursorColor             = ColorVerde
        )
    )
}