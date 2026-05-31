package com.example.futbolnomade.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    nombreUsuario: String,
    onIrAElementos: () -> Unit,
    onIrAAcerca: () -> Unit,
    onIrAPartidos: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        Text(
            text = "Bienvenido $nombreUsuario",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onIrAPartidos,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver partidos")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onIrAElementos,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver elementos")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onIrAAcerca,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Acerca de")
        }
    }
}