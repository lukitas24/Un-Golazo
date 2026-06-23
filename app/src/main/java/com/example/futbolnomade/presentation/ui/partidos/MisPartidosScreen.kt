package com.example.futbolnomade.presentation.ui.partidos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsSoccer
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.futbolnomade.domain.model.EstadoPartido
import com.example.futbolnomade.domain.model.Partido
import com.example.futbolnomade.presentation.viewModel.PartidoViewModel
import com.example.futbolnomade.domain.model.puedeValorarse
import com.example.futbolnomade.presentation.viewModel.ValoracionViewModel
private val ColorFondo    = Color(0xFF1A1A1A)
private val ColorTarjeta  = Color(0xFF242424)
private val ColorBorde    = Color(0xFF2E2E2E)
private val ColorVerde    = Color(0xFF8BC34A)
private val ColorTexto    = Color(0xFFEEEEEE)
private val ColorSubtexto = Color(0xFF999999)
private val ColorRojo     = Color(0xFFE53935)

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
    val misPartidos =
        partidoViewModel
            .partidosDelUsuario(emailUsuario)

    val cantidadPendientes =
        misPartidos.count { partido ->
            partido.puedeValorarse() &&
                    !valoracionViewModel.yaValoro(
                        partido.id,
                        emailUsuario
                    )
        }

    val partidosOrdenados =
        misPartidos.sortedByDescending { partido ->
            partido.puedeValorarse() &&
                    !valoracionViewModel.yaValoro(
                        partido.id,
                        emailUsuario
                    )
        }
    var confirmarEliminarId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = ColorFondo,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVolver) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
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
                    Icons.Default.Add,
                    contentDescription = "Crear partido"
                )
            }
        }
    ) { padding ->

        if (misPartidos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.SportsSoccer,
                        contentDescription = null,
                        tint = ColorSubtexto,
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Todavía no creaste partidos",
                        color = ColorSubtexto,
                        fontSize = 16.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Tocá el + para crear uno",
                        color = ColorVerde,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (cantidadPendientes > 0) {
                    item {
                        Surface(
                            color = ColorVerde.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text =
                                    "Tenés $cantidadPendientes partido(s) pendiente(s) de valorar",
                                color = ColorVerde,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }
                items(
                        partidosOrdenados,
                key = { it.id }
                ) { partido ->

                val valoracionPendiente =
                    partido.puedeValorarse() &&
                            !valoracionViewModel.yaValoro(
                                partido.id,
                                emailUsuario
                            )

                CardMiPartido(
                    partido = partido,
                    esCreador =
                        partido.creador == emailUsuario,
                    valoracionPendiente =
                        valoracionPendiente,

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
                    Spacer(Modifier.height(80.dp))
                }
            }
        }
    }

    confirmarEliminarId?.let { id ->
        val nombre = misPartidos.find { partido ->
            partido.id == id
        }?.titulo ?: ""

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
                    text = "¿Seguro que querés eliminar \"$nombre\"? Esta acción no se puede deshacer.",
                    color = ColorSubtexto
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        partidoViewModel.eliminarPartido(id)
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
private fun CardMiPartido(
    partido: Partido,
    esCreador: Boolean,
    valoracionPendiente: Boolean,
    onAdministrar: () -> Unit,
    onValorar: () -> Unit,
    onSolicitarEliminar: () -> Unit
) {
    val cupoCompleto = partido.participantesActuales >= partido.participantesMaximos

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ColorTarjeta)
            .border(1.dp, ColorBorde, RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            if (esCreador) {
                TextButton(
                    onClick = onSolicitarEliminar
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = ColorRojo,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(Modifier.width(4.dp))

                    Text(
                        text = "Eliminar",
                        color = ColorRojo,
                        fontSize = 13.sp
                    )
                }
            } else {
                Spacer(Modifier.width(1.dp))
            }

            Row {
                if (valoracionPendiente) {
                    TextButton(onClick = onValorar) {
                        Text(
                            text = "Valorar",
                            color = ColorVerde,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                TextButton(onClick = onAdministrar) {
                    Text(
                        text = if (esCreador) {
                            "Administrar"
                        } else {
                            "Ver detalle"
                        },
                        color = ColorVerde,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.width(2.dp))

                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = ColorVerde,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        HorizontalDivider(color = ColorBorde)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoChip(Icons.Default.DateRange, partido.fecha)

            InfoChip(Icons.Default.Schedule, partido.horario)

            InfoChip(
                Icons.Default.Group,
                "${partido.participantesActuales}/${partido.participantesMaximos}"
            )
        }

        HorizontalDivider(color = ColorBorde)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onSolicitarEliminar) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = ColorRojo,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(Modifier.width(4.dp))

                Text(
                    text = "Eliminar",
                    color = ColorRojo,
                    fontSize = 13.sp
                )
            }

            TextButton(onClick = onAdministrar) {
                Text(
                    text = "Administrar",
                    color = ColorVerde,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.width(2.dp))

                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = ColorVerde,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun InfoChip(
    icon: ImageVector,
    texto: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = ColorVerde,
            modifier = Modifier.size(13.dp)
        )

        Spacer(Modifier.width(3.dp))

        Text(
            text = texto,
            color = ColorSubtexto,
            fontSize = 11.sp
        )
    }
}

private fun textoEstadoPartido(estado: EstadoPartido): String {
    return when (estado) {
        EstadoPartido.PUBLICADO -> "Activo"
        EstadoPartido.PENDIENTE_RESERVA -> "Pendiente"
        EstadoPartido.RESERVA_APROBADA -> "Aprobado"
        EstadoPartido.RESERVA_RECHAZADA -> "Rechazado"
    }
}

private fun colorEstadoPartido(estado: EstadoPartido): Color {
    return when (estado) {
        EstadoPartido.PUBLICADO -> ColorVerde
        EstadoPartido.PENDIENTE_RESERVA -> Color(0xFFFFC107)
        EstadoPartido.RESERVA_APROBADA -> ColorVerde
        EstadoPartido.RESERVA_RECHAZADA -> ColorRojo
    }
}
