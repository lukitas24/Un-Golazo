package com.example.futbolnomade.presentation.ui.components

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VerdeOscuro = Color(0xFF0B4F00)

@Composable
fun AppBottomBar() {
    NavigationBar(
        containerColor = VerdeOscuro
    ) {
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = { Text("⌂", color = Color.White) },
            label = { Text("Inicio", color = Color.White) }
        )

        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Text("⌕", color = Color.White) },
            label = { Text("Buscar", color = Color.White) }
        )

        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Text("▣", color = Color.White) },
            label = { Text("Calendario", color = Color.White) }
        )

        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Text("○", color = Color.White) },
            label = { Text("Perfil", color = Color.White) }
        )
    }
}