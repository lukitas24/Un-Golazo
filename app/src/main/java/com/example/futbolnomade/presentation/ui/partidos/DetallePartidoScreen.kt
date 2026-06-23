package com.example.futbolnomade.presentation.ui.partidos

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

private val FondoPantalla = Color(0xFF171717)
private val FondoSuperior = Color(0xFF1D1D1D)
private val FondoTarjeta = Color(0xFF242424)
private val FondoTarjetaSecundaria = Color(0xFF2B2B2B)
private val ColorBorde = Color(0xFF383838)
private val VerdePrincipal = Color(0xFF8BC34A)
private val VerdeOscuro = Color(0xFF618D25)
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
        PartidoNoEncontrado(
            onVolver = onVolver
        )

        return
    }

    val usuarioNormalizado = usuarioActual.trim()

    val esCreador = partido.creador
        .trim()
        .equals(
            usuarioNormalizado,
            ignoreCase = true
        )

    val yaEstaAnotado = partido.usuariosAnotados.any { usuario ->
        usuario
            .trim()
            .equals(
                usuarioNormalizado,
                ignoreCase = true
            )
    }

    val partidoLleno =
        partido.participantesActuales >= partido.participantesMaximos

    val permiteInscripciones =
        partido.estado == EstadoPartido.PUBLICADO ||
                partido.estado == EstadoPartido.RESERVA_APROBADA

    val context = LocalContext.current

    var jugadorAEliminar by remember {
        mutableStateOf<String?>(null)
    }

    var confirmarEliminarPartido by remember {
        mutableStateOf(false)
    }

    val coordenadasPartido = remember(
        partido.latitud,
        partido.longitud,
        partido.ubicacion
    ) {
        obtenerCoordenadasPartido(partido)
    }

    val posicionInicial =
        coordenadasPartido ?: LatLng(
            -42.7692,
            -65.0385
        )

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            posicionInicial,
            15f
        )
    }

    Scaffold(
        containerColor = FondoPantalla,
        topBar = {
            BarraSuperiorDetalle(
                onVolver = onVolver
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(
                    rememberScrollState()
                )
                .navigationBarsPadding()
                .padding(
                    horizontal = 16.dp,
                    vertical = 14.dp
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CabeceraPartido(
                partido = partido,
                esCreador = esCreador
            )

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

            DatosPrincipalesPartido(
                partido = partido
            )

            InformacionPartido(
                partido = partido
            )

            UbicacionPartido(
                partido = partido,
                coordenadasPartido = coordenadasPartido,
                cameraPositionState = cameraPositionState,
                onAbrirMapa = {
                    coordenadasPartido?.let { coordenadas ->
                        abrirUbicacionEnMapa(
                            context = context,
                            coordenadas = coordenadas,
                            nombreLugar = partido.nombreCancha
                                ?: partido.titulo
                        )
                    }
                }
            )

            ParticipantesPartido(
                partido = partido,
                usuarioActual = usuarioActual,
                esCreador = esCreador,
                onSolicitarEliminarJugador = { usuario ->
                    jugadorAEliminar = usuario
                }
            )

            AccionesPartido(
                partido = partido,
                esCreador = esCreador,
                yaEstaAnotado = yaEstaAnotado,
                partidoLleno = partidoLleno,
                permiteInscripciones = permiteInscripciones,
                puedeValorar = puedeValorar,
                yaValoro = yaValoro,
                onAnotarse = {
                    onAnotarse(
                        partido.id,
                        usuarioActual
                    )
                },
                onCancelarInscripcion = {
                    onCancelarInscripcion(
                        partido.id,
                        usuarioActual
                    )
                },
                onValorar = {
                    onValorarPartido(partido.id)
                },
                onCompartir = {
                    compartirPartido(
                        context = context,
                        partido = partido
                    )
                },
                onSolicitarEliminarPartido = {
                    confirmarEliminarPartido = true
                }
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )
        }
    }

    jugadorAEliminar?.let { usuario ->
        AlertDialog(
            onDismissRequest = {
                jugadorAEliminar = null
            },
            containerColor = FondoTarjeta,
            title = {
                Text(
                    text = "Eliminar participante",
                    color = TextoPrincipal,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "¿Querés eliminar a $usuario de este partido?",
                    color = TextoSecundario
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEliminarJugador(
                            partido.id,
                            usuario
                        )

                        jugadorAEliminar = null
                    }
                ) {
                    Text(
                        text = "Eliminar",
                        color = RojoEliminar,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        jugadorAEliminar = null
                    }
                ) {
                    Text(
                        text = "Cancelar",
                        color = TextoPrincipal
                    )
                }
            }
        )
    }

    if (confirmarEliminarPartido) {
        AlertDialog(
            onDismissRequest = {
                confirmarEliminarPartido = false
            },
            containerColor = FondoTarjeta,
            title = {
                Text(
                    text = "Eliminar partido",
                    color = TextoPrincipal,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "¿Seguro que querés eliminar " +
                            "\"${partido.titulo}\"? " +
                            "Esta acción no se puede deshacer.",
                    color = TextoSecundario
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEliminarPartido(partido.id)
                        confirmarEliminarPartido = false
                    }
                ) {
                    Text(
                        text = "Eliminar partido",
                        color = RojoEliminar,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        confirmarEliminarPartido = false
                    }
                ) {
                    Text(
                        text = "Cancelar",
                        color = TextoPrincipal
                    )
                }
            }
        )
    }
}

@Composable
private fun BarraSuperiorDetalle(
    onVolver: () -> Unit
) {
    Surface(
        color = FondoSuperior,
        shadowElevation = 5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(
                    horizontal = 6.dp,
                    vertical = 6.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onVolver
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = TextoPrincipal
                )
            }

            Text(
                text = "Detalle del partido",
                color = TextoPrincipal,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PartidoNoEncontrado(
    onVolver: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoPantalla)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = FondoTarjeta,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.SportsSoccer,
                contentDescription = null,
                tint = TextoSecundario,
                modifier = Modifier
                    .padding(20.dp)
                    .size(48.dp)
            )
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Text(
            text = "No se encontró el partido",
            color = TextoPrincipal,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "Puede que haya sido eliminado o que ya no esté disponible.",
            color = TextoSecundario,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Button(
            onClick = onVolver,
            colors = ButtonDefaults.buttonColors(
                containerColor = VerdePrincipal
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Volver",
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CabeceraPartido(
    partido: Partido,
    esCreador: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = FondoTarjeta,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(
            width = 1.dp,
            color = ColorBorde
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = partido.titulo.ifBlank {
                            "Partido sin nombre"
                        },
                        color = TextoPrincipal,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = TextoSecundario,
                            modifier = Modifier.size(17.dp)
                        )

                        Spacer(
                            modifier = Modifier.width(6.dp)
                        )

                        Text(
                            text = partido.creador.ifBlank {
                                "Organizador desconocido"
                            },
                            color = TextoSecundario,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.width(10.dp)
                )

                EstadoPartidoChip(
                    estado = partido.estado
                )
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = AmarilloPendiente,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(
                    modifier = Modifier.width(5.dp)
                )

                Text(
                    text = String.format(
                        "%.1f",
                        partido.calificacionCreador
                    ),
                    color = TextoPrincipal,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "  Calificación del organizador",
                    color = TextoSecundario,
                    fontSize = 12.sp
                )
            }

            if (esCreador) {
                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                Surface(
                    color = VerdePrincipal.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "Sos el organizador",
                        color = VerdePrincipal,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 6.dp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun AvisoEstadoPartido(
    icono: ImageVector,
    titulo: String,
    mensaje: String,
    color: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = color.copy(alpha = 0.11f),
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(
            width = 1.dp,
            color = color.copy(alpha = 0.65f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )

            Spacer(
                modifier = Modifier.width(10.dp)
            )

            Column {
                Text(
                    text = titulo,
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text = mensaje,
                    color = TextoSecundario,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun DatosPrincipalesPartido(
    partido: Partido
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        DatoPrincipalCard(
            icono = Icons.Default.DateRange,
            titulo = "Fecha",
            valor = partido.fecha.ifBlank {
                "Sin fecha"
            },
            modifier = Modifier.weight(1f)
        )

        DatoPrincipalCard(
            icono = Icons.Default.Schedule,
            titulo = "Horario",
            valor = partido.horario.ifBlank {
                "Sin hora"
            },
            modifier = Modifier.weight(1f)
        )

        DatoPrincipalCard(
            icono = Icons.Default.Group,
            titulo = "Jugadores",
            valor = "${partido.participantesActuales}/" +
                    "${partido.participantesMaximos}",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DatoPrincipalCard(
    icono: ImageVector,
    titulo: String,
    valor: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = FondoTarjeta,
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(
            width = 1.dp,
            color = ColorBorde
        )
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 13.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = VerdePrincipal,
                modifier = Modifier.size(21.dp)
            )

            Spacer(
                modifier = Modifier.height(7.dp)
            )

            Text(
                text = titulo,
                color = TextoSecundario,
                fontSize = 10.sp
            )

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            Text(
                text = valor,
                color = TextoPrincipal,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun InformacionPartido(
    partido: Partido
) {
    SeccionDetalle(
        titulo = "Información del partido",
        icono = Icons.Default.Info
    ) {
        DatoHorizontal(
            titulo = "Dificultad",
            valor = partido.dificultad.ifBlank {
                "No especificada"
            }
        )

        if (partido.descripcion.isNotBlank()) {
            HorizontalDivider(
                color = ColorBorde
            )

            Column {
                Text(
                    text = "Descripción",
                    color = TextoSecundario,
                    fontSize = 11.sp
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text = partido.descripcion,
                    color = TextoPrincipal,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        } else {
            HorizontalDivider(
                color = ColorBorde
            )

            Text(
                text = "El organizador no agregó una descripción.",
                color = TextoSecundario,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun UbicacionPartido(
    partido: Partido,
    coordenadasPartido: LatLng?,
    cameraPositionState:
    com.google.maps.android.compose.CameraPositionState,
    onAbrirMapa: () -> Unit
) {
    SeccionDetalle(
        titulo = "Punto de encuentro",
        icono = Icons.Default.Place
    ) {
        val nombreLugar = partido.nombreCancha
            ?.takeIf { it.isNotBlank() }

        if (nombreLugar != null) {
            Column {
                Text(
                    text = "Cancha",
                    color = TextoSecundario,
                    fontSize = 11.sp
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text = nombreLugar,
                    color = TextoPrincipal,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (partido.ubicacion.isNotBlank()) {
                Spacer(
                    modifier = Modifier.height(5.dp)
                )

                Text(
                    text = partido.ubicacion,
                    color = TextoSecundario,
                    fontSize = 12.sp
                )
            }
        } else {
            Text(
                text = partido.ubicacion.ifBlank {
                    "No se especificó una ubicación"
                },
                color = TextoPrincipal,
                fontSize = 14.sp
            )
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        if (coordenadasPartido != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(235.dp)
                    .clip(
                        RoundedCornerShape(16.dp)
                    )
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = true,
                        scrollGesturesEnabled = false
                    ),
                    properties = MapProperties(
                        isMyLocationEnabled = false
                    )
                ) {
                    Marker(
                        state = MarkerState(
                            position = coordenadasPartido
                        ),
                        title = partido.nombreCancha
                            ?: partido.titulo
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            OutlinedButton(
                onClick = onAbrirMapa,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = VerdePrincipal
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = VerdePrincipal
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(
                    modifier = Modifier.width(7.dp)
                )

                Text(
                    text = "Abrir ubicación en el mapa",
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = FondoTarjetaSecundaria,
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = TextoSecundario
                    )

                    Spacer(
                        modifier = Modifier.width(9.dp)
                    )

                    Text(
                        text = "Este partido no tiene coordenadas disponibles.",
                        color = TextoSecundario,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ParticipantesPartido(
    partido: Partido,
    usuarioActual: String,
    esCreador: Boolean,
    onSolicitarEliminarJugador: (String) -> Unit
) {
    val progresoCupos = if (partido.participantesMaximos > 0) {
        (
                partido.participantesActuales.toFloat() /
                        partido.participantesMaximos.toFloat()
                ).coerceIn(
                0f,
                1f
            )
    } else {
        0f
    }

    SeccionDetalle(
        titulo = "Participantes",
        icono = Icons.Default.Group
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${partido.participantesActuales} anotados",
                color = TextoPrincipal,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "${partido.participantesMaximos} cupos",
                color = TextoSecundario,
                fontSize = 12.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(CircleShape)
                .background(ColorBorde)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progresoCupos)
                    .height(7.dp)
                    .clip(CircleShape)
                    .background(
                        if (progresoCupos >= 1f) {
                            RojoEliminar
                        } else {
                            VerdePrincipal
                        }
                    )
            )
        }

        if (partido.usuariosAnotados.isEmpty()) {
            Text(
                text = "Todavía no hay participantes anotados.",
                color = TextoSecundario,
                fontSize = 13.sp
            )
        } else {
            partido.usuariosAnotados.forEachIndexed { indice, usuario ->

                ParticipanteItem(
                    usuario = usuario,
                    esUsuarioActual = usuario
                        .trim()
                        .equals(
                            usuarioActual.trim(),
                            ignoreCase = true
                        ),
                    esOrganizador = usuario
                        .trim()
                        .equals(
                            partido.creador.trim(),
                            ignoreCase = true
                        ),
                    puedeEliminar =
                        esCreador &&
                                !usuario.trim().equals(
                                    partido.creador.trim(),
                                    ignoreCase = true
                                ),
                    onEliminar = {
                        onSolicitarEliminarJugador(usuario)
                    }
                )

                if (indice < partido.usuariosAnotados.lastIndex) {
                    HorizontalDivider(
                        color = ColorBorde
                    )
                }
            }
        }
    }
}

@Composable
private fun ParticipanteItem(
    usuario: String,
    esUsuarioActual: Boolean,
    esOrganizador: Boolean,
    puedeEliminar: Boolean,
    onEliminar: () -> Unit
) {
    val inicial = usuario
        .substringBefore("@")
        .firstOrNull()
        ?.uppercase()
        ?: "?"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = if (esUsuarioActual) {
                VerdePrincipal.copy(alpha = 0.2f)
            } else {
                FondoTarjetaSecundaria
            },
            shape = CircleShape
        ) {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = inicial,
                    color = if (esUsuarioActual) {
                        VerdePrincipal
                    } else {
                        TextoPrincipal
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(
            modifier = Modifier.width(11.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = usuario,
                color = if (esUsuarioActual) {
                    VerdePrincipal
                } else {
                    TextoPrincipal
                },
                fontSize = 13.sp,
                fontWeight = if (
                    esUsuarioActual ||
                    esOrganizador
                ) {
                    FontWeight.Bold
                } else {
                    FontWeight.Normal
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            when {
                esOrganizador && esUsuarioActual -> {
                    Text(
                        text = "Organizador · Tú",
                        color = VerdePrincipal,
                        fontSize = 11.sp
                    )
                }

                esOrganizador -> {
                    Text(
                        text = "Organizador",
                        color = VerdePrincipal,
                        fontSize = 11.sp
                    )
                }

                esUsuarioActual -> {
                    Text(
                        text = "Tú",
                        color = VerdePrincipal,
                        fontSize = 11.sp
                    )
                }
            }
        }

        if (puedeEliminar) {
            IconButton(
                onClick = onEliminar
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar participante",
                    tint = RojoEliminar,
                    modifier = Modifier.size(20.dp)
                )
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = FondoTarjeta,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = 1.dp,
            color = ColorBorde
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Text(
                text = "Acciones",
                color = TextoPrincipal,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            if (!esCreador) {
                val habilitarBotonInscripcion =
                    yaEstaAnotado ||
                            (
                                    permiteInscripciones &&
                                            !partidoLleno
                                    )

                Button(
                    onClick = {
                        if (yaEstaAnotado) {
                            onCancelarInscripcion()
                        } else {
                            onAnotarse()
                        }
                    },
                    enabled = habilitarBotonInscripcion,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(13.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (yaEstaAnotado) {
                            RojoEliminar
                        } else {
                            VerdePrincipal
                        },
                        contentColor = if (yaEstaAnotado) {
                            Color.White
                        } else {
                            Color.Black
                        },
                        disabledContainerColor = ColorBorde,
                        disabledContentColor = TextoSecundario
                    )
                ) {
                    Text(
                        text = when {
                            yaEstaAnotado -> {
                                "Cancelar inscripción"
                            }

                            !permiteInscripciones -> {
                                "Inscripción no disponible"
                            }

                            partidoLleno -> {
                                "Partido completo"
                            }

                            else -> {
                                "Anotarme al partido"
                            }
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (puedeValorar && !yaValoro) {
                Button(
                    onClick = onValorar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(13.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VerdePrincipal,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(19.dp)
                    )

                    Spacer(
                        modifier = Modifier.width(7.dp)
                    )

                    Text(
                        text = "Valorar participantes",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (yaValoro) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = VerdePrincipal.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(13.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = VerdePrincipal,
                            modifier = Modifier.size(21.dp)
                        )

                        Spacer(
                            modifier = Modifier.width(9.dp)
                        )

                        Text(
                            text = "Ya valoraste este partido",
                            color = VerdePrincipal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = onCompartir,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(13.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = VerdePrincipal
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = VerdePrincipal
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp)
                )

                Spacer(
                    modifier = Modifier.width(7.dp)
                )

                Text(
                    text = "Compartir partido",
                    fontWeight = FontWeight.Bold
                )
            }

            if (esCreador) {
                HorizontalDivider(
                    color = ColorBorde,
                    modifier = Modifier.padding(
                        vertical = 4.dp
                    )
                )

                OutlinedButton(
                    onClick = onSolicitarEliminarPartido,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(13.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = RojoEliminar
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = RojoEliminar
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(19.dp)
                    )

                    Spacer(
                        modifier = Modifier.width(7.dp)
                    )

                    Text(
                        text = "Eliminar partido",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SeccionDetalle(
    titulo: String,
    icono: ImageVector,
    contenido: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = FondoTarjeta,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = 1.dp,
            color = ColorBorde
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = VerdePrincipal.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = VerdePrincipal,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(19.dp)
                    )
                }

                Spacer(
                    modifier = Modifier.width(10.dp)
                )

                Text(
                    text = titulo,
                    color = TextoPrincipal,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            contenido()
        }
    }
}

@Composable
private fun DatoHorizontal(
    titulo: String,
    valor: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = titulo,
            color = TextoSecundario,
            fontSize = 13.sp
        )

        Spacer(
            modifier = Modifier.weight(1f)
        )

        Surface(
            color = VerdePrincipal.copy(alpha = 0.13f),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                text = valor,
                color = VerdePrincipal,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.padding(
                    horizontal = 11.dp,
                    vertical = 6.dp
                )
            )
        }
    }
}

@Composable
private fun EstadoPartidoChip(
    estado: EstadoPartido
) {
    val color = colorEstadoPartido(estado)

    Surface(
        color = color.copy(alpha = 0.14f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = 1.dp,
            color = color.copy(alpha = 0.5f)
        )
    ) {
        Text(
            text = textoEstadoPartido(estado),
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 6.dp
            )
        )
    }
}

private fun textoEstadoPartido(
    estado: EstadoPartido
): String {
    return when (estado) {
        EstadoPartido.PUBLICADO -> {
            "Publicado"
        }

        EstadoPartido.PENDIENTE_RESERVA -> {
            "Pendiente"
        }

        EstadoPartido.RESERVA_APROBADA -> {
            "Publicado"
        }

        EstadoPartido.RESERVA_RECHAZADA -> {
            "Rechazado"
        }
    }
}

private fun colorEstadoPartido(
    estado: EstadoPartido
): Color {
    return when (estado) {
        EstadoPartido.PUBLICADO -> {
            VerdePrincipal
        }

        EstadoPartido.PENDIENTE_RESERVA -> {
            AmarilloPendiente
        }

        EstadoPartido.RESERVA_APROBADA -> {
            VerdePrincipal
        }

        EstadoPartido.RESERVA_RECHAZADA -> {
            RojoEliminar
        }
    }
}

private fun obtenerCoordenadasPartido(
    partido: Partido
): LatLng? {
    if (
        partido.latitud != null &&
        partido.longitud != null
    ) {
        return LatLng(
            partido.latitud,
            partido.longitud
        )
    }

    val partes = partido.ubicacion
        .split(",")

    if (partes.size != 2) {
        return null
    }

    val latitud = partes[0]
        .trim()
        .toDoubleOrNull()

    val longitud = partes[1]
        .trim()
        .toDoubleOrNull()

    if (
        latitud == null ||
        longitud == null
    ) {
        return null
    }

    return LatLng(
        latitud,
        longitud
    )
}

private fun abrirUbicacionEnMapa(
    context: android.content.Context,
    coordenadas: LatLng,
    nombreLugar: String
) {
    val nombreCodificado = Uri.encode(
        nombreLugar
    )

    val uri = Uri.parse(
        "geo:${coordenadas.latitude}," +
                "${coordenadas.longitude}?" +
                "q=${coordenadas.latitude}," +
                "${coordenadas.longitude}" +
                "($nombreCodificado)"
    )

    val intent = Intent(
        Intent.ACTION_VIEW,
        uri
    )

    context.startActivity(intent)
}

private fun compartirPartido(
    context: android.content.Context,
    partido: Partido
) {
    val enlacePartido =
        "futbolnomade://partido/${Uri.encode(partido.id)}"

    val lugar = partido.nombreCancha
        ?.takeIf { it.isNotBlank() }
        ?: partido.ubicacion.ifBlank {
            "Ubicación a confirmar"
        }

    val mensaje = """
        ⚽ Te invito a jugar "${partido.titulo}"
        
        📅 Fecha: ${partido.fecha}
        🕒 Horario: ${partido.horario} hs
        📍 Lugar: $lugar
        👥 Jugadores: ${partido.participantesActuales}/${partido.participantesMaximos}
        
        Abrí el partido desde acá:
        $enlacePartido
    """.trimIndent()

    val intentCompartir = Intent(
        Intent.ACTION_SEND
    ).apply {
        type = "text/plain"

        putExtra(
            Intent.EXTRA_SUBJECT,
            "Invitación a un partido"
        )

        putExtra(
            Intent.EXTRA_TEXT,
            mensaje
        )
    }

    context.startActivity(
        Intent.createChooser(
            intentCompartir,
            "Compartir partido"
        )
    )
}