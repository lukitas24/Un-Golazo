package com.example.futbolnomade.presentation.ui.canchas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

// Importaciones para el correcto funcionamiento de Google Maps
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
        horarioCierre: String
    ) -> Unit,
    onVolver: () -> Unit
) {
    var nombre          by remember { mutableStateOf("") }
    var ubicacion       by remember { mutableStateOf("") }
    var descripcion     by remember { mutableStateOf("") }
    var precio          by remember { mutableStateOf("") }
    var telefono        by remember { mutableStateOf("") }
    var horarioApertura by remember { mutableStateOf("") }
    var horarioCierre   by remember { mutableStateOf("") }
    var error           by remember { mutableStateOf<String?>(null) }

    // ── CONFIGURACIÓN DEL MAPA E INTERCEPCIÓN DE SCROLL ──────────────────
    // Coordenadas por defecto (Puerto Madryn, según tu placeholder)
    val ubicacionInicial = LatLng(-42.7692, -65.0385)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(ubicacionInicial, 14f)
    }

    var posicionSeleccionada by remember { mutableStateOf(ubicacionInicial) }

    val scrollState = rememberScrollState()
    var scrollHabilitado by remember { mutableStateOf(true) }

    // Apaga el scroll general del formulario de canchas cuando el usuario arrastra el mapa
    LaunchedEffect(cameraPositionState.isMoving) {
        scrollHabilitado = !cameraPositionState.isMoving
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorFondo)
            // Vinculamos la variable condicional para que el scroll fluya perfecto
            .verticalScroll(scrollState, enabled = scrollHabilitado)
    ) {
        // ── TopBar ─────────────────────────────────────────────────────────
        Row(
            modifier          = Modifier.fillMaxWidth().statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = ColorTexto)
            }
            Text("Subir mi cancha", color = ColorTexto, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier            = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CampoLabel("NOMBRE DE LA CANCHA")
            Campo(nombre, { nombre = it }, "Ej: Cancha Maracana")

            CampoLabel("UBICACIÓN")
            Campo(ubicacion, { ubicacion = it }, "Ej: Puerto Madryn, Mitre 423")

            // CONTENEDOR DEL MAPA (Integrado armónicamente con tu diseño oscuro)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
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
                    properties = MapProperties(isMyLocationEnabled = false),
                    onMapClick = { latLng ->
                        posicionSeleccionada = latLng
                        // Setea automáticamente las coordenadas simplificadas en el input
                        ubicacion = "${String.format(Locale.US, "%.5f", latLng.latitude)}, ${String.format(Locale.US, "%.5f", latLng.longitude)}"
                    }
                ) {
                    Marker(
                        state = MarkerState(position = posicionSeleccionada),
                        title = "Ubicación de la cancha"
                    )
                }
            }

            CampoLabel("TELÉFONO DE CONTACTO")
            Campo(telefono, { telefono = it }, "Ej: 2804001234")

            CampoLabel("PRECIO POR HORA (\$)")
            Campo(precio, { precio = it }, "Ej: 5000")

            CampoLabel("HORARIO GENERAL")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("Apertura", color = ColorSub, fontSize = 11.sp)
                    Spacer(Modifier.height(4.dp))
                    Campo(horarioApertura, { horarioApertura = it }, "HH:MM")
                }
                Column(Modifier.weight(1f)) {
                    Text("Cierre", color = ColorSub, fontSize = 11.sp)
                    Spacer(Modifier.height(4.dp))
                    Campo(horarioCierre, { horarioCierre = it }, "HH:MM")
                }
            }

            CampoLabel("DESCRIPCIÓN (opcional)")
            Campo(descripcion, { descripcion = it }, "Césped sintético, techada, vestuarios...", minLines = 3)

            error?.let {
                Text(it, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    when {
                        nombre.isBlank()    -> error = "El nombre es obligatorio"
                        ubicacion.isBlank() -> error = "La ubicación es obligatoria"
                        telefono.isBlank()  -> error = "El teléfono es obligatorio"
                        precio.toDoubleOrNull().let { it == null || it <= 0 } ->
                            error = "El precio debe ser mayor a 0"
                        horarioApertura.isBlank() -> error = "El horario de apertura es obligatorio"
                        horarioCierre.isBlank()   -> error = "El horario de cierre es obligatorio"
                        else -> {
                            error = null
                            onCrearCancha(
                                nombre.trim(), ubicacion.trim(), descripcion.trim(),
                                precio.trim(), telefono.trim(),
                                horarioApertura.trim(), horarioCierre.trim()
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

@Composable
private fun CampoLabel(texto: String) {
    Text(texto, color = ColorVerde, fontSize = 11.sp, fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 2.dp))
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