package com.example.futbolnomade.presentation.ui.canchas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.futbolnomade.domain.model.HorarioDisponible
import com.example.futbolnomade.presentation.viewModel.CanchaViewModel

private val ColorFondo    = Color(0xFF1A1A1A)
private val ColorTarjeta  = Color(0xFF242424)
private val ColorBorde    = Color(0xFF2E2E2E)
private val ColorVerde    = Color(0xFF8BC34A)
private val ColorTexto    = Color(0xFFEEEEEE)
private val ColorSubtexto = Color(0xFF999999)
private val ColorRojo     = Color(0xFFE53935)
private val ColorCampo    = Color(0xFF2A2A2A)

private val DIAS = listOf("Lunes","Martes","Miércoles","Jueves","Viernes","Sábado","Domingo")

@Composable
fun AdminCanchaScreen(
    canchaId: String,
    canchaViewModel: CanchaViewModel,       // ← ViewModel completo
    onEliminarYVolver: () -> Unit,
    onVolver: () -> Unit
) {
    // Lee reactivamente: cada vez que el ViewModel muta, Compose recompone
    val cancha = canchaViewModel.getCanchaById(canchaId)

    if (cancha == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Cancha no encontrada", color = ColorSubtexto)
        }
        return
    }

    // Estado del formulario — se reinicia si cambia la cancha (key = cancha)
    var nombre      by remember(cancha.id) { mutableStateOf(cancha.nombre) }
    var ubicacion   by remember(cancha.id) { mutableStateOf(cancha.ubicacion) }
    var descripcion by remember(cancha.id) { mutableStateOf(cancha.descripcion) }
    var precio      by remember(cancha.id) { mutableStateOf(cancha.precio.toInt().toString()) }
    var telefono    by remember(cancha.id) { mutableStateOf(cancha.telefono) }
    var disponible  by remember(cancha.id) { mutableStateOf(cancha.disponible) }

    var mostrarFormHorario by remember { mutableStateOf(false) }
    var horarioDia         by remember { mutableStateOf(DIAS[0]) }
    var horarioApertura    by remember { mutableStateOf("") }
    var horarioCierre      by remember { mutableStateOf("") }
    var errorHorario       by remember { mutableStateOf<String?>(null) }

    var confirmarEliminar  by remember { mutableStateOf(false) }
    var guardado           by remember { mutableStateOf(false) }
    var errorGuardar       by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorFondo)
            .verticalScroll(rememberScrollState())
    ) {
        // ── TopBar ────────────────────────────────────────────────────────
        Row(
            modifier          = Modifier.fillMaxWidth().statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = ColorTexto)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Administrar cancha", color = ColorTexto, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                // cancha.nombre se actualiza reactivamente al guardar
                Text(cancha.nombre, color = ColorSubtexto, fontSize = 12.sp)
            }
        }

        // ════════════════════════════════════════════════════════════════
        // SECCIÓN 1 — Datos básicos
        // ════════════════════════════════════════════════════════════════
        SeccionTitulo("Datos de la cancha", Icons.Default.Edit)

        Column(
            modifier            = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CampoTexto("Nombre",             nombre,      { nombre = it; guardado = false })
            CampoTexto("Ubicación",          ubicacion,   { ubicacion = it; guardado = false })
            CampoTexto("Descripción",        descripcion, { descripcion = it; guardado = false }, minLines = 3)
            CampoTexto("Precio por hora ($)", precio,     { precio = it; guardado = false })
            CampoTexto("Teléfono",           telefono,    { telefono = it; guardado = false })

            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ColorTarjeta)
                    .border(1.dp, ColorBorde, RoundedCornerShape(10.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Disponible para reservas", color = ColorTexto, fontSize = 14.sp)
                    Text(
                        if (disponible) "Visible para todos los usuarios"
                        else "Oculta del listado general",
                        color = ColorSubtexto, fontSize = 11.sp
                    )
                }
                Switch(
                    checked         = disponible,
                    onCheckedChange = { disponible = it; guardado = false },
                    colors          = SwitchDefaults.colors(
                        checkedThumbColor   = Color.White,
                        checkedTrackColor   = ColorVerde,
                        uncheckedTrackColor = ColorBorde
                    )
                )
            }

            errorGuardar?.let { Text(it, color = ColorRojo, fontSize = 12.sp) }
            if (guardado) Text("✓ Cambios guardados", color = ColorVerde, fontSize = 12.sp)

            Button(
                onClick = {
                    when {
                        nombre.isBlank()    -> errorGuardar = "El nombre es obligatorio"
                        ubicacion.isBlank() -> errorGuardar = "La ubicación es obligatoria"
                        telefono.isBlank()  -> errorGuardar = "El teléfono es obligatorio"
                        precio.toDoubleOrNull() == null -> errorGuardar = "El precio debe ser un número"
                        else -> {
                            errorGuardar = null
                            // Llama al ViewModel directamente — la UI reacciona sola
                            canchaViewModel.actualizarCancha(
                                canchaId, nombre.trim(), ubicacion.trim(),
                                descripcion.trim(), precio.trim(), telefono.trim(), disponible
                            )
                            guardado = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape    = RoundedCornerShape(10.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = ColorVerde)
            ) {
                Text("Guardar cambios", color = Color.Black, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(24.dp))

        // ════════════════════════════════════════════════════════════════
        // SECCIÓN 2 — Horarios
        // ════════════════════════════════════════════════════════════════
        SeccionTitulo("Horarios disponibles", Icons.Default.Schedule)

        Column(
            modifier            = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // cancha.horarios se recompone solo al agregar/eliminar
            if (cancha.horarios.isEmpty()) {
                Text(
                    "No hay franjas configuradas.\nHorario general: ${cancha.horarioApertura}–${cancha.horarioCierre}.",
                    color = ColorSubtexto, fontSize = 13.sp
                )
            } else {
                cancha.horarios.forEach { horario ->
                    FilaHorario(
                        horario    = horario,
                        onEliminar = {
                            canchaViewModel.eliminarHorario(canchaId, horario)
                        }
                    )
                }
            }

            if (mostrarFormHorario) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ColorTarjeta)
                        .border(1.dp, ColorVerde.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Nueva franja horaria", color = ColorVerde, fontWeight = FontWeight.SemiBold)

                    SelectorDia(seleccionado = horarioDia, onSeleccionar = { horarioDia = it })

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(Modifier.weight(1f)) {
                            CampoLabel("APERTURA")
                            CampoTexto("HH:MM", horarioApertura, { horarioApertura = it })
                        }
                        Column(Modifier.weight(1f)) {
                            CampoLabel("CIERRE")
                            CampoTexto("HH:MM", horarioCierre, { horarioCierre = it })
                        }
                    }

                    errorHorario?.let { Text(it, color = ColorRojo, fontSize = 12.sp) }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick  = { mostrarFormHorario = false; errorHorario = null },
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(8.dp),
                            colors   = ButtonDefaults.outlinedButtonColors(contentColor = ColorSubtexto)
                        ) { Text("Cancelar") }

                        Button(
                            onClick = {
                                when {
                                    horarioApertura.isBlank() -> errorHorario = "Ingresá la apertura"
                                    horarioCierre.isBlank()   -> errorHorario = "Ingresá el cierre"
                                    else -> {
                                        canchaViewModel.agregarHorario(
                                            canchaId,
                                            HorarioDisponible(horarioDia, horarioApertura.trim(), horarioCierre.trim())
                                        )
                                        horarioApertura    = ""
                                        horarioCierre      = ""
                                        errorHorario       = null
                                        mostrarFormHorario = false
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(8.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = ColorVerde)
                        ) { Text("Agregar", color = Color.Black) }
                    }
                }
            } else {
                OutlinedButton(
                    onClick  = { mostrarFormHorario = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(10.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = ColorVerde),
                    border   = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.dp, brush = SolidColor(ColorVerde)
                    )
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Agregar franja horaria")
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ════════════════════════════════════════════════════════════════
        // SECCIÓN 3 — Zona de peligro
        // ════════════════════════════════════════════════════════════════
        SeccionTitulo("Zona de peligro", Icons.Default.Warning, tintTitulo = ColorRojo)

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            OutlinedButton(
                onClick  = { confirmarEliminar = true },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape    = RoundedCornerShape(10.dp),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = ColorRojo),
                border   = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.dp, brush = SolidColor(ColorRojo)
                )
            ) {
                Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Eliminar esta cancha", fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(32.dp))
    }

    if (confirmarEliminar) {
        AlertDialog(
            onDismissRequest = { confirmarEliminar = false },
            containerColor   = ColorTarjeta,
            title  = { Text("Eliminar cancha", color = ColorTexto, fontWeight = FontWeight.Bold) },
            text   = {
                Text(
                    "¿Seguro que querés eliminar \"${cancha.nombre}\"? Esta acción no se puede deshacer.",
                    color = ColorSubtexto
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmarEliminar = false
                    canchaViewModel.eliminarCancha(canchaId)
                    onEliminarYVolver()             // ← navega solo después de eliminar
                }) {
                    Text("Eliminar", color = ColorRojo, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarEliminar = false }) {
                    Text("Cancelar", color = ColorSubtexto)
                }
            }
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun SeccionTitulo(titulo: String, icon: ImageVector, tintTitulo: Color = ColorVerde) {
    Row(
        modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = tintTitulo, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(titulo, color = tintTitulo, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(Modifier.width(8.dp))
        HorizontalDivider(modifier = Modifier.weight(1f), color = ColorBorde)
    }
}

@Composable
private fun CampoLabel(texto: String) {
    Text(texto, color = ColorVerde, fontSize = 10.sp, fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 2.dp))
}

@Composable
private fun CampoTexto(placeholder: String, valor: String, onChange: (String) -> Unit, minLines: Int = 1) {
    OutlinedTextField(
        value         = valor,
        onValueChange = onChange,
        placeholder   = { Text(placeholder, color = ColorSubtexto, fontSize = 13.sp) },
        minLines      = minLines,
        modifier      = Modifier.fillMaxWidth(),
        shape         = RoundedCornerShape(10.dp),
        colors        = OutlinedTextFieldDefaults.colors(
            focusedContainerColor   = ColorCampo,
            unfocusedContainerColor = ColorCampo,
            focusedBorderColor      = ColorVerde,
            unfocusedBorderColor    = ColorBorde,
            focusedTextColor        = ColorTexto,
            unfocusedTextColor      = ColorTexto,
            cursorColor             = ColorVerde
        )
    )
}

@Composable
private fun SelectorDia(seleccionado: String, onSeleccionar: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick  = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(10.dp),
            colors   = ButtonDefaults.outlinedButtonColors(contentColor = ColorTexto),
            border   = ButtonDefaults.outlinedButtonBorder.copy(
                width = 1.dp, brush = SolidColor(ColorBorde)
            )
        ) {
            Text(seleccionado, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ArrowDropDown, null, tint = ColorVerde)
        }
        DropdownMenu(
            expanded         = expanded,
            onDismissRequest = { expanded = false },
            containerColor   = ColorTarjeta
        ) {
            DIAS.forEach { dia ->
                DropdownMenuItem(
                    text    = { Text(dia, color = ColorTexto) },
                    onClick = { onSeleccionar(dia); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun FilaHorario(horario: HorarioDisponible, onEliminar: () -> Unit) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ColorTarjeta)
            .border(1.dp, ColorBorde, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Schedule, null, tint = ColorVerde, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(horario.dia, color = ColorTexto, fontSize = 13.sp, modifier = Modifier.width(90.dp))
        Text("${horario.horaApertura} – ${horario.horaCierre}",
            color = ColorSubtexto, fontSize = 13.sp, modifier = Modifier.weight(1f))
        IconButton(onClick = onEliminar, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Close, null, tint = ColorRojo, modifier = Modifier.size(16.dp))
        }
    }
}