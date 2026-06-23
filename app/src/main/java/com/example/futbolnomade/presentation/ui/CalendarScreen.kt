package com.example.futbolnomade.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.futbolnomade.domain.model.Partido
import com.example.futbolnomade.presentation.viewModel.PartidoViewModel
import java.text.SimpleDateFormat
import java.util.*

private val FondoOscuro = Color(0xFF1A1A1A)
private val ColorTarjeta = Color(0xFF242424)
private val ColorBorde = Color(0xFF2E2E2E)
private val ColorVerde = Color(0xFF8BC34A)
private val ColorTexto = Color(0xFFEEEEEE)
private val ColorSubtexto = Color(0xFF999999)

@Composable
fun CalendarScreen(
    emailUsuario: String,
    partidoViewModel: PartidoViewModel,
    onVerDetallePartido: (String) -> Unit,
    onVolver: () -> Unit
) {
    val uiState = partidoViewModel.uiState
    
    LaunchedEffect(Unit) {
        partidoViewModel.cargarPartidos()
    }
    
    // Filtrar partidos del usuario (donde es creador o está anotado)
    val misPartidos = remember(uiState.partidos, emailUsuario) {
        uiState.partidos.filter { partido ->
            partido.creador == emailUsuario || partido.usuariosAnotados.contains(emailUsuario)
        }
    }

    var fechaSeleccionada by remember { mutableStateOf(Calendar.getInstance()) }
    var mesActual by remember { mutableStateOf(Calendar.getInstance()) }

    val partidosDelDia = remember(misPartidos, fechaSeleccionada) {
        misPartidos.filter { partido ->
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            try {
                val fechaPartido = sdf.parse(partido.fecha)
                val calPartido = Calendar.getInstance().apply { time = fechaPartido ?: Date() }
                
                calPartido.get(Calendar.DAY_OF_MONTH) == fechaSeleccionada.get(Calendar.DAY_OF_MONTH) &&
                calPartido.get(Calendar.MONTH) == fechaSeleccionada.get(Calendar.MONTH) &&
                calPartido.get(Calendar.YEAR) == fechaSeleccionada.get(Calendar.YEAR)
            } catch (e: Exception) {
                false
            }
        }
    }

    Scaffold(
        containerColor = FondoOscuro,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = ColorTexto)
                }
                Text(
                    text = "Calendario",
                    color = ColorTexto,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // --- CALENDARIO ESTÉTICO ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ColorBorde, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = ColorTarjeta),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header del mes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            mesActual = (mesActual.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                        }) {
                            Icon(Icons.Default.ChevronLeft, "Mes anterior", tint = ColorVerde)
                        }

                        Text(
                            text = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
                                .format(mesActual.time)
                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                            color = ColorTexto,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        IconButton(onClick = {
                            mesActual = (mesActual.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                        }) {
                            Icon(Icons.Default.ChevronRight, "Mes siguiente", tint = ColorVerde)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Días de la semana
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom").forEach { día ->
                            Text(
                                text = día,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                color = ColorSubtexto,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Grilla de días
                    val díasEnMes = generarDíasDelMes(mesActual)
                    
                    // Calculamos cuántos partidos hay por cada día para mostrar puntos
                    val partidosPorDía = remember(misPartidos, mesActual) {
                        misPartidos.groupBy { partido ->
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            try {
                                val d = sdf.parse(partido.fecha)
                                val c = Calendar.getInstance().apply { time = d ?: Date() }
                                "${c.get(Calendar.DAY_OF_MONTH)}-${c.get(Calendar.MONTH)}-${c.get(Calendar.YEAR)}"
                            } catch (e: Exception) {
                                ""
                            }
                        }
                    }

                    Column {
                        díasEnMes.chunked(7).forEach { semana ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                semana.forEach { cal ->
                                    if (cal == null) {
                                        Spacer(Modifier.weight(1f))
                                    } else {
                                        val esSeleccionado = cal.get(Calendar.DAY_OF_MONTH) == fechaSeleccionada.get(Calendar.DAY_OF_MONTH) &&
                                                cal.get(Calendar.MONTH) == fechaSeleccionada.get(Calendar.MONTH) &&
                                                cal.get(Calendar.YEAR) == fechaSeleccionada.get(Calendar.YEAR)
                                        
                                        val esHoy = cal.get(Calendar.DAY_OF_MONTH) == Calendar.getInstance().get(Calendar.DAY_OF_MONTH) &&
                                                cal.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH) &&
                                                cal.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)

                                        val key = "${cal.get(Calendar.DAY_OF_MONTH)}-${cal.get(Calendar.MONTH)}-${cal.get(Calendar.YEAR)}"
                                        val tienePartidos = partidosPorDía.containsKey(key)

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .padding(2.dp)
                                                .clip(CircleShape)
                                                .background(if (esSeleccionado) ColorVerde else Color.Transparent)
                                                .clickable { fechaSeleccionada = cal.clone() as Calendar },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = cal.get(Calendar.DAY_OF_MONTH).toString(),
                                                    color = when {
                                                        esSeleccionado -> Color.Black
                                                        esHoy -> ColorVerde
                                                        else -> ColorTexto
                                                    },
                                                    fontWeight = if (esSeleccionado || esHoy) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 14.sp
                                                )
                                                if (tienePartidos && !esSeleccionado) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .clip(CircleShape)
                                                            .background(ColorVerde)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // --- LISTA DE PARTIDOS DEL DÍA ---
            Text(
                text = "Partidos para el ${SimpleDateFormat("dd 'de' MMMM", Locale("es", "ES")).format(fechaSeleccionada.time)}",
                color = ColorVerde,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(Modifier.height(12.dp))

            if (partidosDelDia.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No tienes partidos programados para este día", color = ColorSubtexto, textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(partidosDelDia) { partido ->
                        CardCalendarioPartido(partido, onVerDetallePartido)
                    }
                }
            }
        }
    }
}

@Composable
private fun CardCalendarioPartido(partido: Partido, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(partido.id) }
            .border(1.dp, ColorBorde, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = ColorTarjeta),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(ColorVerde.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.SportsSoccer, null, tint = ColorVerde, modifier = Modifier.size(24.dp))
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(partido.titulo, color = ColorTexto, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                    Icon(Icons.Default.Schedule, null, tint = ColorSubtexto, modifier = Modifier.size(12.dp))
                    Text(" ${partido.horario} hs", color = ColorSubtexto, fontSize = 12.sp)
                    
                    Spacer(Modifier.width(12.dp))
                    
                    Icon(Icons.Default.LocationOn, null, tint = ColorSubtexto, modifier = Modifier.size(12.dp))
                    Text(" ${partido.nombreCancha ?: partido.ubicacion.take(15)}", color = ColorSubtexto, fontSize = 12.sp, maxLines = 1)
                }
            }
            
            Icon(Icons.Default.ChevronRight, null, tint = ColorVerde)
        }
    }
}

private fun generarDíasDelMes(mesActual: Calendar): List<Calendar?> {
    val calendario = mesActual.clone() as Calendar
    calendario.set(Calendar.DAY_OF_MONTH, 1)
    
    val primerDíaSemana = (calendario.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Ajuste a lunes = 0
    val díasMes = calendario.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    val lista = mutableListOf<Calendar?>()
    
    // Relleno inicial
    repeat(primerDíaSemana) { lista.add(null) }
    
    // Días del mes
    for (i in 1..díasMes) {
        val día = calendario.clone() as Calendar
        día.set(Calendar.DAY_OF_MONTH, i)
        lista.add(día)
    }
    
    return lista
}
