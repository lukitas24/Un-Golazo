package com.example.futbolnomade.presentation.ui

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location // IMPORTANTE: Agregado para el tipado explícito
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.futbolnomade.domain.model.Cancha
import com.example.futbolnomade.domain.model.EstadoPartido
import com.example.futbolnomade.domain.model.Partido
import com.google.android.gms.location.LocationServices // IMPORTANTE: Asegúrate de que esté este import
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlin.math.*

// PALETA DE COLORES OFICIAL DE FÚTBOL NÓMADE
private val ColorFondo   = Color(0xFF1A1A1A)
private val ColorTarjeta = Color(0xFF242424)
private val ColorBorde   = Color(0xFF2E2E2E)
private val ColorVerde   = Color(0xFF8BC34A)
private val ColorTexto   = Color(0xFFEEEEEE)
private val ColorSubtexto = Color(0xFF999999)

private sealed class ElementoMapa {
    data class EsCancha(val cancha: Cancha) : ElementoMapa()
    data class EsPartido(val partido: Partido) : ElementoMapa()
}

@SuppressLint("MissingPermission")
@Composable
fun CercaDeMiScreen(
    canchas: List<Cancha>,
    partidos: List<Partido>,
    onVerDetalleCancha: (String) -> Unit,
    onVerDetallePartido: (String) -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current

    // Coordenada por defecto en caso de que no haya GPS o permisos
    val posicionDefecto = LatLng(-42.7692, -65.0385)

    // Aquí guardaremos de forma ESTABLE tu posición GPS real para el filtro
    var ubicacionUsuarioParaFiltro by remember { mutableStateOf(posicionDefecto) }
    var tienePermisoUbicacion by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(posicionDefecto, 13f)
    }

    // Inicializador del cliente de ubicación nativo
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val launcherPermisos = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        val aprobado = permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        tienePermisoUbicacion = aprobado

        if (aprobado) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                // Tipado explícito 'Location?' añadido para evitar que Kotlin falle al inferir el tipo
                location?.let {
                    val miGps = LatLng(it.latitude, it.longitude)
                    ubicacionUsuarioParaFiltro = miGps
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(miGps, 13f)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        launcherPermisos.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    var radioSeleccionadoKm by remember { mutableStateOf(5.0) }
    var dropdownExpandido by remember { mutableStateOf(false) }
    val opcionesRadio = listOf(1.0, 3.0, 5.0, 10.0, 20.0, 50.0)

    var elementoSeleccionado by remember { mutableStateOf<ElementoMapa?>(null) }

    // FILTRADO ROBUSTO: Las posiciones simuladas y el filtro se calculan contra la ubicación fija
    val marcadoresFiltrados = remember(canchas, partidos, radioSeleccionadoKm, ubicacionUsuarioParaFiltro) {
        val canchasMapeadas = canchas.mapIndexed { index, cancha ->
            val pos = try {
                val partes = cancha.ubicacion.split(",")
                LatLng(partes[0].trim().toDouble(), partes[1].trim().toDouble())
            } catch (e: Exception) {
                LatLng(ubicacionUsuarioParaFiltro.latitude + (index * 0.015), ubicacionUsuarioParaFiltro.longitude - (index * 0.015))
            }
            ElementoMapa.EsCancha(cancha) to pos
        }

        val partidosMapeados = partidos
            .filter { it.estado == EstadoPartido.PUBLICADO || it.estado == EstadoPartido.RESERVA_APROBADA }
            .mapIndexed { index, partido ->
            val pos = try {
                val partes = partido.ubicacion.split(",")
                LatLng(partes[0].trim().toDouble(), partes[1].trim().toDouble())
            } catch (e: Exception) {
                LatLng(ubicacionUsuarioParaFiltro.latitude - (index * 0.02), ubicacionUsuarioParaFiltro.longitude + (index * 0.012))
            }
            ElementoMapa.EsPartido(partido) to pos
        }

        (canchasMapeadas + partidosMapeados).filter { (_, coordenada) ->
            calcularDistanciaKm(ubicacionUsuarioParaFiltro, coordenada) <= radioSeleccionadoKm
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(ColorFondo)) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = tienePermisoUbicacion
            ),
            properties = MapProperties(
                isMyLocationEnabled = tienePermisoUbicacion
            ),
            onMapClick = { elementoSeleccionado = null }
        ) {
            marcadoresFiltrados.forEach { (elemento, coordenada) ->
                when (elemento) {
                    is ElementoMapa.EsCancha -> {
                        Marker(
                            state = MarkerState(position = coordenada),
                            title = elemento.cancha.nombre,
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                            snippet = "Precio: \$${elemento.cancha.precio}",
                            onClick = {
                                elementoSeleccionado = elemento
                                false
                            }
                        )
                    }
                    is ElementoMapa.EsPartido -> {
                        Marker(
                            state = MarkerState(position = coordenada),
                            title = elemento.partido.titulo,
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                            snippet = "Horario: ${elemento.partido.horario} hs",
                            onClick = {
                                elementoSeleccionado = elemento
                                false
                            }
                        )
                    }
                }
            }
        }

        // TOP BAR FLOTANTE
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onVolver,
                    modifier = Modifier.background(ColorFondo.copy(alpha = 0.9f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = ColorTexto)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = ColorFondo.copy(alpha = 0.9f)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "Cerca mío",
                        color = ColorTexto,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }

            // Selector de Radio (Dropdown)
            Box {
                Button(
                    onClick = { dropdownExpandido = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorTarjeta.copy(alpha = 0.95f)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.border(1.dp, ColorBorde, RoundedCornerShape(20.dp)),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("Radio: ${radioSeleccionadoKm.toInt()} km ⌄", color = ColorVerde, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                DropdownMenu(
                    expanded = dropdownExpandido,
                    onDismissRequest = { dropdownExpandido = false },
                    modifier = Modifier.background(ColorTarjeta)
                ) {
                    opcionesRadio.forEach { km ->
                        DropdownMenuItem(
                            text = { Text("$km km", color = ColorTexto) },
                            onClick = {
                                radioSeleccionadoKm = km
                                dropdownExpandido = false
                            }
                        )
                    }
                }
            }
        }

        // PANEL INFERIOR DE DETALLE
        elementoSeleccionado?.let { elemento ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ColorBorde, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = ColorTarjeta),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(ColorBorde, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (elemento is ElementoMapa.EsCancha) Icons.Default.Place else Icons.Default.Home,
                                contentDescription = null,
                                tint = ColorVerde,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when (elemento) {
                                    is ElementoMapa.EsCancha -> elemento.cancha.nombre
                                    is ElementoMapa.EsPartido -> elemento.partido.titulo
                                },
                                color = ColorTexto,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 1
                            )
                            Text(
                                text = when (elemento) {
                                    is ElementoMapa.EsCancha -> "🏟️ Complejo • \$${elemento.cancha.precio}/hr"
                                    is ElementoMapa.EsPartido -> "⚽ Partido • ${elemento.partido.fecha} a las ${elemento.partido.horario}hs"
                                },
                                color = ColorSubtexto,
                                fontSize = 13.sp
                            )
                        }

                        Button(
                            onClick = {
                                when (elemento) {
                                    is ElementoMapa.EsCancha -> onVerDetalleCancha(elemento.cancha.id)
                                    is ElementoMapa.EsPartido -> onVerDetallePartido(elemento.partido.id)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorVerde),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("Ver", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

// FÓRMULA DE HAVERSINE
private fun calcularDistanciaKm(p1: LatLng, p2: LatLng): Double {
    val radioTierraKm = 6371.0
    val dLat = Math.toRadians(p2.latitude - p1.latitude)
    val dLon = Math.toRadians(p2.longitude - p1.longitude)

    val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(p1.latitude)) * cos(Math.toRadians(p2.latitude)) * sin(dLon / 2).pow(2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return radioTierraKm * c
}