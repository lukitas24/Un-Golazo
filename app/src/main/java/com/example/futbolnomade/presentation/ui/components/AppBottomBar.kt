package com.example.futbolnomade.presentation.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.futbolnomade.presentation.navigation.Screen

private val VerdeOscuro = Color(0xFF121212) // Fondo más moderno (casi negro)
private val VerdeNeon = Color(0xFF8BC34A)    // Color principal de la app

@Composable
fun AppBottomBar(
    navController: NavController,
    currentRoute: String?
) {
    NavigationBar(
        containerColor = VerdeOscuro,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            Triple(Screen.Home.route, Icons.Default.Home, "Inicio"),
            Triple(Screen.Search.route, Icons.Default.Search, "Buscar"),
            Triple(Screen.Calendar.route, Icons.Default.CalendarMonth, "Calendario"),
            Triple(Screen.Perfil.route, Icons.Default.Person, "Perfil")
        )

        items.forEach { (route, icon, label) ->
            val isSelected = currentRoute == route
            
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) VerdeNeon else Color.Gray
                    )
                },
                label = {
                    Text(
                        text = label,
                        color = if (isSelected) VerdeNeon else Color.Gray
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = VerdeNeon.copy(alpha = 0.1f)
                )
            )
        }
    }
}