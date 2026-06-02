package com.example.futbolnomade.presentation.ui.partidos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.futbolnomade.domain.model.Partido
import com.example.futbolnomade.presentation.state.PartidoUiState
import com.example.futbolnomade.presentation.ui.components.AppBottomBar

private val FondoOscuro = Color(0xFF202020)
private val Verde = Color(0xFF82A820)
private val BlancoCard = Color(0xFFF8F8F8)

@Composable
fun PartidosScreen(
    uiState: PartidoUiState,
    onCrearPartido: () -> Unit,
    onVerDetalle: (Int) -> Unit,
    onVolver: () -> Unit
) {
    var busqueda by remember { mutableStateOf("") }
    var filtroSeleccionado by remember { mutableStateOf("Todos") }
    var ordenSeleccionado by remember { mutableStateOf("Más próximos") }

    val filtros = listOf(
        "Todos",
        "Con cupo",
        "Llenos",
        "Fácil",
        "Avanzado"
    )

    val ordenes = listOf(
        "Más próximos",
        "Más cupos",
        "Nombre A-Z"
    )

    val partidosFiltrados = uiState.partidos
        .filter { partido ->
            busqueda.isBlank() ||
                    partido.titulo.contains(busqueda, ignoreCase = true) ||
                    partido.ubicacion.contains(busqueda, ignoreCase = true) ||
                    partido.creador.contains(busqueda, ignoreCase = true)
        }
        .filter { partido ->
            when (filtroSeleccionado) {
                "Con cupo" -> partido.participantesActuales < partido.participantesMaximos
                "Llenos" -> partido.participantesActuales >= partido.participantesMaximos
                "Fácil" -> partido.dificultad.equals("Fácil", ignoreCase = true)
                "Avanzado" -> partido.dificultad.equals("Avanzado", ignoreCase = true)
                else -> true
            }
        }
        .let { lista ->
            when (ordenSeleccionado) {
                "Más cupos" -> lista.sortedByDescending {
                    it.participantesMaximos - it.participantesActuales
                }
                "Nombre A-Z" -> lista.sortedBy { it.titulo }
                else -> lista
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoOscuro)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Partidos",
            color = Verde,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = busqueda,
            onValueChange = { busqueda = it },
            placeholder = { Text("Buscar partido") },
            trailingIcon = {
                Text("⌕", color = Verde)
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(6.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        FiltroDropdown(
            titulo = "Filtrar por",
            opciones = filtros,
            seleccion = filtroSeleccionado,
            onSeleccionar = { filtroSeleccionado = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        FiltroDropdown(
            titulo = "Ordenar por",
            opciones = ordenes,
            seleccion = ordenSeleccionado,
            onSeleccionar = { ordenSeleccionado = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (partidosFiltrados.isEmpty()) {
            Text("No se encontraron partidos", color = Color.White)
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(partidosFiltrados) { partido ->
                    PartidoCard(
                        partido = partido,
                        onClick = { onVerDetalle(partido.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun PartidoCard(
    partido: Partido,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 18.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = BlancoCard
        ),
        border = BorderStroke(4.dp, Verde),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚽",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(end = 12.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = partido.titulo,
                    color = Color.Black,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Row {
                    Text(
                        text = "${partido.horario} hs",
                        color = Color.Black,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = partido.fecha,
                        color = Color.Black,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Text(
                    text = partido.dificultad,
                    color = Color.Black,
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "📍 ${partido.ubicacion}",
                    color = Color.Black,
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "👥 ${partido.participantesActuales}/${partido.participantesMaximos}",
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Creado por: ${partido.creador} ⭐ ${partido.calificacionCreador}",
                    color = Color.Black,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Surface(
                color = Verde,
                shape = CircleShape,
                border = BorderStroke(2.dp, Color.Black),
                modifier = Modifier.size(36.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        color = Color.Black,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun FiltroDropdown(
    titulo: String,
    opciones: List<String>,
    seleccion: String,
    onSeleccionar: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = titulo,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(95.dp)
        )

        Box(
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        expanded = true
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = seleccion,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "⌄",
                        color = Verde,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                }
            ) {
                opciones.forEach { opcion ->
                    DropdownMenuItem(
                        text = {
                            Text(opcion)
                        },
                        onClick = {
                            onSeleccionar(opcion)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}