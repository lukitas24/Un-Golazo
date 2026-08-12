package com.example.futbolnomade.presentation.ui.partidos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.futbolnomade.domain.model.Partido
import com.example.futbolnomade.domain.model.calcularFechaHoraInicioMillis
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val FondoEditar = Color(0xFF202020)
private val CampoEditar = Color(0xFF2E2E2E)
private val TarjetaEditar = Color(0xFF282828)
private val BordeEditar = Color(0xFF3A3A3A)
private val VerdeEditar = Color(0xFF82A820)
private val TextoEditar = Color(0xFFEEEEEE)
private val SubtextoEditar = Color(0xFFAAAAAA)
private val RojoEditar = Color(0xFFE53935)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarPartidoScreen(
    partido: Partido?,
    usuarioActualUid: String,
    usuarioActualEmail: String,
    onGuardar: (Partido) -> Unit,
    onVolver: () -> Unit
) {
    if (partido == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FondoEditar),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = VerdeEditar)
        }
        return
    }

    /*
     * Si el partido tiene creadorUid usamos UID.
     * En partidos viejos, donde creadorUid puede estar vacío,
     * usamos el email como fallback.
     */
    val esCreador = if (partido.creadorUid.isNotBlank()) {
        usuarioActualUid.isNotBlank() &&
                partido.creadorUid == usuarioActualUid
    } else {
        partido.creador.trim().equals(
            usuarioActualEmail.trim(),
            ignoreCase = true
        )
    }

    if (!esCreador) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(FondoEditar)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No podés editar este partido",
                color = TextoEditar,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Solo el organizador puede modificarlo.",
                color = SubtextoEditar
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onVolver,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdeEditar
                )
            ) {
                Text(
                    "Volver",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        return
    }

    /*
     * Si hay cancha asociada existe también una Reserva.
     *
     * Por ahora bloqueamos fecha/hora/ubicación para no dejar
     * al partido diciendo una cosa y a la reserva otra.
     */
    val usaCancha = !partido.canchaId.isNullOrBlank()

    var titulo by remember(partido.id) {
        mutableStateOf(partido.titulo)
    }

    var fecha by remember(partido.id) {
        mutableStateOf(partido.fecha)
    }

    var horario by remember(partido.id) {
        mutableStateOf(partido.horario)
    }

    var ubicacion by remember(partido.id) {
        mutableStateOf(partido.ubicacion)
    }

    var dificultad by remember(partido.id) {
        mutableStateOf(partido.dificultad)
    }

    var participantes by remember(partido.id) {
        mutableStateOf(
            partido.participantesMaximos.toString()
        )
    }

    var descripcion by remember(partido.id) {
        mutableStateOf(partido.descripcion)
    }

    var error by remember {
        mutableStateOf<String?>(null)
    }

    var mostrarDatePicker by remember {
        mutableStateOf(false)
    }

    var dropdownDificultad by remember {
        mutableStateOf(false)
    }

    val dificultades =
        listOf(
            "Fácil",
            "Medio",
            "Avanzado"
        )

    val datePickerState =
        rememberDatePickerState()

    val coloresCampos =
        OutlinedTextFieldDefaults.colors(
            focusedContainerColor = CampoEditar,
            unfocusedContainerColor = CampoEditar,
            disabledContainerColor = CampoEditar,

            focusedBorderColor = VerdeEditar,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = BordeEditar,

            focusedTextColor = TextoEditar,
            unfocusedTextColor = TextoEditar,
            disabledTextColor = SubtextoEditar,

            focusedLabelColor = VerdeEditar,
            unfocusedLabelColor = SubtextoEditar,
            disabledLabelColor = SubtextoEditar,

            cursorColor = VerdeEditar
        )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoEditar)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(
                    horizontal = 6.dp,
                    vertical = 8.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onVolver
            ) {
                Icon(
                    imageVector =
                        Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = TextoEditar
                )
            }

            Column {
                Text(
                    text = "Editar partido",
                    color = TextoEditar,
                    style =
                        MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = partido.titulo,
                    color = SubtextoEditar,
                    style =
                        MaterialTheme.typography.bodySmall
                )
            }
        }

        Column(
            modifier = Modifier.padding(
                horizontal = 20.dp,
                vertical = 12.dp
            )
        ) {

            OutlinedTextField(
                value = titulo,
                onValueChange = {
                    titulo = it
                    error = null
                },
                label = {
                    Text("Título")
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Edit,
                        null
                    )
                },
                colors = coloresCampos,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            /*
             * FECHA Y HORA
             */
            Text(
                text = "Fecha y horario",
                color = TextoEditar,
                fontWeight = FontWeight.Bold,
                style =
                    MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                OutlinedTextField(
                    value = fecha,
                    onValueChange = {},
                    readOnly = true,
                    enabled = !usaCancha,
                    label = {
                        Text("Fecha")
                    },
                    trailingIcon = {
                        if (!usaCancha) {
                            IconButton(
                                onClick = {
                                    mostrarDatePicker = true
                                }
                            ) {
                                Icon(
                                    Icons.Default.DateRange,
                                    null,
                                    tint = VerdeEditar
                                )
                            }
                        }
                    },
                    colors = coloresCampos,
                    shape = RoundedCornerShape(10.dp),
                    modifier =
                        Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = horario,
                    onValueChange = {
                        if (!usaCancha) {
                            horario = it
                            error = null
                        }
                    },
                    enabled = !usaCancha,
                    label = {
                        Text("Horario")
                    },
                    placeholder = {
                        Text("HH:MM")
                    },
                    colors = coloresCampos,
                    shape = RoundedCornerShape(10.dp),
                    modifier =
                        Modifier.weight(1f)
                )
            }

            if (usaCancha) {
                Spacer(Modifier.height(10.dp))

                Surface(
                    color =
                        VerdeEditar.copy(
                            alpha = 0.10f
                        ),
                    shape =
                        RoundedCornerShape(12.dp),
                    border =
                        BorderStroke(
                            1.dp,
                            VerdeEditar.copy(
                                alpha = 0.35f
                            )
                        )
                ) {
                    Column(
                        modifier =
                            Modifier.padding(12.dp)
                    ) {
                        Text(
                            text =
                                "Turno reservado",
                            color = VerdeEditar,
                            fontWeight =
                                FontWeight.Bold
                        )

                        Spacer(
                            Modifier.height(4.dp)
                        )

                        Text(
                            text =
                                "La fecha y el horario no se pueden modificar porque este partido tiene una reserva de cancha asociada.",
                            color = SubtextoEditar,
                            style =
                                MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            /*
             * UBICACIÓN
             */
            Text(
                text = "Ubicación",
                color = TextoEditar,
                fontWeight = FontWeight.Bold,
                style =
                    MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(10.dp))

            if (usaCancha) {
                Surface(
                    modifier =
                        Modifier.fillMaxWidth(),
                    color = TarjetaEditar,
                    shape =
                        RoundedCornerShape(12.dp),
                    border =
                        BorderStroke(
                            1.dp,
                            BordeEditar
                        )
                ) {
                    Row(
                        modifier =
                            Modifier.padding(14.dp),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Icon(
                            Icons.Default.Place,
                            null,
                            tint = VerdeEditar
                        )

                        Spacer(
                            Modifier.width(12.dp)
                        )

                        Column {
                            Text(
                                text =
                                    partido.nombreCancha
                                        ?: "Cancha",
                                color = TextoEditar,
                                fontWeight =
                                    FontWeight.Bold
                            )

                            Text(
                                text =
                                    partido.ubicacion,
                                color = SubtextoEditar,
                                style =
                                    MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = ubicacion,
                    onValueChange = {
                        ubicacion = it
                        error = null
                    },
                    label = {
                        Text(
                            "Ubicación o referencia"
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Place,
                            null
                        )
                    },
                    colors = coloresCampos,
                    shape =
                        RoundedCornerShape(10.dp),
                    modifier =
                        Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(20.dp))

            /*
             * DIFICULTAD Y CUPOS
             */
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(10.dp),
                verticalAlignment =
                    Alignment.Top
            ) {

                ExposedDropdownMenuBox(
                    expanded =
                        dropdownDificultad,
                    onExpandedChange = {
                        dropdownDificultad =
                            !dropdownDificultad
                    },
                    modifier =
                        Modifier.weight(1f)
                ) {

                    OutlinedTextField(
                        value = dificultad,
                        onValueChange = {},
                        readOnly = true,
                        label = {
                            Text("Dificultad")
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults
                                .TrailingIcon(
                                    expanded =
                                        dropdownDificultad
                                )
                        },
                        colors =
                            coloresCampos,
                        shape =
                            RoundedCornerShape(
                                10.dp
                            ),
                        modifier =
                            Modifier.menuAnchor(
                                MenuAnchorType
                                    .PrimaryNotEditable,
                                true
                            )
                    )

                    ExposedDropdownMenu(
                        expanded =
                            dropdownDificultad,
                        onDismissRequest = {
                            dropdownDificultad =
                                false
                        },
                        modifier =
                            Modifier.background(
                                CampoEditar
                            )
                    ) {

                        dificultades.forEach {
                                opcion ->

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        opcion,
                                        color =
                                            TextoEditar
                                    )
                                },
                                onClick = {
                                    dificultad =
                                        opcion

                                    dropdownDificultad =
                                        false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = participantes,
                    onValueChange = {
                        participantes =
                            it.filter {
                                    c ->
                                c.isDigit()
                            }

                        error = null
                    },
                    label = {
                        Text("Cupos")
                    },
                    colors = coloresCampos,
                    shape =
                        RoundedCornerShape(10.dp),
                    modifier =
                        Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text =
                    "Actualmente hay ${partido.participantesActuales} participante(s). No podés poner menos cupos que esa cantidad.",
                color = SubtextoEditar,
                style =
                    MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = descripcion,
                onValueChange = {
                    descripcion = it
                    error = null
                },
                label = {
                    Text("Descripción")
                },
                minLines = 3,
                colors = coloresCampos,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            error?.let {
                Spacer(Modifier.height(12.dp))

                Text(
                    text = it,
                    color = RojoEditar,
                    style =
                        MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = {

                    val maxParticipantes =
                        participantes
                            .toIntOrNull()

                    val fechaFinal =
                        if (usaCancha) {
                            partido.fecha
                        } else {
                            fecha.trim()
                        }

                    val horarioFinal =
                        if (usaCancha) {
                            partido.horario
                        } else {
                            horario.trim()
                        }

                    val ubicacionFinal =
                        if (usaCancha) {
                            partido.ubicacion
                        } else {
                            ubicacion.trim()
                        }

                    val formatter =
                        SimpleDateFormat(
                            "dd/MM/yyyy",
                            Locale.getDefault()
                        ).apply {
                            isLenient = false
                        }

                    val fechaDate =
                        try {
                            formatter.parse(
                                fechaFinal
                            )
                        } catch (_: Exception) {
                            null
                        }

                    val hoy =
                        Calendar
                            .getInstance()
                            .apply {
                                set(
                                    Calendar.HOUR_OF_DAY,
                                    0
                                )
                                set(
                                    Calendar.MINUTE,
                                    0
                                )
                                set(
                                    Calendar.SECOND,
                                    0
                                )
                                set(
                                    Calendar.MILLISECOND,
                                    0
                                )
                            }
                            .time

                    when {

                        titulo.isBlank() -> {
                            error =
                                "El título es obligatorio"
                        }

                        maxParticipantes == null ||
                                maxParticipantes <= 0 -> {

                            error =
                                "La cantidad de cupos no es válida"
                        }

                        maxParticipantes <
                                partido.participantesActuales -> {

                            error =
                                "No podés poner menos de ${partido.participantesActuales} cupos porque ya hay jugadores anotados"
                        }

                        !usaCancha &&
                                fechaDate == null -> {

                            error =
                                "La fecha no es válida"
                        }

                        !usaCancha &&
                                fechaDate?.before(hoy) == true -> {

                            error =
                                "No podés seleccionar una fecha pasada"
                        }

                        horarioFinal.isBlank() -> {
                            error =
                                "El horario es obligatorio"
                        }

                        ubicacionFinal.isBlank() -> {
                            error =
                                "La ubicación es obligatoria"
                        }
                        else -> {

                            error = null

                            val cambioUbicacion =
                                !ubicacionFinal.equals(
                                    partido.ubicacion,
                                    ignoreCase = true
                                )

                            val actualizado =
                                partido.copy(
                                    titulo = titulo.trim(),

                                    fecha = fechaFinal,

                                    horario = horarioFinal,

                                    fechaHoraInicio =
                                        calcularFechaHoraInicioMillis(
                                            fecha = fechaFinal,
                                            horario = horarioFinal
                                        ),

                                    ubicacion = ubicacionFinal,

                                    dificultad = dificultad,

                                    participantesMaximos =
                                        maxParticipantes,

                                    descripcion =
                                        descripcion.trim(),

                                    latitud =
                                        if (
                                            !usaCancha &&
                                            cambioUbicacion
                                        ) {
                                            null
                                        } else {
                                            partido.latitud
                                        },

                                    longitud =
                                        if (
                                            !usaCancha &&
                                            cambioUbicacion
                                        ) {
                                            null
                                        } else {
                                            partido.longitud
                                        }
                                )

                            onGuardar(actualizado)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape =
                    RoundedCornerShape(10.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            VerdeEditar
                    )
            ) {

                Text(
                    text =
                        "Guardar cambios",
                    color = Color.Black,
                    fontWeight =
                        FontWeight.Bold
                )
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = onVolver,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape =
                    RoundedCornerShape(10.dp),
                border =
                    BorderStroke(
                        1.dp,
                        Color.Gray
                    )
            ) {
                Text(
                    text = "Cancelar",
                    color = TextoEditar
                )
            }

            Spacer(Modifier.height(30.dp))
        }
    }

    if (mostrarDatePicker) {

        DatePickerDialog(
            onDismissRequest = {
                mostrarDatePicker = false
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        datePickerState
                            .selectedDateMillis
                            ?.let {
                                    millis ->

                                val formatter =
                                    SimpleDateFormat(
                                        "dd/MM/yyyy",
                                        Locale.getDefault()
                                    )

                                fecha =
                                    formatter.format(
                                        Date(millis)
                                    )
                            }

                        mostrarDatePicker =
                            false
                    }
                ) {
                    Text(
                        "Aceptar",
                        color = VerdeEditar
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        mostrarDatePicker =
                            false
                    }
                ) {
                    Text(
                        "Cancelar",
                        color =
                            SubtextoEditar
                    )
                }
            }
        ) {
            DatePicker(
                state = datePickerState
            )
        }
    }
}