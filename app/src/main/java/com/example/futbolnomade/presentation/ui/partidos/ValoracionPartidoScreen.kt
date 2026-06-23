package com.example.futbolnomade.presentation.ui.partidos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.futbolnomade.domain.model.Partido
import com.example.futbolnomade.domain.model.ValoracionJugador
import com.example.futbolnomade.domain.model.ValoracionPartido

private val FondoValoracion = Color(0xFF202020)
private val TarjetaValoracion = Color(0xFF2E2E2E)
private val VerdeValoracion = Color(0xFF82A820)

@Composable
fun ValorarPartidoScreen(
    partido: Partido?,
    usuarioActual: String,
    guardando: Boolean,
    error: String?,
    onGuardar: (ValoracionPartido) -> Unit,
    onVolver: () -> Unit
) {
    if (partido == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FondoValoracion),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No se encontró el partido",
                color = Color.White
            )
        }

        return
    }

    val jugadoresValorables = remember(
        partido.id,
        usuarioActual
    ) {
        partido.usuariosAnotados.filter {
            it != usuarioActual
        }
    }

    var puntuacionGrupo by remember {
        mutableIntStateOf(0)
    }

    var mostrarJugadores by remember {
        mutableStateOf(false)
    }

    var puntuacionesJugadores by remember(
        partido.id,
        usuarioActual
    ) {
        mutableStateOf(
            jugadoresValorables.associateWith { 0 }
        )
    }

    val puedeValorarOrganizador =
        partido.creador != usuarioActual

    var puntuacionOrganizador by remember {
        mutableIntStateOf(0)
    }

    val puedeValorarCancha =
        partido.canchaId != null

    var puntuacionCancha by remember {
        mutableIntStateOf(0)
    }

    val jugadoresCompletos =
        puntuacionesJugadores.values.all {
            it in 1..5
        }

    val organizadorCompleto =
        !puedeValorarOrganizador ||
                puntuacionOrganizador in 1..5

    val canchaCompleta =
        !puedeValorarCancha ||
                puntuacionCancha in 1..5

    val hayAlgoParaValorar =
        jugadoresValorables.isNotEmpty() ||
                puedeValorarOrganizador ||
                puedeValorarCancha

    val puedeGuardar =
        hayAlgoParaValorar &&
                jugadoresCompletos &&
                organizadorCompleto &&
                canchaCompleta &&
                !guardando

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoValoracion)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Valorar partido",
            color = VerdeValoracion,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = partido.titulo,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Tu valoración es privada y ayuda a mejorar la comunidad.",
            color = Color.LightGray,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(24.dp))

        if (jugadoresValorables.isNotEmpty()) {
            SeccionValoracion(
                titulo = "¿Cómo estuvo el grupo?",
                descripcion =
                    "Esta puntuación se aplicará inicialmente a todos los jugadores."
            ) {
                SelectorEstrellas(
                    valor = puntuacionGrupo,
                    onCambiar = { puntuacion ->
                        puntuacionGrupo = puntuacion

                        puntuacionesJugadores =
                            jugadoresValorables.associateWith {
                                puntuacion
                            }
                    }
                )

                TextButton(
                    onClick = {
                        mostrarJugadores =
                            !mostrarJugadores
                    }
                ) {
                    Text(
                        text = if (mostrarJugadores) {
                            "Ocultar valoración individual"
                        } else {
                            "Personalizar jugadores"
                        },
                        color = VerdeValoracion
                    )
                }

                if (mostrarJugadores) {
                    jugadoresValorables.forEach { jugador ->
                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = jugador,
                                    color = Color.White,
                                    style =
                                        MaterialTheme.typography.bodyMedium
                                )

                                if (
                                    jugador ==
                                    partido.creador
                                ) {
                                    Text(
                                        text = "Organizador",
                                        color = VerdeValoracion,
                                        style =
                                            MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            SelectorEstrellas(
                                valor =
                                    puntuacionesJugadores[
                                        jugador
                                    ] ?: 0,
                                compacto = true,
                                onCambiar = { puntuacion ->
                                    puntuacionesJugadores =
                                        puntuacionesJugadores +
                                                (
                                                        jugador to
                                                                puntuacion
                                                        )
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        if (puedeValorarOrganizador) {
            SeccionValoracion(
                titulo = "Organización",
                descripcion =
                    "¿Cómo estuvo la organización de ${partido.creador}?"
            ) {
                SelectorEstrellas(
                    valor = puntuacionOrganizador,
                    onCambiar = {
                        puntuacionOrganizador = it
                    }
                )
            }

            Spacer(Modifier.height(16.dp))
        }

        if (puedeValorarCancha) {
            SeccionValoracion(
                titulo = "Cancha",
                descripcion =
                    "¿Cómo estuvo ${partido.nombreCancha ?: "la cancha"}?"
            ) {
                SelectorEstrellas(
                    valor = puntuacionCancha,
                    onCambiar = {
                        puntuacionCancha = it
                    }
                )
            }

            Spacer(Modifier.height(16.dp))
        }

        if (!hayAlgoParaValorar) {
            Text(
                text =
                    "No hay jugadores, organizador ni cancha para valorar.",
                color = Color.LightGray
            )
        }

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(
                    vertical = 12.dp
                )
            )
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                val valoracion =
                    ValoracionPartido(
                        partidoId = partido.id,
                        autorEmail = usuarioActual,

                        valoracionesJugadores =
                            puntuacionesJugadores.map {
                                    (email, puntuacion) ->

                                ValoracionJugador(
                                    jugadorEmail = email,
                                    puntuacion = puntuacion
                                )
                            },

                        organizadorEmail =
                            partido.creador.takeIf {
                                puedeValorarOrganizador
                            },

                        puntuacionOrganizador =
                            if (
                                puedeValorarOrganizador
                            ) {
                                puntuacionOrganizador
                            } else {
                                0
                            },

                        canchaId = partido.canchaId,

                        puntuacionCancha =
                            if (puedeValorarCancha) {
                                puntuacionCancha
                            } else {
                                0
                            },

                        fechaCreacion =
                            System.currentTimeMillis()
                    )

                onGuardar(valoracion)
            },
            enabled = puedeGuardar,
            colors = ButtonDefaults.buttonColors(
                containerColor = VerdeValoracion,
                disabledContainerColor = Color.DarkGray
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (guardando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = Color.Black
                )
            } else {
                Text(
                    text = "Guardar valoraciones",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "Ahora no",
                color = Color.White
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SeccionValoracion(
    titulo: String,
    descripcion: String,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                TarjetaValoracion,
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = titulo,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = descripcion,
            color = Color.LightGray,
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(Modifier.height(12.dp))

        contenido()
    }
}

@Composable
private fun SelectorEstrellas(
    valor: Int,
    compacto: Boolean = false,
    onCambiar: (Int) -> Unit
) {
    Row {
        (1..5).forEach { puntuacion ->
            IconButton(
                onClick = {
                    onCambiar(puntuacion)
                },
                modifier = Modifier.size(
                    if (compacto) 34.dp else 44.dp
                )
            ) {
                Icon(
                    imageVector =
                        if (puntuacion <= valor) {
                            Icons.Default.Star
                        } else {
                            Icons.Default.StarBorder
                        },
                    contentDescription =
                        "$puntuacion estrellas",
                    tint =
                        if (puntuacion <= valor) {
                            VerdeValoracion
                        } else {
                            Color.Gray
                        },
                    modifier = Modifier.size(
                        if (compacto) 23.dp else 30.dp
                    )
                )
            }
        }
    }
}