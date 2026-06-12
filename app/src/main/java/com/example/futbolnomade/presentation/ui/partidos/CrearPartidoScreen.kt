package com.example.futbolnomade.presentation.ui.partidos

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.futbolnomade.domain.model.Cancha
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private val FondoOscuro = Color(0xFF202020)
private val VerdeMetalico = Color(0xFF82A820)
private val GrisCampos = Color(0xFF2E2E2E)
private val GrisTarjeta = Color(0xFF282828)
private val GrisBorde = Color(0xFF3A3A3A)
private val TextoClaro = Color(0xFFEEEEEE)
private val TextoSecundario = Color(0xFFAAAAAA)
private val ColorRojo = Color(0xFFE53935)

private enum class ModoUbicacion {
    CANCHA_APP,
    UBICACION_LIBRE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearPartidoScreen(
    canchas: List<Cancha>,
    onCrearPartido: (
        titulo: String,
        horario: String,
        fecha: String,
        ubicacion: String,
        dificultad: String,
        participantes: Int,
        descripcion: String,
        canchaId: Int?,
        nombreCancha: String?,
        latitud: Double?,
        longitud: Double?
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

    var modoUbicacion by remember { mutableStateOf(ModoUbicacion.CANCHA_APP) }
    var canchaSeleccionada by remember { mutableStateOf<Cancha?>(null) }
    var turnoSeleccionado by remember { mutableStateOf<String?>(null) }
    var canchaPreviewLibre by remember { mutableStateOf<Cancha?>(null) }

    val radioInvisibleKm = 20.0

    var mostrarDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    var dropdownExpandido by remember { mutableStateOf(false) }
    val opcionesDificultad = listOf("Fácil", "Medio", "Avanzado")

    var diasAdelante by remember { mutableStateOf(0) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var tienePermisoUbicacion by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
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

    val posicionInicial = LatLng(-42.7692, -65.0385)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(posicionInicial, 13f)
    }

    var posicionSeleccionada by remember {
        mutableStateOf(posicionInicial)
    }

    val canchasCercanas = remember(canchas, posicionSeleccionada) {
        canchas.filter { cancha ->
            val posicionCancha = LatLng(cancha.latitud, cancha.longitud)

            calcularDistanciaKm(
                posicionSeleccionada,
                posicionCancha
            ) <= radioInvisibleKm
        }
    }

    val mapProperties by remember(tienePermisoUbicacion) {
        mutableStateOf(
            MapProperties(
                isMyLocationEnabled = tienePermisoUbicacion
            )
        )
    }

    val mapUiSettingsLibre = remember {
        MapUiSettings(
            myLocationButtonEnabled = true,
            zoomControlsEnabled = false,
            scrollGesturesEnabled = true,
            zoomGesturesEnabled = true
        )
    }

    val mapUiSettingsFijo = remember {
        MapUiSettings(
            myLocationButtonEnabled = false,
            zoomControlsEnabled = false,
            scrollGesturesEnabled = false,
            zoomGesturesEnabled = false,
            rotationGesturesEnabled = false,
            tiltGesturesEnabled = false
        )
    }

    val scrollState = rememberScrollState()
    var scrollHabilitado by remember { mutableStateOf(true) }

    LaunchedEffect(cameraPositionState.isMoving) {
        scrollHabilitado = !cameraPositionState.isMoving
    }

    val fechaBase = remember(fecha, diasAdelante) {
        obtenerFechaParaHorarios(fecha, diasAdelante)
    }

    val ahora = Calendar.getInstance()

    val esHoy = remember(fechaBase) {
        mismoDia(fechaBase, ahora)
    }

    val diaSeleccionado = remember(fechaBase) {
        SimpleDateFormat("EEEE", Locale("es", "ES"))
            .format(fechaBase.time)
            .replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
    }

    val fechaTextoCorta = remember(fechaBase) {
        SimpleDateFormat("dd/MM", Locale("es", "ES")).format(fechaBase.time)
    }

    val fechaTextoCompleta = remember(fechaBase) {
        SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES")).format(fechaBase.time)
    }

    val tituloDia = if (esHoy) {
        "Hoy $fechaTextoCorta"
    } else {
        "$diaSeleccionado $fechaTextoCorta"
    }

    val turnosDelDia = remember(canchaSeleccionada, diaSeleccionado) {
        generarTurnosDelDia(
            cancha = canchaSeleccionada,
            diaSeleccionado = diaSeleccionado
        )
    }

    val coloresCampos = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = GrisCampos,
        unfocusedContainerColor = GrisCampos,
        disabledContainerColor = GrisCampos,
        focusedBorderColor = VerdeMetalico,
        unfocusedBorderColor = Color.Transparent,
        focusedLabelColor = VerdeMetalico,
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
            color = VerdeMetalico,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Elegí una cancha cargada o marcá una ubicación libre.",
            color = TextoSecundario,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            label = { Text("Título del partido") },
            colors = coloresCampos,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = horario,
                onValueChange = { horario = it },
                label = {
                    Text(
                        if (modoUbicacion == ModoUbicacion.CANCHA_APP) {
                            "Turno elegido"
                        } else {
                            "Horario"
                        }
                    )
                },
                placeholder = { Text("HH:MM") },
                readOnly = modoUbicacion == ModoUbicacion.CANCHA_APP,
                colors = coloresCampos,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = fecha,
                onValueChange = {},
                label = { Text("Fecha") },
                readOnly = true,
                colors = coloresCampos,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            mostrarDatePicker = true
                        }
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Elegir fecha",
                            tint = VerdeMetalico
                        )
                    }
                }
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Ubicación",
            color = TextoClaro,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(GrisTarjeta)
                .border(1.dp, GrisBorde, RoundedCornerShape(14.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            BotonModoUbicacion(
                text = "Cancha de la app",
                selected = modoUbicacion == ModoUbicacion.CANCHA_APP,
                modifier = Modifier.weight(1f),
                onClick = {
                    modoUbicacion = ModoUbicacion.CANCHA_APP
                    turnoSeleccionado = null
                    horario = ""
                    ubicacion = canchaSeleccionada?.ubicacion ?: ""

                    canchaSeleccionada?.let { cancha ->
                        posicionSeleccionada = LatLng(cancha.latitud, cancha.longitud)
                    }
                }
            )

            BotonModoUbicacion(
                text = "Ubicación libre",
                selected = modoUbicacion == ModoUbicacion.UBICACION_LIBRE,
                modifier = Modifier.weight(1f),
                onClick = {
                    modoUbicacion = ModoUbicacion.UBICACION_LIBRE
                    canchaSeleccionada = null
                    turnoSeleccionado = null
                    horario = ""
                    ubicacion = ""
                }
            )
        }

        Spacer(Modifier.height(14.dp))

        if (modoUbicacion == ModoUbicacion.CANCHA_APP) {
            AvisoReservaPendiente()

            Spacer(Modifier.height(12.dp))

            if (canchas.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(GrisTarjeta)
                        .border(1.dp, GrisBorde, RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Todavía no hay canchas cargadas. Podés usar una ubicación libre.",
                        color = TextoSecundario,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    canchas.forEach { cancha ->
                        CardCanchaSeleccionable(
                            cancha = cancha,
                            seleccionada = canchaSeleccionada?.id == cancha.id,
                            onClick = {
                                canchaSeleccionada = cancha
                                turnoSeleccionado = null
                                horario = ""
                                ubicacion = cancha.ubicacion

                                posicionSeleccionada = LatLng(cancha.latitud, cancha.longitud)

                                scope.launch {
                                    cameraPositionState.animate(
                                        update = CameraUpdateFactory.newLatLngZoom(
                                            posicionSeleccionada,
                                            15f
                                        ),
                                        durationMs = 600
                                    )
                                }
                            }
                        )
                    }
                }
            }

            canchaSeleccionada?.let { cancha ->
                Spacer(Modifier.height(14.dp))

                Text(
                    text = "Ubicación de ${cancha.nombre}",
                    color = VerdeMetalico,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )

                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, GrisBorde, RoundedCornerShape(16.dp))
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(isMyLocationEnabled = false),
                        uiSettings = mapUiSettingsFijo
                    ) {
                        Marker(
                            state = MarkerState(
                                position = LatLng(cancha.latitud, cancha.longitud)
                            ),
                            title = cancha.nombre,
                            snippet = cancha.ubicacion
                        )
                    }

                    Surface(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = VerdeMetalico,
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(Modifier.width(6.dp))

                            Text(
                                text = "Ubicación fija de la cancha",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                SelectorTurnoCancha(
                    cancha = cancha,
                    tituloDia = tituloDia,
                    diaSeleccionado = diaSeleccionado,
                    fechaTextoCompleta = fechaTextoCompleta,
                    turnosDelDia = turnosDelDia,
                    turnoSeleccionado = turnoSeleccionado,
                    esHoy = esHoy,
                    ahora = ahora,
                    onAnteriorDia = {
                        diasAdelante--
                        turnoSeleccionado = null
                        horario = ""
                    },
                    onSiguienteDia = {
                        diasAdelante++
                        turnoSeleccionado = null
                        horario = ""
                    },
                    onVolverAHoy = {
                        diasAdelante = 0
                        turnoSeleccionado = null
                        horario = ""
                    },
                    onSeleccionarTurno = { turno ->
                        turnoSeleccionado = turno
                        horario = turno
                        fecha = fechaTextoCompleta
                    }
                )
            }
        } else {
            OutlinedTextField(
                value = ubicacion,
                onValueChange = { ubicacion = it },
                label = { Text("Ubicación o referencia") },
                placeholder = { Text("Ej: Plaza San Martín, Puerto Madryn") },
                colors = coloresCampos,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "También podés tocar el mapa. Las canchas cargadas aparecen como referencia.",
                color = TextoSecundario,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, GrisBorde, RoundedCornerShape(18.dp))
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = mapProperties,
                    uiSettings = mapUiSettingsLibre,
                    onMapClick = { latLng ->
                        canchaPreviewLibre = null
                        posicionSeleccionada = latLng
                        ubicacion = "${String.format(Locale.US, "%.5f", latLng.latitude)}, ${
                            String.format(Locale.US, "%.5f", latLng.longitude)
                        }"
                    }
                ) {
                    canchasCercanas.forEach { cancha ->
                        val posicionCancha = LatLng(cancha.latitud, cancha.longitud)

                        Marker(
                            state = MarkerState(position = posicionCancha),
                            title = cancha.nombre,
                            snippet = "Cancha cargada • \$${cancha.precio}/hr",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                            onClick = {
                                canchaPreviewLibre = cancha
                                false
                            }
                        )
                    }

                    Marker(
                        state = MarkerState(position = posicionSeleccionada),
                        title = "Punto de encuentro",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                }

                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = VerdeMetalico,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(Modifier.width(6.dp))

                        Text(
                            text = "Tocá el mapa o elegí una cancha marcada",
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                canchaPreviewLibre?.let { cancha ->
                    CardCanchaMapaLibre(
                        cancha = cancha,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(12.dp),
                        onSeleccionar = {
                            modoUbicacion = ModoUbicacion.CANCHA_APP
                            canchaSeleccionada = cancha
                            turnoSeleccionado = null
                            horario = ""
                            ubicacion = cancha.ubicacion

                            val posicionCancha = LatLng(cancha.latitud, cancha.longitud)
                            posicionSeleccionada = posicionCancha
                            canchaPreviewLibre = null

                            scope.launch {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLngZoom(
                                        posicionCancha,
                                        15f
                                    ),
                                    durationMs = 600
                                )
                            }
                        }
                    )
                }
            }
        }

        if (ubicacion.isNotBlank()) {
            Spacer(Modifier.height(10.dp))

            Text(
                text = "Ubicación seleccionada: $ubicacion",
                color = TextoSecundario,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            ExposedDropdownMenuBox(
                expanded = dropdownExpandido,
                onExpandedChange = {
                    dropdownExpandido = !dropdownExpandido
                },
                modifier = Modifier.weight(1.2f)
            ) {
                OutlinedTextField(
                    value = dificultad,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Dificultad") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = dropdownExpandido
                        )
                    },
                    colors = coloresCampos,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.menuAnchor(
                        MenuAnchorType.PrimaryNotEditable,
                        true
                    )
                )

                ExposedDropdownMenu(
                    expanded = dropdownExpandido,
                    onDismissRequest = {
                        dropdownExpandido = false
                    },
                    modifier = Modifier.background(GrisCampos)
                ) {
                    opcionesDificultad.forEach { opcion ->
                        DropdownMenuItem(
                            text = {
                                Text(opcion, color = Color.White)
                            },
                            onClick = {
                                dificultad = opcion
                                dropdownExpandido = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            OutlinedTextField(
                value = participantes,
                onValueChange = { participantes = it },
                label = { Text("Cupos") },
                colors = coloresCampos,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(0.8f)
            )
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción opcional") },
            placeholder = { Text("Ej: Traer camiseta blanca y negra...") },
            colors = coloresCampos,
            shape = RoundedCornerShape(10.dp),
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(28.dp))

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        Button(
            onClick = {
                val participantesInt = participantes.toIntOrNull()

                when {
                    titulo.isBlank() -> {
                        error = "El título es obligatorio"
                    }

                    fecha.isBlank() -> {
                        error = "La fecha es obligatoria"
                    }

                    modoUbicacion == ModoUbicacion.CANCHA_APP && canchaSeleccionada == null -> {
                        error = "Seleccioná una cancha o usá una ubicación libre"
                    }

                    modoUbicacion == ModoUbicacion.CANCHA_APP && turnoSeleccionado == null -> {
                        error = "Seleccioná un turno disponible de la cancha"
                    }

                    modoUbicacion == ModoUbicacion.UBICACION_LIBRE && horario.isBlank() -> {
                        error = "El horario es obligatorio"
                    }

                    ubicacion.isBlank() -> {
                        error = "La ubicación es obligatoria"
                    }

                    participantesInt == null || participantesInt <= 0 -> {
                        error = "La cantidad de participantes debe ser mayor a 0"
                    }

                    else -> {
                        error = null

                        val cancha = canchaSeleccionada

                        onCrearPartido(
                            titulo.trim(),
                            horario.trim(),
                            fecha.trim(),
                            ubicacion.trim(),
                            dificultad.trim(),
                            participantesInt,
                            descripcion.trim(),
                            cancha?.id as Int?,
                            cancha?.nombre,
                            if (modoUbicacion == ModoUbicacion.CANCHA_APP) {
                                cancha?.latitud
                            } else {
                                posicionSeleccionada.latitude
                            },
                            if (modoUbicacion == ModoUbicacion.CANCHA_APP) {
                                cancha?.longitud
                            } else {
                                posicionSeleccionada.longitude
                            }
                        )
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = VerdeMetalico
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = if (modoUbicacion == ModoUbicacion.CANCHA_APP) {
                    "Solicitar reserva y crear partido"
                } else {
                    "Publicar partido"
                },
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onVolver,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, Color.Gray),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "Cancelar",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(Modifier.height(24.dp))
    }

    if (mostrarDatePicker) {
        DatePickerDialog(
            onDismissRequest = {
                mostrarDatePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis

                        if (millis != null) {
                            val formatter = SimpleDateFormat(
                                "dd/MM/yyyy",
                                Locale.getDefault()
                            )

                            fecha = formatter.format(Date(millis))
                            diasAdelante = 0
                            turnoSeleccionado = null
                            horario = ""
                        }

                        mostrarDatePicker = false
                    }
                ) {
                    Text("Aceptar", color = VerdeMetalico)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDatePicker = false
                    }
                ) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun SelectorTurnoCancha(
    cancha: Cancha,
    tituloDia: String,
    diaSeleccionado: String,
    fechaTextoCompleta: String,
    turnosDelDia: List<String>,
    turnoSeleccionado: String?,
    esHoy: Boolean,
    ahora: Calendar,
    onAnteriorDia: () -> Unit,
    onSiguienteDia: () -> Unit,
    onVolverAHoy: () -> Unit,
    onSeleccionarTurno: (String) -> Unit
) {
    Text(
        text = "Seleccioná un turno en ${cancha.nombre}",
        color = VerdeMetalico,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.labelLarge
    )

    Spacer(Modifier.height(8.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GrisTarjeta, RoundedCornerShape(12.dp))
            .border(1.dp, GrisBorde, RoundedCornerShape(12.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedButton(
            onClick = onAnteriorDia,
            enabled = !esHoy,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextoClaro)
        ) {
            Text("←")
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = tituloDia,
                color = TextoClaro,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = fechaTextoCompleta,
                color = TextoSecundario,
                style = MaterialTheme.typography.bodySmall
            )

            if (!esHoy) {
                Text(
                    text = "Volver a hoy",
                    color = VerdeMetalico,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.clickable {
                        onVolverAHoy()
                    }
                )
            }
        }

        OutlinedButton(
            onClick = onSiguienteDia,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = VerdeMetalico)
        ) {
            Text("→")
        }
    }

    Spacer(Modifier.height(10.dp))

    if (turnosDelDia.isEmpty()) {
        Text(
            text = "La cancha no tiene horarios disponibles para $diaSeleccionado.",
            color = ColorRojo,
            style = MaterialTheme.typography.bodySmall
        )
    } else {
        Box(modifier = Modifier.heightIn(max = 280.dp)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(turnosDelDia) { hora ->
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

                    val esElSeleccionado = turnoSeleccionado == hora
                    val deshabilitado = turnoYaPaso

                    val colorFondoCaja = when {
                        deshabilitado -> GrisCampos.copy(alpha = 0.4f)
                        esElSeleccionado -> VerdeMetalico
                        else -> GrisCampos
                    }

                    val colorTextoCaja = when {
                        deshabilitado -> TextoSecundario.copy(alpha = 0.4f)
                        esElSeleccionado -> Color.Black
                        else -> TextoClaro
                    }

                    val colorBordeCaja = when {
                        esElSeleccionado -> VerdeMetalico
                        deshabilitado -> GrisBorde.copy(alpha = 0.3f)
                        else -> GrisBorde
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colorFondoCaja, RoundedCornerShape(8.dp))
                            .border(1.dp, colorBordeCaja, RoundedCornerShape(8.dp))
                            .clickable(enabled = !deshabilitado) {
                                onSeleccionarTurno(hora)
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = hora,
                                color = colorTextoCaja,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Text(
                                text = if (turnoYaPaso) "Pasó" else "Disponible",
                                color = if (esElSeleccionado) {
                                    Color.Black.copy(alpha = 0.7f)
                                } else {
                                    TextoSecundario.copy(alpha = 0.8f)
                                },
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BotonModoUbicacion(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        color = if (selected) VerdeMetalico else Color.Transparent,
        shape = RoundedCornerShape(10.dp)
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (selected) Color.Black else TextoSecundario,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun AvisoReservaPendiente() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(VerdeMetalico.copy(alpha = 0.12f))
            .border(1.dp, VerdeMetalico.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = null,
            tint = VerdeMetalico,
            modifier = Modifier.size(20.dp)
        )

        Spacer(Modifier.width(10.dp))

        Text(
            text = "Si usás una cancha de la app, el partido quedará pendiente hasta que el dueño apruebe la reserva.",
            color = TextoClaro,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun CardCanchaSeleccionable(
    cancha: Cancha,
    seleccionada: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (seleccionada) VerdeMetalico.copy(alpha = 0.18f)
                else GrisTarjeta
            )
            .border(
                width = 1.dp,
                color = if (seleccionada) VerdeMetalico else GrisBorde,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable {
                onClick()
            }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(VerdeMetalico.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SportsSoccer,
                contentDescription = null,
                tint = VerdeMetalico,
                modifier = Modifier.size(23.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = cancha.nombre,
                color = TextoClaro,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = TextoSecundario,
                    modifier = Modifier.size(14.dp)
                )

                Spacer(Modifier.width(4.dp))

                Text(
                    text = cancha.ubicacion,
                    color = TextoSecundario,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        if (seleccionada) {
            Surface(
                color = VerdeMetalico,
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = "Elegida",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun CardCanchaMapaLibre(
    cancha: Cancha,
    modifier: Modifier = Modifier,
    onSeleccionar: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, GrisBorde, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = GrisTarjeta
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(GrisBorde, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = VerdeMetalico,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = cancha.nombre,
                    color = TextoClaro,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = "🏟️ Cancha • \$${cancha.precio}/hr",
                    color = TextoSecundario,
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = cancha.ubicacion,
                    color = TextoSecundario,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            }

            Button(
                onClick = onSeleccionar,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdeMetalico
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Text(
                    text = "Seleccionar",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

private fun calcularDistanciaKm(
    p1: LatLng,
    p2: LatLng
): Double {
    val radioTierraKm = 6371.0
    val dLat = Math.toRadians(p2.latitude - p1.latitude)
    val dLon = Math.toRadians(p2.longitude - p1.longitude)

    val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(p1.latitude)) *
            cos(Math.toRadians(p2.latitude)) *
            sin(dLon / 2).pow(2)

    val c = 2 * atan2(
        sqrt(a),
        sqrt(1 - a)
    )

    return radioTierraKm * c
}

private fun obtenerFechaParaHorarios(
    fecha: String,
    diasAdelante: Int
): Calendar {
    val calendar = Calendar.getInstance()

    if (fecha.isNotBlank()) {
        try {
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = formatter.parse(fecha)

            if (date != null) {
                calendar.time = date
            }
        } catch (_: Exception) {
        }
    }

    calendar.add(Calendar.DAY_OF_YEAR, diasAdelante)

    return calendar
}

private fun mismoDia(
    a: Calendar,
    b: Calendar
): Boolean {
    return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
            a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
}

private fun generarTurnosDelDia(
    cancha: Cancha?,
    diaSeleccionado: String
): List<String> {
    if (cancha == null) return emptyList()

    val horarioEspecifico = cancha.horarios.find { horario ->
        horario.dia.lowercase() == diaSeleccionado.lowercase()
    } ?: return emptyList()

    return try {
        val aperturaItem = horarioEspecifico.horaApertura.split(":")
        val cierreItem = horarioEspecifico.horaCierre.split(":")

        var horaActual = aperturaItem[0].toInt()
        val horaFin = cierreItem[0].toInt()

        val horaLimite = if (horaFin <= horaActual) {
            horaFin + 24
        } else {
            horaFin
        }

        val turnos = mutableListOf<String>()

        while (horaActual < horaLimite) {
            val horaNormalizada = horaActual % 24
            val stringHora = String.format(Locale.US, "%02d:00", horaNormalizada)
            turnos.add(stringHora)
            horaActual++
        }

        turnos
    } catch (_: Exception) {
        emptyList()
    }
}