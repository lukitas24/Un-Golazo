package com.example.futbolnomade.presentation.ui.partidos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import com.google.maps.android.compose.GoogleMap
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.futbolnomade.domain.model.Partido
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
private val FondoOscuro = Color(0xFF202020)
private val VerdeMetalico = Color(0xFF82A820)
private val GrisContenedor = Color(0xFF2E2E2E)
private val RojoEliminar = Color(0xFFC62828)

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
    onValorarPartido: (String) ->Unit,
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
            Text(
                text = "No se encontró el partido",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onVolver,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdeMetalico
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Volver",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        return
    }

    val esCreador = partido.creador == usuarioActual
    val yaEstaAnotado = partido.usuariosAnotados.contains(usuarioActual)
    val partidoLleno =
        partido.participantesActuales >= partido.participantesMaximos

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
        if (partido.latitud != null && partido.longitud != null) {
            LatLng(
                partido.latitud,
                partido.longitud
            )
        } else {
            try {
                val partes = partido.ubicacion.split(",")

                if (partes.size == 2) {
                    LatLng(
                        partes[0].trim().toDouble(),
                        partes[1].trim().toDouble()
                    )
                } else {
                    LatLng(-42.7692, -65.0385)
                }
            } catch (_: Exception) {
                LatLng(-42.7692, -65.0385)
            }
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            coordenadasPartido,
            15f
        )
    }

    val scrollState = rememberScrollState()
    var scrollHabilitado by remember {
        mutableStateOf(true)
    }

    LaunchedEffect(cameraPositionState.isMoving) {
        scrollHabilitado = !cameraPositionState.isMoving
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoOscuro)
            .verticalScroll(
                state = scrollState,
                enabled = scrollHabilitado
            )
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(
            text = partido.titulo,
            color = VerdeMetalico,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Organizado por: ${partido.creador}  ⭐ ${partido.calificacionCreador}",
            color = Color.LightGray,
            style = MaterialTheme.typography.bodyMedium
        )

        if (esCreador) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sos el creador de este partido",
                color = VerdeMetalico,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = GrisContenedor,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    Text(
                        text = "📅 Fecha: ",
                        color = VerdeMetalico,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = partido.fecha,
                        color = Color.White
                    )
                }

                Row {
                    Text(
                        text = "🕒 Horario: ",
                        color = VerdeMetalico,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${partido.horario} hs",
                        color = Color.White
                    )
                }
            }

            Divider(
                color = FondoOscuro,
                thickness = 1.dp
            )

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🔥 Dificultad: ",
                    color = VerdeMetalico,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = partido.dificultad,
                    color = Color.White
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "👥 Cupos: ",
                    color = VerdeMetalico,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${partido.participantesActuales} / ${partido.participantesMaximos}",
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (partido.descripcion.isNotBlank()) {
            Text(
                text = "Descripción",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = GrisContenedor,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = partido.descripcion,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = "Punto de encuentro",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(285.dp)
                .background(
                    color = GrisContenedor,
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = 1.dp,
                    color = GrisContenedor,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(vertical = 8.dp)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    scrollGesturesEnabled = true
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
                        ?: "Lugar del partido"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Lista de convocados",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = GrisContenedor,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (partido.usuariosAnotados.isEmpty()) {
                Text(
                    text = "Todavía no hay participantes anotados",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                partido.usuariosAnotados.forEach { usuario ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚽",
                            modifier = Modifier.padding(end = 8.dp)
                        )

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = usuario,
                                color = if (usuario == usuarioActual) {
                                    VerdeMetalico
                                } else {
                                    Color.White
                                },
                                fontWeight = if (
                                    usuario == usuarioActual ||
                                    usuario == partido.creador
                                ) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                }
                            )

                            when {
                                usuario == partido.creador -> {
                                    Text(
                                        text = "Organizador",
                                        color = VerdeMetalico,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                usuario == usuarioActual -> {
                                    Text(
                                        text = "Tú",
                                        color = VerdeMetalico,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }

                        if (
                            esCreador &&
                            usuario != partido.creador
                        ) {
                            TextButton(
                                onClick = {
                                    jugadorAEliminar = usuario
                                }
                            ) {
                                Text(
                                    text = "Eliminar",
                                    color = RojoEliminar,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (!esCreador) {
            val colorFondoBoton = if (yaEstaAnotado) {
                RojoEliminar
            } else {
                VerdeMetalico
            }

            val colorTextoBoton = if (yaEstaAnotado) {
                Color.White
            } else {
                Color.Black
            }

            Button(
                onClick = {
                    if (yaEstaAnotado) {
                        onCancelarInscripcion(
                            partido.id,
                            usuarioActual
                        )
                    } else {
                        onAnotarse(
                            partido.id,
                            usuarioActual
                        )
                    }
                },
                enabled = !partidoLleno || yaEstaAnotado,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorFondoBoton,
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
                    color = if (!partidoLleno || yaEstaAnotado) {
                        colorTextoBoton
                    } else {
                        Color.Gray
                    },
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (esCreador) {
            Button(
                onClick = {
                    confirmarEliminarPartido = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = RojoEliminar
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "Eliminar partido",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (puedeValorar && !yaValoro) {
            Button(
                onClick = {
                    onValorarPartido(partido.id)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdeMetalico
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "Valorar partido",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (yaValoro) {
            Text(
                text = "✓ Ya valoraste este partido",
                color = VerdeMetalico,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            )
        }

        Button(
            onClick = {
                val enlacePartido =
                    "futbolnomade://partido/${Uri.encode(partido.id)}"

                val intentCompartir = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"

                    putExtra(
                        Intent.EXTRA_SUBJECT,
                        "Te invito a jugar un partido"
                    )

                    putExtra(
                        Intent.EXTRA_TEXT,
                        """
                ⚽ Te invito a jugar "${partido.titulo}"
                
                📅 ${partido.fecha}
                🕒 ${partido.horario} hs
                📍 ${partido.nombreCancha ?: partido.ubicacion}
                
                Abrí el partido desde acá:
                $enlacePartido
                """.trimIndent()
                    )
                }

                context.startActivity(
                    Intent.createChooser(
                        intentCompartir,
                        "Compartir partido"
                    )
                )
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = VerdeMetalico
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = "Compartir partido",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onVolver,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(
                width = 1.dp,
                color = Color.Gray
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = "Volver",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    jugadorAEliminar?.let { usuario ->
        AlertDialog(
            onDismissRequest = {
                jugadorAEliminar = null
            },
            containerColor = GrisContenedor,
            title = {
                Text(
                    text = "Eliminar jugador",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "¿Querés eliminar a $usuario del partido?",
                    color = Color.LightGray
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
                        color = Color.White
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
            containerColor = GrisContenedor,
            title = {
                Text(
                    text = "Eliminar partido",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "¿Seguro que querés eliminar \"${partido.titulo}\"? Esta acción no se puede deshacer.",
                    color = Color.LightGray
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
                        color = Color.White
                    )
                }
            }
        )
    }
}
