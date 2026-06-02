package com.example.futbolnomade.presentation.ui.components

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.futbolnomade.presentation.navigation.Screen

private val VerdeOscuro = Color(0xFF0B4F00)

@Composable
fun AppBottomBar(
    navController: NavController,
    currentRoute: String?
) {
    NavigationBar(containerColor = VerdeOscuro) {

        NavigationBarItem(
            selected = currentRoute == Screen.Home.route,
            onClick = {
                navController.navigate(Screen.Home.route) {
                    launchSingleTop = true
                }
            },
            icon = { Text("⌂", color = Color.White) },
            label = { Text("Inicio", color = Color.White) }
        )

        NavigationBarItem(
            selected = currentRoute == Screen.Search.route,
            onClick = {
                navController.navigate(Screen.Search.route)
            },
            icon = { Text("⌕", color = Color.White) },
            label = { Text("Buscar", color = Color.White) }
        )

        NavigationBarItem(
            selected = currentRoute == Screen.Calendar.route,
            onClick = {
                navController.navigate(Screen.Calendar.route)
            },
            icon = { Text("▣", color = Color.White) },
            label = { Text("Calendario", color = Color.White) }
        )

        NavigationBarItem(
            selected = currentRoute == Screen.Perfil.route,
            onClick = {
                navController.navigate(Screen.Perfil.route)
            },
            icon = { Text("○", color = Color.White) },
            label = { Text("Perfil", color = Color.White) }
        )
    }
}