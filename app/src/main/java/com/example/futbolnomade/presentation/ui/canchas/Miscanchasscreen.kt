package com.example.futbolnomade.presentation.ui.canchas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.futbolnomade.domain.model.Cancha
import com.example.futbolnomade.presentation.viewModel.CanchaViewModel

private val ColorFondo    = Color(0xFF1A1A1A)
private val ColorTarjeta  = Color(0xFF242424)
private val ColorBorde    = Color(0xFF2E2E2E)
private val ColorVerde    = Color(0xFF8BC34A)
private val ColorTexto    = Color(0xFFEEEEEE)
private val ColorSubtexto = Color(0xFF999999)
private val ColorRojo     = Color(0xFFE53935)

@Composable
fun MisCanchasScreen(
    emailUsuario: String,
    canchaViewModel: CanchaViewModel,          // ← ViewModel completo, no lista
    onCrearCancha: () -> Unit,
    onAdministrarCancha: (Int) -> Unit,
    onVolver: () -> Unit
) {
    // Lee reactivamente del ViewModel — se recompone cada vez que uiState cambia
    val misCanchas = canchaViewModel.misCanchas(emailUsuario)

    var confirmarEliminarId by remember { mutableStateOf<Int?>(null) }

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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = ColorTexto)
                }
                Text("Mis canchas", color = ColorTexto, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = onCrearCancha,
                containerColor = ColorVerde,
                contentColor   = Color.Black,
                shape          = CircleShape
            ) {
                Icon(Icons.Default.Add, "Agregar cancha")
            }
        }
    ) { padding ->

        if (misCanchas.isEmpty()) {
            Box(
                modifier         = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Place, null, tint = ColorSubtexto, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Todavía no tenés canchas", color = ColorSubtexto, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Tocá el + para agregar una", color = ColorVerde, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier            = Modifier.fillMaxSize().padding(padding),
                contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(misCanchas, key = { it.id }) { cancha ->
                    CardMiCancha(
                        cancha              = cancha,
                        onAdministrar       = { onAdministrarCancha(cancha.id) },
                        onSolicitarEliminar = { confirmarEliminarId = cancha.id }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    confirmarEliminarId?.let { id ->
        val nombre = misCanchas.find { it.id == id }?.nombre ?: ""
        AlertDialog(
            onDismissRequest = { confirmarEliminarId = null },
            containerColor   = ColorTarjeta,
            title  = { Text("Eliminar cancha", color = ColorTexto, fontWeight = FontWeight.Bold) },
            text   = {
                Text(
                    "¿Seguro que querés eliminar \"$nombre\"? Esta acción no se puede deshacer.",
                    color = ColorSubtexto
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    canchaViewModel.eliminarCancha(id)  // ← llama directo al ViewModel
                    confirmarEliminarId = null
                }) {
                    Text("Eliminar", color = ColorRojo, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarEliminarId = null }) {
                    Text("Cancelar", color = ColorSubtexto)
                }
            }
        )
    }
}

@Composable
private fun CardMiCancha(
    cancha: Cancha,
    onAdministrar: () -> Unit,
    onSolicitarEliminar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ColorTarjeta)
            .border(1.dp, ColorBorde, RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier         = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ColorVerde.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Place, null, tint = ColorVerde, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(cancha.nombre, color = ColorTexto, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = ColorSubtexto, modifier = Modifier.size(11.dp))
                    Spacer(Modifier.width(2.dp))
                    Text(cancha.ubicacion, color = ColorSubtexto, fontSize = 12.sp)
                }
            }
            Surface(
                color = if (cancha.disponible) ColorVerde.copy(alpha = 0.2f) else ColorRojo.copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text       = if (cancha.disponible) "Activa" else "Inactiva",
                    color      = if (cancha.disponible) ColorVerde else ColorRojo,
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        HorizontalDivider(color = ColorBorde)

        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoChip(Icons.Default.AttachMoney, "$${cancha.precio.toInt()}/h")
            InfoChip(Icons.Default.Phone, cancha.telefono.ifBlank { "—" })
            InfoChip(
                icon  = Icons.Default.Schedule,
                texto = if (cancha.horarios.isEmpty()) "${cancha.horarioApertura}–${cancha.horarioCierre}"
                else "${cancha.horarios.size} franjas"
            )
        }

        HorizontalDivider(color = ColorBorde)

        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            TextButton(onClick = onSolicitarEliminar) {
                Icon(Icons.Default.Delete, null, tint = ColorRojo, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Eliminar", color = ColorRojo, fontSize = 13.sp)
            }
            TextButton(onClick = onAdministrar) {
                Text("Administrar", color = ColorVerde, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(2.dp))
                Icon(Icons.Default.ChevronRight, null, tint = ColorVerde, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun InfoChip(icon: ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = ColorVerde, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(3.dp))
        Text(texto, color = ColorSubtexto, fontSize = 11.sp)
    }
}