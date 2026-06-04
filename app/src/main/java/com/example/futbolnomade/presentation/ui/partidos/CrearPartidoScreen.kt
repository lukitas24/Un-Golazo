package com.example.futbolnomade.presentation.ui.partidos

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings

// COLORES ESTILO FUTBOL NOMADE
private val FondoOscuro = Color(0xFF202020)
private val VerdeMetálico = Color(0xFF82A820)
private val GrisCampos = Color(0xFF2E2E2E)

@OptIn(ExperimentalMaterial3Api::class)
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
    var dificultad by remember { mutableStateOf("Fácil") }
    var participantes by remember { mutableStateOf("10") }
    var descripcion by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    var mostrarDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    var dropdownExpandido by remember { mutableStateOf(false) }
    val opcionesDificultad = listOf("Fácil", "Medio", "Avanzado")

    // --- CONFIGURACIÓN DE PERMISOS DE UBICACIÓN ---
    val context = LocalContext.current
    var tienePermisoUbicacion by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcherPermisos = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { grantResult ->
        tienePermisoUbicacion = grantResult
    }

    LaunchedEffect(Unit) {
        if (!tienePermisoUbicacion) {
            launcherPermisos.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // --- ESTADO PARA EL MAPA ---
    val cameraPositionState = rememberCameraPositionState {
        // Coordenadas iniciales por defecto
        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
            com.google.android.gms.maps.model.LatLng(-34.6037, -58.3816), 15f
        )
    }

    var posicionSeleccionada by remember {
        mutableStateOf(com.google.android.gms.maps.model.LatLng(-34.6037, -58.3816))
    }

    val mapProperties by remember(tienePermisoUbicacion) {
        mutableStateOf(MapProperties(isMyLocationEnabled = tienePermisoUbicacion))
    }

    val mapUiSettings by remember {
        mutableStateOf(MapUiSettings(myLocationButtonEnabled = true))
    }

    // --- CONTROL INTELIGENTE DEL SCROLL ---
    val scrollState = rememberScrollState()
    var scrollHabilitado by remember { mutableStateOf(true) }

    LaunchedEffect(cameraPositionState.isMoving) {
        scrollHabilitado = !cameraPositionState.isMoving
    }

    // Configuración de estilos para reutilizar en los campos oscuros
    val coloresCampos = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = GrisCampos,
        unfocusedContainerColor = GrisCampos,
        disabledContainerColor = GrisCampos,
        focusedBorderColor = VerdeMetálico,
        unfocusedBorderColor = Color.Transparent,
        focusedLabelColor = VerdeMetálico,
        unfocusedLabelColor = Color.Gray,
        disabledLabelColor = Color.Gray,
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        disabledTextColor = Color.White,
        disabledBorderColor = Color.Transparent
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoOscuro)
            .verticalScroll(scrollState, enabled = scrollHabilitado)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(
            text = "Crear partido",
            color = VerdeMetálico,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // TÍTULO
        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            label = { Text("Título del partido") },
            colors = coloresCampos,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // FILA: HORARIO Y FECHA
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = horario,
                onValueChange = { horario = it },
                label = { Text("Horario") },
                placeholder = { Text("HH:MM") },
                colors = coloresCampos,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = fecha,
                onValueChange = {},
                label = { Text("Fecha") },
                readOnly = true,
                colors = coloresCampos,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f),
                trailingIcon = {
                    TextButton(onClick = { mostrarDatePicker = true }) {
                        Text("Elegir", color = VerdeMetálico, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // UBICACIÓN TEXTO
        OutlinedTextField(
            value = ubicacion,
            onValueChange = { ubicacion = it },
            label = { Text("Ubicación (o marca en el mapa)") },
            colors = coloresCampos,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Toca el mapa para fijar el punto de encuentro:",
            style = MaterialTheme.typography.bodySmall,
            color = Color.LightGray
        )

        // MAPA CONTAINER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(285.dp)
                .padding(vertical = 8.dp)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = mapUiSettings,
                onMapClick = { latLng ->
                    posicionSeleccionada = latLng
                    ubicacion = "${String.format(Locale.US, "%.5f", latLng.latitude)}, ${String.format(Locale.US, "%.5f", latLng.longitude)}"
                }
            ) {
                Marker(
                    state = MarkerState(position = posicionSeleccionada),
                    title = "Punto de encuentro"
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // FILA: DIFICULTAD (SELECT) Y PARTICIPANTES
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // SELECTOR DE DIFICULTAD (Dropdown)
            ExposedDropdownMenuBox(
                expanded = dropdownExpandido,
                onExpandedChange = { dropdownExpandido = !dropdownExpandido },
                modifier = Modifier.weight(1.2f)
            ) {
                OutlinedTextField(
                    value = dificultad,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Dificultad") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpandido) },
                    colors = coloresCampos,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                )

                ExposedDropdownMenu(
                    expanded = dropdownExpandido,
                    onDismissRequest = { dropdownExpandido = false },
                    modifier = Modifier.background(GrisCampos)
                ) {
                    opcionesDificultad.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(opcion, color = Color.White) },
                            onClick = {
                                dificultad = opcion
                                dropdownExpandido = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            // PARTICIPANTES
            OutlinedTextField(
                value = participantes,
                onValueChange = { participantes = it },
                label = { Text("Cupos") },
                colors = coloresCampos,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(0.8f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // DESCRIPCIÓN
        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción (opcional)") },
            placeholder = { Text("Ej: Traer camiseta blanca y negra...") },
            colors = coloresCampos,
            shape = RoundedCornerShape(6.dp),
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(28.dp))

        if (error != null) {
            Text(
                text = error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // BOTÓN PUBLICAR PARTIDO
        Button(
            onClick = {
                val participantesInt = participantes.toIntOrNull()
                when {
                    titulo.isBlank() -> error = "El título es obligatorio"
                    horario.isBlank() -> error = "El horario es obligatorio"
                    fecha.isBlank() -> error = "La fecha es obligatoria"
                    ubicacion.isBlank() -> error = "La ubicación es obligatoria"
                    participantesInt == null || participantesInt <= 0 -> {
                        error = "La cantidad de participantes debe ser mayor a 0"
                    }
                    else -> {
                        error = null
                        onCrearPartido(
                            titulo.trim(),
                            horario.trim(),
                            fecha.trim(),
                            ubicacion.trim(),
                            dificultad.trim(),
                            participantesInt,
                            descripcion.trim()
                        )
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = VerdeMetálico),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Publicar partido", color = Color.Black, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // BOTÓN CANCELAR
        OutlinedButton(
            onClick = onVolver,
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Cancelar", color = Color.White, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
        }
    }

    if (mostrarDatePicker) {
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            fecha = formatter.format(Date(millis))
                        }
                        mostrarDatePicker = false
                    }
                ) { Text("Aceptar", color = VerdeMetálico) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) { Text("Cancelar", color = Color.Gray) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}