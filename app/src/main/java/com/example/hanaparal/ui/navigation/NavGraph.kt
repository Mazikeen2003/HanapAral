package com.example.hanaparal.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object Profile : Screen("profile")
}

@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(route = Screen.Login.route) {
            // LoginScreen()
        }
        composable(route = Screen.Dashboard.route) {
            // DashboardScreen()
        }
        composable(route = Screen.Profile.route) {
            // ProfileScreen()
        }
    }
}
