package com.example.futbolnomade.presentation.ui.canchas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.futbolnomade.domain.model.Cancha
import com.example.futbolnomade.presentation.ui.components.AppBottomBar

private val FondoOscuro = Color(0xFF202020)
private val Verde = Color(0xFF82A820)
private val BlancoCard = Color(0xFFF8F8F8)

@Composable
fun CanchasScreen(
    canchas: List<Cancha>,
    onSubirCancha: () -> Unit,
    onVerDetalle: (Int) -> Unit
) {
    var busqueda by remember { mutableStateOf("") }
    var filtroSeleccionado by remember { mutableStateOf("Disponibilidad") }

    val filtros = listOf(
        "Disponibilidad",
        "Disponibles",
        "No disponibles"
    )

    val canchasFiltradas = canchas
        .filter { cancha ->
            busqueda.isBlank() ||
                    cancha.nombre.contains(busqueda, ignoreCase = true) ||
                    cancha.ubicacion.contains(busqueda, ignoreCase = true) ||
                    cancha.propietario.contains(busqueda, ignoreCase = true)
        }
        .filter { cancha ->
            when (filtroSeleccionado) {
                "Disponibles" -> cancha.disponible
                "No disponibles" -> !cancha.disponible
                else -> true
            }
        }

    Scaffold(
        containerColor = FondoOscuro,
        bottomBar = {
            AppBottomBar()
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Canchas",
                color = Verde,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                placeholder = { Text("Buscar cancha") },
                trailingIcon = {
                    Text(
                        text = "⌕",
                        color = Verde,
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedPlaceholderColor = Color.Gray,
                    unfocusedPlaceholderColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            FiltroDropdownCanchas(
                titulo = "Filtrar por",
                opciones = filtros,
                seleccion = filtroSeleccionado,
                onSeleccionar = { filtroSeleccionado = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (canchasFiltradas.isEmpty()) {
                Text(
                    text = "No se encontraron canchas",
                    color = Color.White
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(canchasFiltradas) { cancha ->
                        CanchaCard(
                            cancha = cancha,
                            onClick = { onVerDetalle(cancha.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSubirCancha,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Subir mi cancha",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CanchaCard(
    cancha: Cancha,
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
            // Imagen placeholder
            Surface(
                modifier = Modifier
                    .size(80.dp)
                    .padding(end = 12.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF4CAF50)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "🏟️",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cancha.nombre,
                    color = Color.Black,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "📍 ${cancha.ubicacion}",
                    color = Color.Black,
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "⭐ ${cancha.calificacion}",
                    color = Color.Black,
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (cancha.disponible) Verde else Color.Gray,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text(
                        text = if (cancha.disponible) "Ver disponibilidad" else "No disponible",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun FiltroDropdownCanchas(
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

        Box(modifier = Modifier.weight(1f)) {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
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
                onDismissRequest = { expanded = false }
            ) {
                opciones.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion) },
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