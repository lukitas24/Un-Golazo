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
import com.example.futbolnomade.presentation.viewModel.ValoracionViewModel

private val ColorFondo       = Color(0xFF1A1A1A)
private val ColorTarjeta     = Color(0xFF242424)
private val ColorBorde       = Color(0xFF2E2E2E)
private val ColorVerde       = Color(0xFF8BC34A)
private val ColorVerdeOscuro = Color(0xFF6A9E2F)
private val ColorTexto       = Color(0xFFEEEEEE)
private val ColorSubtexto    = Color(0xFF999999)
private val ColorOro         = Color(0xFFFFD700)

private data class GridItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun HomeScreen(
    nombreUsuario: String,
    emailUsuario: String = "",
    onIrAPartidos: () -> Unit,
    onIrACanchas: () -> Unit,
    onIrAMisPartidos: () -> Unit,
    onIrAMisCanchas: () -> Unit,
    onIrACercaMio: () -> Unit,
    onBuscarPartido: (String) -> Unit,
    onVerDetallePartido: (String) -> Unit,
    onIrAElementos: () -> Unit = {},
    onIrAAcerca: () -> Unit = {},
    homeViewModel: HomeViewModel = viewModel(),
    valoracionViewModel: ValoracionViewModel = viewModel()
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    var mostrarPopupValoracion by remember { mutableStateOf(false) }

    LaunchedEffect(nombreUsuario, emailUsuario) {
        homeViewModel.inicializar(nombreUsuario, emailUsuario)
        if (emailUsuario.isNotBlank()) {
            valoracionViewModel.cargarValoracionesUsuario(emailUsuario)
        }
    }

    val gridItems = listOf(
        GridItem("Canchas",      Icons.Default.Place,      onIrACanchas),
        GridItem("Partidos",     Icons.Default.Home,       onIrAPartidos),
        GridItem("Cerca mío",    Icons.Default.LocationOn, onIrACercaMio),
        GridItem("Mi valoración", Icons.Default.Star,      { mostrarPopupValoracion = true }),
        GridItem("Mis partidos", Icons.Default.Person,     onIrAMisPartidos),
        GridItem("Mis canchas",  Icons.Default.Favorite,   onIrAMisCanchas)
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ColorFondo
    ) {
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
                    email = emailUsuario.ifBlank { uiState.emailUsuario }
                )
            }

            item {
                BuscadorPartido(onBuscar = onBuscarPartido)
            }

            item {
                GrillaAccesos(items = gridItems)
            }

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
                CardPartido(
                    partido = partido,
                    onClick = {
                        onVerDetallePartido(partido.id)
                    }
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    if (mostrarPopupValoracion) {
        val promedioOrg = valoracionViewModel.obtenerPromedioOrganizador(emailUsuario)
        val cantidadOrg = valoracionViewModel.obtenerCantidadValoracionesOrganizador(emailUsuario)
        
        val promedioJug = valoracionViewModel.obtenerPromedioJugador(emailUsuario)
        val cantidadJug = valoracionViewModel.obtenerCantidadValoracionesJugador(emailUsuario)

        AlertDialog(
            onDismissRequest = { mostrarPopupValoracion = false },
            containerColor = ColorTarjeta,
            title = {
                Text(
                    text = "Mi Valoración",
                    color = ColorTexto,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    SeccionValoracionPopup(
                        titulo = "Como Organizador",
                        promedio = promedioOrg,
                        cantidad = cantidadOrg
                    )
                    
                    HorizontalDivider(color = ColorBorde)
                    
                    SeccionValoracionPopup(
                        titulo = "Como Jugador",
                        promedio = promedioJug,
                        cantidad = cantidadJug
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarPopupValoracion = false }) {
                    Text("Cerrar", color = ColorVerde)
                }
            }
        )
    }
}

@Composable
private fun SeccionValoracionPopup(
    titulo: String,
    promedio: Double,
    cantidad: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = titulo,
            color = ColorSubtexto,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(Modifier.height(8.dp))
        
        Text(
            text = String.format(java.util.Locale.getDefault(), "%.1f", promedio),
            color = ColorVerde,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
        )
        
        Row(modifier = Modifier.padding(vertical = 4.dp)) {
            repeat(5) { index ->
                val starColor = if (index < promedio.toInt()) Color(0xFFFFD700) else Color(0xFF2E2E2E)
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = starColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Text(
            text = "$cantidad valoraciones",
            color = ColorSubtexto,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun HeaderUsuario(
    nombre: String,
    email: String
) {
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
            Text(
                text = "Bienvenido",
                color = ColorTexto,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = email.ifBlank { nombre },
                color = ColorSubtexto,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun BuscadorPartido(
    onBuscar: (String) -> Unit
) {
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
            onValueChange = {
                texto = it
            },
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = TextStyle(
                color = ColorTexto,
                fontSize = 14.sp
            ),
            cursorBrush = SolidColor(ColorVerde),
            decorationBox = { inner ->
                if (texto.isEmpty()) {
                    Text(
                        text = "Buscar partido",
                        color = ColorSubtexto,
                        fontSize = 14.sp
                    )
                }

                inner()
            }
        )

        IconButton(
            onClick = {
                onBuscar(texto)
            },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = "Buscar",
                tint = ColorVerde
            )
        }
    }
}

@Composable
private fun GrillaAccesos(
    items: List<GridItem>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.chunked(3).forEach { fila ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                fila.forEach { item ->
                    ItemGrilla(
                        item = item,
                        modifier = Modifier.weight(1f)
                    )
                }

                repeat(3 - fila.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ItemGrilla(
    item: GridItem,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ColorTarjeta)
            .border(1.dp, ColorBorde, RoundedCornerShape(12.dp))
            .clickable {
                item.onClick()
            }
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

        Text(
            text = item.label,
            color = ColorTexto,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CardPartido(
    partido: PartidoResumen,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ColorTarjeta)
            .border(1.dp, ColorBorde, RoundedCornerShape(12.dp))
            .clickable {
                onClick()
            }
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

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = partido.titulo,
                color = ColorTexto,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = "Anfitrión: ${partido.anfitrion}",
                color = ColorSubtexto,
                fontSize = 12.sp
            )

            Spacer(Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = ColorVerde,
                    modifier = Modifier.size(14.dp)
                )

                Text(
                    text = " ${partido.rating}",
                    color = ColorSubtexto,
                    fontSize = 12.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Top)
                .clip(RoundedCornerShape(6.dp))
                .background(ColorVerde)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = partido.fecha,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}