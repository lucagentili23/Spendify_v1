package com.example.spendify_v1.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.spendify_v1.ui.screen.main.MainScreen
import com.example.spendify_v1.ui.screen.login.LoginScreen
import com.example.spendify_v1.ui.screen.signUp.SignUpScreen
import com.example.spendify_v1.ui.screen.splash.SplashScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph(
    navController: NavHostController
) {
    // NavHost è il componente di Jetpack Navigation che mostra la schermata
    // corrente in base alla "rotta" attiva
    NavHost(navController, startDestination = "splash") {
        // Associa la stringa (rotta) "splash" al Composable SplashScreen
        composable("splash") { SplashScreen(navController) }

        // Per cancellare lo stack back una volta loggati
        navigation(startDestination = "login", route = "auth") {
            composable("login") { LoginScreen(navController) }
            composable("signup") { SignUpScreen(navController) }
        }

        composable("main") {
            MainScreen(appNavController = navController)
        }
    }
}