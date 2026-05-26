package com.example.futbolnomade.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.futbolnomade.presentation.ui.AcercaScreen
import com.example.futbolnomade.presentation.ui.ElementosScreen
import com.example.futbolnomade.presentation.ui.HomeScreen
import com.example.futbolnomade.presentation.ui.LoginScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { nombreUsuario ->
                    navController.navigate(
                        Screen.Home.createRoute(nombreUsuario)
                    )
                }
            )
        }

        composable(Screen.Home.route) { backStackEntry ->
            val nombreUsuario = backStackEntry.arguments
                ?.getString("nombreUsuario")
                ?: ""

            HomeScreen(
                nombreUsuario = nombreUsuario,
                onIrAElementos = {
                    navController.navigate(Screen.Elementos.route)
                },
                onIrAAcerca = {
                    navController.navigate(Screen.Acerca.route)
                }
            )
        }

        composable(Screen.Elementos.route) {
            ElementosScreen(
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Acerca.route) {
            AcercaScreen(
                onVolver = {
                    navController.popBackStack()
                }
            )
        }
    }
}