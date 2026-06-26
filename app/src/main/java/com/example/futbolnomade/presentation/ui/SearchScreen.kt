package com.example.futbolnomade.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.futbolnomade.domain.model.Cancha
import com.example.futbolnomade.domain.model.Partido
import com.example.futbolnomade.presentation.ui.canchas.CanchaCard
import com.example.futbolnomade.presentation.ui.partidos.PartidoCard

private val ColorFondo = Color(0xFF1A1A1A)
private val ColorTarjeta = Color(0xFF242424)
private val ColorBorde = Color(0xFF2E2E2E)
private val ColorVerde = Color(0xFF8BC34A)
private val ColorTexto = Color(0xFFEEEEEE)
private val ColorSubtexto = Color(0xFF999999)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    initialQuery: String = "",
    partidos: List<Partido>,
    canchas: List<Cancha>,
    onVerDetallePartido: (String) -> Unit,
    onVerDetalleCancha: (String) -> Unit,
    onVolver: () -> Unit
) {
    var query by remember { mutableStateOf(initialQuery) }
    var tabSeleccionada by remember { mutableStateOf(0) } // 0: Partidos, 1: Canchas

    val partidosFiltrados = remember(query, partidos) {
        if (query.isBlank()) emptyList()
        else partidos.filter { 
            it.titulo.contains(query, ignoreCase = true) || 
            it.ubicacion.contains(query, ignoreCase = true) ||
            it.nombreCancha?.contains(query, ignoreCase = true) == true
        }
    }

    val canchasFiltradas = remember(query, canchas) {
        if (query.isBlank()) emptyList()
        else canchas.filter { 
            it.nombre.contains(query, ignoreCase = true) || 
            it.ubicacion.contains(query, ignoreCase = true) 
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(ColorFondo)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = ColorTexto)
                    }
                    
                    SearchBar(
                        query = query,
                        onQueryChange = { query = it },
                        modifier = Modifier.weight(1f).padding(end = 16.dp)
                    )
                }

                TabRow(
                    selectedTabIndex = tabSeleccionada,
                    containerColor = ColorFondo,
                    contentColor = ColorVerde,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[tabSeleccionada]),
                            color = ColorVerde
                        )
                    },
                    divider = { HorizontalDivider(color = ColorBorde) }
                ) {
                    Tab(
                        selected = tabSeleccionada == 0,
                        onClick = { tabSeleccionada = 0 },
                        text = { Text("Partidos (${partidosFiltrados.size})", color = if (tabSeleccionada == 0) ColorVerde else ColorSubtexto) }
                    )
                    Tab(
                        selected = tabSeleccionada == 1,
                        onClick = { tabSeleccionada = 1 },
                        text = { Text("Canchas (${canchasFiltradas.size})", color = if (tabSeleccionada == 1) ColorVerde else ColorSubtexto) }
                    )
                }
            }
        },
        containerColor = ColorFondo
    ) { padding ->
        if (query.isBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Search, null, tint = ColorSubtexto, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Buscá por nombre o ubicación", color = ColorSubtexto)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (tabSeleccionada == 0) {
                    if (partidosFiltrados.isEmpty()) {
                        item { EmptySearchMessage("No se encontraron partidos") }
                    } else {
                        items(partidosFiltrados) { partido ->
                            PartidoCard(partido = partido, onClick = { onVerDetallePartido(partido.id) })
                        }
                    }
                } else {
                    if (canchasFiltradas.isEmpty()) {
                        item { EmptySearchMessage("No se encontraron canchas") }
                    } else {
                        items(canchasFiltradas) { cancha ->
                            CanchaCard(cancha = cancha, onClick = { onVerDetalleCancha(cancha.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(ColorTarjeta)
            .border(1.dp, ColorBorde, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Search, contentDescription = null, tint = ColorVerde, modifier = Modifier.size(20.dp))
        
        Spacer(Modifier.width(8.dp))
        
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = TextStyle(color = ColorTexto, fontSize = 16.sp),
            cursorBrush = SolidColor(ColorVerde),
            decorationBox = { inner ->
                if (query.isEmpty()) {
                    Text("¿Qué estás buscando?", color = ColorSubtexto, fontSize = 14.sp)
                }
                inner()
            }
        )

        if (query.isNotEmpty()) {
            IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = ColorSubtexto)
            }
        }
    }
}

@Composable
private fun EmptySearchMessage(text: String) {
    Box(Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
        Text(text, color = ColorSubtexto, fontSize = 14.sp)
    }
}
