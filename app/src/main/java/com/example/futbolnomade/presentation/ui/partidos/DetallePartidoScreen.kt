package com.example.futbolnomade.presentation.ui.partidos

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.futbolnomade.domain.model.EstadoPartido
import com.example.futbolnomade.domain.model.Partido
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.util.Locale

private val FondoPantalla = Color(0xFF171717)
private val FondoSuperior = Color(0xFF1D1D1D)
private val FondoTarjeta = Color(0xFF242424)
private val FondoTarjetaSecundaria = Color(0xFF2B2B2B)
private val ColorBorde = Color(0xFF383838)
private val VerdePrincipal = Color(0xFF8BC34A)
private val TextoPrincipal = Color(0xFFF2F2F2)
private val TextoSecundario = Color(0xFFA8A8A8)
private val RojoEliminar = Color(0xFFE05252)
private val AmarilloPendiente = Color(0xFFFFC107)

@Composable
fun DetallePartidoScreen(
    partido: Partido?,
    usuarioActual: String,
    onAnotarse: (String, String) -> Unit,
    onCancelarInscripcion: (String, String) -> Unit,
    onEliminarJugador: (String, String) -> Unit,
    onEliminarPartido: (String) -> Unit,
    puedeValorar: Boolean,
    yaValoro: Boolean,
    onValorarPartido: (String) -> Unit,
    onVolver: () -> Unit
) {
    if (partido == null) {
        PartidoNoEncontrado(onVolver = onVolver)
        return
    }

    val usuarioNormalizado = usuarioActual.trim()
    val esCreador = partido.creador.trim().equals(usuarioNormalizado, ignoreCase = true)
    val yaEstaAnotado = partido.usuariosAnotados.any { it.trim().equals(usuarioNormalizado, ignoreCase = true) }
    val partidoLleno = partido.participantesActuales >= partido.participantesMaximos
    val permiteInscripciones = partido.estado == EstadoPartido.PUBLICADO || partido.estado == EstadoPartido.RESERVA_APROBADA

    val context = LocalContext.current
    var jugadorAEliminar by remember { mutableStateOf<String?>(null) }
    var confirmarEliminarPartido by remember { mutableStateOf(false) }

    val coordenadasPartido = remember(partido.latitud, partido.longitud, partido.ubicacion) {
        obtenerCoordenadasPartido(partido)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(coordenadasPartido ?: LatLng(-42.7692, -65.0385), 15f)
    }

    Scaffold(
        containerColor = FondoPantalla,
        topBar = { BarraSuperiorDetalle(onVolver = onVolver) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CabeceraPartido(partido = partido, esCreador = esCreador)

            when (partido.estado) {
                EstadoPartido.PENDIENTE_RESERVA -> {
                    AvisoEstadoPartido(
                        icono = Icons.Default.Warning,
                        titulo = "Pendiente de aprobación",
                        mensaje = "El dueño de la cancha todavía debe aprobar la reserva.",
                        color = AmarilloPendiente
                    )
                }
                EstadoPartido.RESERVA_RECHAZADA -> {
                    AvisoEstadoPartido(
                        icono = Icons.Default.Warning,
                        titulo = "Reserva rechazada",
                        mensaje = "La solicitud para utilizar esta cancha fue rechazada.",
                        color = RojoEliminar
                    )
                }
                else -> Unit
            }

            DatosPrincipalesPartido(partido = partido)
            InformacionPartido(partido = partido)
            UbicacionPartido(
                partido = partido,
                coordenadasPartido = coordenadasPartido,
                cameraPositionState = cameraPositionState,
                onAbrirMapa = {
                    coordenadasPartido?.let {
                        abrirUbicacionEnMapa(context, it, partido.nombreCancha ?: partido.titulo)
                    }
                }
            )

            ParticipantesPartido(
                partido = partido,
                usuarioActual = usuarioActual,
                esCreador = esCreador,
                onSolicitarEliminarJugador = { jugadorAEliminar = it }
            )

            AccionesPartido(
                partido = partido,
                esCreador = esCreador,
                yaEstaAnotado = yaEstaAnotado,
                partidoLleno = partidoLleno,
                permiteInscripciones = permiteInscripciones,
                puedeValorar = puedeValorar,
                yaValoro = yaValoro,
                onAnotarse = { onAnotarse(partido.id, usuarioActual) },
                onCancelarInscripcion = { onCancelarInscripcion(partido.id, usuarioActual) },
                onValorar = { onValorarPartido(partido.id) },
                onCompartir = { compartirPartido(context, partido) },
                onSolicitarEliminarPartido = { confirmarEliminarPartido = true }
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    jugadorAEliminar?.let { usuario ->
        AlertDialog(
            onDismissRequest = { jugadorAEliminar = null },
            containerColor = FondoTarjeta,
            title = { Text("Eliminar participante", color = TextoPrincipal, fontWeight = FontWeight.Bold) },
            text = { Text("¿Querés eliminar a $usuario de este partido?", color = TextoSecundario) },
            confirmButton = {
                TextButton(onClick = {
                    onEliminarJugador(partido.id, usuario)
                    jugadorAEliminar = null
                }) {
                    Text("Eliminar", color = RojoEliminar, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { jugadorAEliminar = null }) {
                    Text("Cancelar", color = TextoPrincipal)
                }
            }
        )
    }

    if (confirmarEliminarPartido) {
        AlertDialog(
            onDismissRequest = { confirmarEliminarPartido = false },
            containerColor = FondoTarjeta,
            title = { Text("Eliminar partido", color = TextoPrincipal, fontWeight = FontWeight.Bold) },
            text = { Text("¿Seguro que querés eliminar \"${partido.titulo}\"? Esta acción no se puede deshacer.", color = TextoSecundario) },
            confirmButton = {
                TextButton(onClick = {
                    onEliminarPartido(partido.id)
                    confirmarEliminarPartido = false
                }) {
                    Text("Eliminar partido", color = RojoEliminar, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarEliminarPartido = false }) {
                    Text("Cancelar", color = TextoPrincipal)
                }
            }
        )
    }
}

@Composable
private fun BarraSuperiorDetalle(onVolver: () -> Unit) {
    Surface(color = FondoSuperior, shadowElevation = 4.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = TextoPrincipal)
            }
            Text("Detalle del partido", color = TextoPrincipal, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PartidoNoEncontrado(onVolver: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(FondoPantalla).padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.SportsSoccer, null, tint = TextoSecundario, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(20.dp))
        Text("No se encontró el partido", color = TextoPrincipal, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onVolver, colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal)) {
            Text("Volver", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CabeceraPartido(partido: Partido, esCreador: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = FondoTarjeta,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, ColorBorde)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(partido.titulo, color = TextoPrincipal, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, tint = TextoSecundario, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(partido.creador, color = TextoSecundario, fontSize = 14.sp)
                    }
                }
                EstadoPartidoChip(estado = partido.estado)
            }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, null, tint = AmarilloPendiente, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(String.format(Locale.getDefault(), "%.1f", partido.calificacionCreador), color = TextoPrincipal, fontWeight = FontWeight.Bold)
                Text("  Calificación organizador", color = TextoSecundario, fontSize = 12.sp)
            }
            if (esCreador) {
                Spacer(Modifier.height(12.dp))
                Surface(color = VerdePrincipal.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                    Text("Eres el organizador", color = VerdePrincipal, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun AvisoEstadoPartido(icono: ImageVector, titulo: String, mensaje: String, color: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icono, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(titulo, color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(mensaje, color = TextoSecundario, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun DatosPrincipalesPartido(partido: Partido) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DatoPrincipalCard(Icons.Default.DateRange, "Fecha", partido.fecha, Modifier.weight(1f))
        DatoPrincipalCard(Icons.Default.Schedule, "Hora", "${partido.horario} hs", Modifier.weight(1f))
        DatoPrincipalCard(Icons.Default.Group, "Cupos", "${partido.participantesActuales}/${partido.participantesMaximos}", Modifier.weight(1f))
    }
}

@Composable
private fun DatoPrincipalCard(icono: ImageVector, titulo: String, valor: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = FondoTarjeta, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, ColorBorde)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icono, null, tint = VerdePrincipal, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(titulo, color = TextoSecundario, fontSize = 10.sp)
            Text(valor, color = TextoPrincipal, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
private fun InformacionPartido(partido: Partido) {
    SeccionDetalle("Información", Icons.Default.Info) {
        DatoHorizontal("Dificultad", partido.dificultad)
        if (partido.descripcion.isNotBlank()) {
            HorizontalDivider(color = ColorBorde, modifier = Modifier.padding(vertical = 8.dp))
            Text("Descripción", color = TextoSecundario, fontSize = 12.sp)
            Text(partido.descripcion, color = TextoPrincipal, fontSize = 14.sp)
        }
    }
}

@Composable
private fun UbicacionPartido(partido: Partido, coordenadasPartido: LatLng?, cameraPositionState: CameraPositionState, onAbrirMapa: () -> Unit) {
    SeccionDetalle("Ubicación", Icons.Default.Place) {
        val nombreLugar = partido.nombreCancha?.takeIf { it.isNotBlank() }
        if (nombreLugar != null) {
            Text(nombreLugar, color = TextoPrincipal, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(partido.ubicacion, color = TextoSecundario, fontSize = 13.sp)
        } else {
            Text(partido.ubicacion.ifBlank { "Sin ubicación" }, color = TextoPrincipal, fontSize = 14.sp)
        }

        if (coordenadasPartido != null) {
            Spacer(Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp))) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(zoomControlsEnabled = false, scrollGesturesEnabled = false),
                    properties = MapProperties(isMyLocationEnabled = false)
                ) {
                    Marker(state = remember(partido.id) { MarkerState(position = coordenadasPartido) }, title = nombreLugar ?: "Lugar")
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onAbrirMapa, modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, VerdePrincipal)) {
                Icon(Icons.Default.Map, null, modifier = Modifier.size(18.dp), tint = VerdePrincipal)
                Spacer(Modifier.width(8.dp))
                Text("Abrir en Mapas", color = VerdePrincipal)
            }
        }
    }
}

@Composable
private fun ParticipantesPartido(partido: Partido, usuarioActual: String, esCreador: Boolean, onSolicitarEliminarJugador: (String) -> Unit) {
    SeccionDetalle("Participantes", Icons.Default.Group) {
        partido.usuariosAnotados.forEachIndexed { index, usuario ->
            val esYo = usuario.trim().equals(usuarioActual.trim(), ignoreCase = true)
            val esOrg = usuario.trim().equals(partido.creador.trim(), ignoreCase = true)
            ParticipanteItem(
                usuario = usuario,
                esYo = esYo,
                esOrg = esOrg,
                puedeEliminar = esCreador && !esOrg,
                onEliminar = { onSolicitarEliminarJugador(usuario) }
            )
            if (index < partido.usuariosAnotados.size - 1) {
                HorizontalDivider(color = ColorBorde, modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}

@Composable
private fun ParticipanteItem(usuario: String, esYo: Boolean, esOrg: Boolean, puedeEliminar: Boolean, onEliminar: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(modifier = Modifier.size(32.dp), color = if (esYo) VerdePrincipal else FondoTarjetaSecundaria, shape = CircleShape) {
            Box(contentAlignment = Alignment.Center) {
                Text(usuario.firstOrNull()?.uppercase() ?: "?", color = if (esYo) Color.Black else TextoPrincipal, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(usuario, color = if (esYo) VerdePrincipal else TextoPrincipal, fontSize = 14.sp, fontWeight = if (esYo || esOrg) FontWeight.Bold else FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (esOrg) Text("Organizador", color = VerdePrincipal, fontSize = 11.sp)
            else if (esYo) Text("Tú", color = VerdePrincipal, fontSize = 11.sp)
        }
        if (puedeEliminar) {
            IconButton(onClick = onEliminar) {
                Icon(Icons.Default.Delete, null, tint = RojoEliminar, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun AccionesPartido(
    partido: Partido,
    esCreador: Boolean,
    yaEstaAnotado: Boolean,
    partidoLleno: Boolean,
    permiteInscripciones: Boolean,
    puedeValorar: Boolean,
    yaValoro: Boolean,
    onAnotarse: () -> Unit,
    onCancelarInscripcion: () -> Unit,
    onValorar: () -> Unit,
    onCompartir: () -> Unit,
    onSolicitarEliminarPartido: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (!esCreador) {
            val habilitar = yaEstaAnotado || (permiteInscripciones && !partidoLleno)
            Button(
                onClick = { if (yaEstaAnotado) onCancelarInscripcion() else onAnotarse() },
                enabled = habilitar,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (yaEstaAnotado) RojoEliminar else VerdePrincipal, contentColor = if (yaEstaAnotado) Color.White else Color.Black)
            ) {
                Text(if (yaEstaAnotado) "Cancelar inscripción" else if (partidoLleno) "Partido lleno" else "Anotarme", fontWeight = FontWeight.Bold)
            }
        }

        if (puedeValorar && !yaValoro) {
            Button(onClick = onValorar, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal, contentColor = Color.Black)) {
                Icon(Icons.Default.Star, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Valorar participantes", fontWeight = FontWeight.Bold)
            }
        }

        if (yaValoro) {
            Surface(color = VerdePrincipal.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = VerdePrincipal)
                    Spacer(Modifier.width(12.dp))
                    Text("Ya valoraste este partido", color = VerdePrincipal, fontWeight = FontWeight.Bold)
                }
            }
        }

        OutlinedButton(onClick = onCompartir, modifier = Modifier.fillMaxWidth().height(50.dp), border = BorderStroke(1.dp, VerdePrincipal)) {
            Icon(Icons.Default.Share, null, tint = VerdePrincipal)
            Spacer(Modifier.width(8.dp))
            Text("Compartir", color = VerdePrincipal)
        }

        if (esCreador) {
            OutlinedButton(onClick = onSolicitarEliminarPartido, modifier = Modifier.fillMaxWidth().height(50.dp), border = BorderStroke(1.dp, RojoEliminar)) {
                Icon(Icons.Default.Delete, null, tint = RojoEliminar)
                Spacer(Modifier.width(8.dp))
                Text("Eliminar partido", color = RojoEliminar)
            }
        }
    }
}

@Composable
private fun SeccionDetalle(titulo: String, icono: ImageVector, contenido: @Composable () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), color = FondoTarjeta, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, ColorBorde)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icono, null, tint = VerdePrincipal, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(titulo, color = TextoPrincipal, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            contenido()
        }
    }
}

@Composable
private fun DatoHorizontal(titulo: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(titulo, color = TextoSecundario, fontSize = 14.sp)
        Text(valor, color = VerdePrincipal, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
private fun EstadoPartidoChip(estado: EstadoPartido) {
    val color = when(estado) {
        EstadoPartido.PUBLICADO, EstadoPartido.RESERVA_APROBADA -> VerdePrincipal
        EstadoPartido.PENDIENTE_RESERVA -> AmarilloPendiente
        EstadoPartido.RESERVA_RECHAZADA -> RojoEliminar
    }
    val texto = when(estado) {
        EstadoPartido.PUBLICADO, EstadoPartido.RESERVA_APROBADA -> "Publicado"
        EstadoPartido.PENDIENTE_RESERVA -> "Pendiente"
        EstadoPartido.RESERVA_RECHAZADA -> "Rechazado"
    }
    Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, color.copy(alpha = 0.5f))) {
        Text(texto, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
    }
}

private fun obtenerCoordenadasPartido(partido: Partido): LatLng? {
    if (partido.latitud != null && partido.longitud != null) return LatLng(partido.latitud, partido.longitud)
    return try {
        val partes = partido.ubicacion.split(",")
        if (partes.size == 2) LatLng(partes[0].trim().toDouble(), partes[1].trim().toDouble()) else null
    } catch (e: Exception) { null }
}

private fun abrirUbicacionEnMapa(context: Context, coordenadas: LatLng, nombre: String) {
    val uri = Uri.parse("geo:${coordenadas.latitude},${coordenadas.longitude}?q=${coordenadas.latitude},${coordenadas.longitude}(${Uri.encode(nombre)})")
    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
}

private fun compartirPartido(context: Context, partido: Partido) {
    val enlace = "futbolnomade://partido/${Uri.encode(partido.id)}"
    val mensaje = "⚽ Te invito a jugar \"${partido.titulo}\"\n📅 ${partido.fecha}\n🕒 ${partido.horario} hs\n📍 ${partido.nombreCancha ?: partido.ubicacion}\n\n$enlace"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, mensaje)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir partido"))
}
