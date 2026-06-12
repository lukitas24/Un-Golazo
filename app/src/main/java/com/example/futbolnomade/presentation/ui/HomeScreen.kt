package com.example.futbolnomade.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.futbolnomade.presentation.state.PartidoResumen
import com.example.futbolnomade.presentation.viewModel.HomeViewModel

// ── Paleta ──────────────────────────────────────────────────────────────────
private val ColorFondo       = Color(0xFF1A1A1A)
private val ColorTarjeta     = Color(0xFF242424)
private val ColorBorde       = Color(0xFF2E2E2E)
private val ColorVerde       = Color(0xFF8BC34A)
private val ColorVerdeOscuro = Color(0xFF6A9E2F)
private val ColorTexto       = Color(0xFFEEEEEE)
private val ColorSubtexto    = Color(0xFF999999)

// ── Modelo de ítem de grilla ─────────────────────────────────────────────────
private data class GridItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

// ── Pantalla principal ───────────────────────────────────────────────────────
@Composable
fun HomeScreen(
    nombreUsuario: String,
    emailUsuario: String = "",
    onIrAPartidos: () -> Unit,
    onIrACanchas: () -> Unit,
    onIrAMisPartidos: () -> Unit,
    onIrAMisCanchas: () -> Unit,
    onIrAMisReservas: () -> Unit,
    onIrACercaMio: () -> Unit,
    onBuscarPartido: (String) -> Unit,
    onVerDetallePartido: (String) -> Unit,
    onIrAElementos: () -> Unit = {},
    onIrAAcerca: () -> Unit = {},
    homeViewModel: HomeViewModel = viewModel()
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(nombreUsuario, emailUsuario) {
        homeViewModel.inicializar(nombreUsuario, emailUsuario)
    }

    // Icons.Default.* que existen en material-icons-core (sin extended)
    val gridItems = listOf(
        GridItem("Canchas",      Icons.Default.Place,       onIrACanchas),
        GridItem("Partidos",     Icons.Default.Home,        onIrAPartidos),
        GridItem("Cerca mío",    Icons.Default.LocationOn,  onIrACercaMio),
        GridItem("Mis reservas", Icons.Default.DateRange,   onIrAMisReservas),
        GridItem("Mis partidos", Icons.Default.Person,      onIrAMisPartidos),
        GridItem("Mis canchas",  Icons.Default.Favorite,    onIrAMisCanchas)
    )

    Surface(modifier = Modifier.fillMaxSize(), color = ColorFondo) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                HeaderUsuario(
                    nombre = nombreUsuario.ifBlank { uiState.nombreUsuario },
                    email  = emailUsuario.ifBlank  { uiState.emailUsuario }
                )
            }

            item { BuscadorPartido(onBuscar = onBuscarPartido) }

            item { GrillaAccesos(items = gridItems) }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = ColorVerde,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Próximos eventos",
                        color = ColorVerde,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            items(uiState.proximosPartidos) { partido ->
                CardPartido(partido = partido, onClick = { onVerDetallePartido(partido.id) })
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

// ── Header ───────────────────────────────────────────────────────────────────
@Composable
private fun HeaderUsuario(nombre: String, email: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(ColorVerde),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = nombre.firstOrNull()?.uppercase() ?: "U",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text("Bienvenido", color = ColorTexto, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(email.ifBlank { nombre }, color = ColorSubtexto, fontSize = 13.sp)
        }
    }
}

// ── Buscador ─────────────────────────────────────────────────────────────────
@Composable
private fun BuscadorPartido(onBuscar: (String) -> Unit) {
    var texto by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ColorTarjeta)
            .border(1.dp, ColorBorde, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = texto,
            onValueChange = { texto = it },
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = TextStyle(color = ColorTexto, fontSize = 14.sp),
            cursorBrush = SolidColor(ColorVerde),
            decorationBox = { inner ->
                if (texto.isEmpty()) Text("Buscar partido", color = ColorSubtexto, fontSize = 14.sp)
                inner()
            }
        )
        IconButton(onClick = { onBuscar(texto) }, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Search, contentDescription = "Buscar", tint = ColorVerde)
        }
    }
}

// ── Grilla 3×2 ───────────────────────────────────────────────────────────────
@Composable
private fun GrillaAccesos(items: List<GridItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.chunked(3).forEach { fila ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                fila.forEach { item ->
                    ItemGrilla(item = item, modifier = Modifier.weight(1f))
                }
                repeat(3 - fila.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun ItemGrilla(item: GridItem, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ColorTarjeta)
            .border(1.dp, ColorBorde, RoundedCornerShape(12.dp))
            .clickable { item.onClick() }
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = ColorVerde,
            modifier = Modifier.size(32.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(item.label, color = ColorTexto, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

// ── Card de partido próximo ───────────────────────────────────────────────────
@Composable
private fun CardPartido(partido: PartidoResumen, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ColorTarjeta)
            .border(1.dp, ColorBorde, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(ColorVerdeOscuro),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(partido.titulo, color = ColorTexto, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text("Anfitrión: ${partido.anfitrion}", color = ColorSubtexto, fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = ColorVerde, modifier = Modifier.size(14.dp))
                Text(" ${partido.rating}", color = ColorSubtexto, fontSize = 12.sp)
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Top)
                .clip(RoundedCornerShape(6.dp))
                .background(ColorVerde)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(partido.fecha, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}