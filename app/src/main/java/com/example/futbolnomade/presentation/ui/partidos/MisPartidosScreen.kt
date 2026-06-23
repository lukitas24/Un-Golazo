package com.example.futbolnomade.presentation.ui.partidos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.futbolnomade.domain.model.EstadoPartido
import com.example.futbolnomade.domain.model.Partido
import com.example.futbolnomade.presentation.viewModel.PartidoViewModel
import com.example.futbolnomade.presentation.viewModel.ValoracionViewModel
import kotlin.math.abs

private val ColorFondo = Color(0xFF1A1A1A)
private val ColorTarjeta = Color(0xFF242424)
private val ColorBorde = Color(0xFF2E2E2E)
private val ColorVerde = Color(0xFF8BC34A)
private val ColorTexto = Color(0xFFEEEEEE)
private val ColorSubtexto = Color(0xFF999999)
private val ColorRojo = Color(0xFFE53935)
private val ColorAmarillo = Color(0xFFFFC107)

@Composable
fun MisPartidosScreen(
    emailUsuario: String,
    partidoViewModel: PartidoViewModel,
    valoracionViewModel: ValoracionViewModel,
    onCrearPartido: () -> Unit,
    onAdministrarPartido: (String) -> Unit,
    onValorarPartido: (String) -> Unit,
    onVolver: () -> Unit
) {
    /*
     * Trae:
     *
     * - Partidos creados por el usuario.
     * - Partidos en los que está anotado.
     */
    val misPartidos = partidoViewModel
        .partidosDelUsuario(emailUsuario)

    val ahora = System.currentTimeMillis()

    /*
     * Ordena desde la fecha más cercana al momento actual
     * hasta la fecha más lejana.
     *
     * Los partidos sin fecha válida quedan al final.
     */
    val partidosOrdenados = misPartidos.sortedWith(
        compareBy<Partido> { partido ->
            distanciaDesdeAhora(
                fechaHoraPartido = partido.fechaHoraInicio,
                ahora = ahora
            )
        }.thenBy { partido ->
            /*
             * Si dos partidos están a la misma distancia,
             * primero aparece el próximo.
             */
            if (partido.fechaHoraInicio >= ahora) {
                0
            } else {
                1
            }
        }
    )

    val cantidadPendientes = misPartidos.count { partido ->
        partido.puedeValorarseDosHorasDespues() &&
                !valoracionViewModel.yaValoro(
                    partido.id,
                    emailUsuario
                )
    }

    var confirmarEliminarId by remember {
        mutableStateOf<String?>(null)
    }

    Scaffold(
        containerColor = ColorFondo,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(
                        horizontal = 4.dp,
                        vertical = 8.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onVolver
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = ColorTexto
                    )
                }

                Text(
                    text = "Mis partidos",
                    color = ColorTexto,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCrearPartido,
                containerColor = ColorVerde,
                contentColor = Color.Black,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Crear partido"
                )
            }
        }
    ) { padding ->

        if (misPartidos.isEmpty()) {
            EstadoSinPartidos(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(
                    horizontal = 16.dp,
                    vertical = 12.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (cantidadPendientes > 0) {
                    item {
                        AvisoValoracionesPendientes(
                            cantidad = cantidadPendientes
                        )
                    }
                }

                items(
                    items = partidosOrdenados,
                    key = { partido ->
                        partido.id.ifBlank {
                            "${partido.titulo}-${partido.fecha}-${partido.horario}"
                        }
                    }
                ) { partido ->

                    val esCreador = partido.creador
                        .trim()
                        .equals(
                            emailUsuario.trim(),
                            ignoreCase = true
                        )

                    val valoracionPendiente =
                        partido.puedeValorarseDosHorasDespues() &&
                                !valoracionViewModel.yaValoro(
                                    partido.id,
                                    emailUsuario
                                )

                    CardMiPartido(
                        partido = partido,
                        esCreador = esCreador,
                        valoracionPendiente = valoracionPendiente,
                        onAdministrar = {
                            onAdministrarPartido(partido.id)
                        },
                        onValorar = {
                            onValorarPartido(partido.id)
                        },
                        onSolicitarEliminar = {
                            confirmarEliminarId = partido.id
                        }
                    )
                }

                item {
                    Spacer(
                        modifier = Modifier.height(80.dp)
                    )
                }
            }
        }
    }

    confirmarEliminarId?.let { partidoId ->

        val partidoAEliminar = misPartidos.find { partido ->
            partido.id == partidoId
        }

        AlertDialog(
            onDismissRequest = {
                confirmarEliminarId = null
            },
            containerColor = ColorTarjeta,
            title = {
                Text(
                    text = "Eliminar partido",
                    color = ColorTexto,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (partidoAEliminar != null) {
                        "¿Seguro que querés eliminar " +
                                "\"${partidoAEliminar.titulo}\"? " +
                                "Esta acción no se puede deshacer."
                    } else {
                        "¿Seguro que querés eliminar este partido? " +
                                "Esta acción no se puede deshacer."
                    },
                    color = ColorSubtexto
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        partidoViewModel.eliminarPartido(
                            partidoId
                        )

                        confirmarEliminarId = null
                    }
                ) {
                    Text(
                        text = "Eliminar",
                        color = ColorRojo,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        confirmarEliminarId = null
                    }
                ) {
                    Text(
                        text = "Cancelar",
                        color = ColorSubtexto
                    )
                }
            }
        )
    }
}

@Composable
private fun EstadoSinPartidos(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.SportsSoccer,
                contentDescription = null,
                tint = ColorSubtexto,
                modifier = Modifier.size(64.dp)
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                text = "Todavía no participaste ni creaste partidos",
                color = ColorSubtexto,
                fontSize = 16.sp
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Tocá el + para crear uno",
                color = ColorVerde,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun AvisoValoracionesPendientes(
    cantidad: Int
) {
    Surface(
        color = ColorVerde.copy(
            alpha = 0.15f
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = if (cantidad == 1) {
                "Tenés un partido pendiente de valorar"
            } else {
                "Tenés $cantidad partidos pendientes de valorar"
            },
            color = ColorVerde,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(14.dp)
        )
    }
}

@Composable
private fun CardMiPartido(
    partido: Partido,
    esCreador: Boolean,
    valoracionPendiente: Boolean,
    onAdministrar: () -> Unit,
    onValorar: () -> Unit,
    onSolicitarEliminar: () -> Unit
) {
    val tituloPartido = partido.titulo.ifBlank {
        "Partido sin nombre"
    }

    val creadorPartido = partido.creador.ifBlank {
        "Sin información"
    }

    val esCanchaDelSistema =
        !partido.canchaId.isNullOrBlank()

    val canchaOUbicacion = if (esCanchaDelSistema) {
        partido.nombreCancha
            ?.takeIf { it.isNotBlank() }
            ?: "Cancha sin nombre"
    } else {
        partido.ubicacion.ifBlank {
            "Sin ubicación"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(14.dp)
            )
            .background(ColorTarjeta)
            .border(
                width = 1.dp,
                color = ColorBorde,
                shape = RoundedCornerShape(14.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = tituloPartido,
                    color = ColorTexto,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(
                    modifier = Modifier.width(10.dp)
                )

                EstadoPartidoChip(
                    estado = partido.estado
                )
            }

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            InformacionPartido(
                icono = Icons.Default.Person,
                titulo = "Creado por",
                contenido = creadorPartido
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            InformacionPartido(
                icono = Icons.Default.Place,
                titulo = if (esCanchaDelSistema) {
                    "Cancha del sistema"
                } else {
                    "Ubicación"
                },
                contenido = canchaOUbicacion
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoDatoCompacto(
                    icono = Icons.Default.DateRange,
                    texto = partido.fecha.ifBlank {
                        "Sin fecha"
                    },
                    modifier = Modifier.weight(1f)
                )

                InfoDatoCompacto(
                    icono = Icons.Default.Schedule,
                    texto = partido.horario.ifBlank {
                        "Sin horario"
                    },
                    modifier = Modifier.weight(1f)
                )

                InfoDatoCompacto(
                    icono = Icons.Default.Group,
                    texto = "${partido.participantesActuales}/" +
                            "${partido.participantesMaximos}",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        /*
         * Valorar:
         * puede aparecer para cualquier participante,
         * incluido el creador, cuando pasaron dos horas.
         *
         * Administrar y Eliminar:
         * solamente aparecen para el creador.
         */
        if (valoracionPendiente || esCreador) {
            HorizontalDivider(
                color = ColorBorde
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 6.dp,
                        vertical = 4.dp
                    ),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (valoracionPendiente) {
                    TextButton(
                        onClick = onValorar
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = ColorVerde,
                            modifier = Modifier.size(17.dp)
                        )

                        Spacer(
                            modifier = Modifier.width(4.dp)
                        )

                        Text(
                            text = "Valorar",
                            color = ColorVerde,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (esCreador) {
                    TextButton(
                        onClick = onAdministrar
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = ColorVerde,
                            modifier = Modifier.size(17.dp)
                        )

                        Spacer(
                            modifier = Modifier.width(4.dp)
                        )

                        Text(
                            text = "Administrar",
                            color = ColorVerde,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    TextButton(
                        onClick = onSolicitarEliminar
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = ColorRojo,
                            modifier = Modifier.size(17.dp)
                        )

                        Spacer(
                            modifier = Modifier.width(4.dp)
                        )

                        Text(
                            text = "Eliminar",
                            color = ColorRojo,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EstadoPartidoChip(
    estado: EstadoPartido
) {
    val color = colorEstadoPartido(estado)

    Surface(
        color = color.copy(
            alpha = 0.15f
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = textoEstadoPartido(estado),
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 5.dp
            )
        )
    }
}

@Composable
private fun InformacionPartido(
    icono: ImageVector,
    titulo: String,
    contenido: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = ColorVerde,
            modifier = Modifier.size(17.dp)
        )

        Spacer(
            modifier = Modifier.width(8.dp)
        )

        Column {
            Text(
                text = titulo,
                color = ColorSubtexto,
                fontSize = 10.sp
            )

            Text(
                text = contenido,
                color = ColorTexto,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun InfoDatoCompacto(
    icono: ImageVector,
    texto: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = ColorVerde,
            modifier = Modifier.size(14.dp)
        )

        Spacer(
            modifier = Modifier.width(4.dp)
        )

        Text(
            text = texto,
            color = ColorSubtexto,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/*
 * El botón Valorar aparece cuando:
 *
 * 1. El partido está publicado o aprobado.
 * 2. La fecha es válida.
 * 3. Pasaron al menos dos horas desde su inicio.
 */
private fun Partido.puedeValorarseDosHorasDespues(): Boolean {
    val estadoValido =
        estado == EstadoPartido.PUBLICADO ||
                estado == EstadoPartido.RESERVA_APROBADA

    if (!estadoValido) {
        return false
    }

    if (fechaHoraInicio <= 0L) {
        return false
    }

    val dosHorasEnMilisegundos =
        2L * 60L * 60L * 1000L

    val momentoHabilitado =
        fechaHoraInicio + dosHorasEnMilisegundos

    return System.currentTimeMillis() >= momentoHabilitado
}

private fun distanciaDesdeAhora(
    fechaHoraPartido: Long,
    ahora: Long
): Long {
    if (fechaHoraPartido <= 0L) {
        return Long.MAX_VALUE
    }

    return abs(
        fechaHoraPartido - ahora
    )
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
            ColorVerde
        }

        EstadoPartido.PENDIENTE_RESERVA -> {
            ColorAmarillo
        }

        EstadoPartido.RESERVA_APROBADA -> {
            ColorVerde
        }

        EstadoPartido.RESERVA_RECHAZADA -> {
            ColorRojo
        }
    }
}