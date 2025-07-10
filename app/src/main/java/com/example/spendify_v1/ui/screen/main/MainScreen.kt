package com.example.spendify_v1.ui.screen.main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.spendify_v1.ui.component.MainScaffold
import com.example.spendify_v1.ui.screen.aggiungiSpesa.AggiungiSpesaScreen
import com.example.spendify_v1.ui.screen.dettagli.DettagliScreen
import com.example.spendify_v1.ui.screen.dettagliGruppo.DettagliGruppoScreen
import com.example.spendify_v1.ui.screen.dettaglioSpesa.DettaglioSpesaScreen
import com.example.spendify_v1.ui.screen.gruppo.GruppoScreen
import com.example.spendify_v1.ui.screen.gruppoCreaUnisciti.GruppoCreaUniscitiScreen
import com.example.spendify_v1.ui.screen.home.HomeScreen
import com.example.spendify_v1.ui.screen.informazioniGruppo.InformazioniGruppoScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    appNavController: NavHostController
) {
    // L'oggetto NavController viene creato solo una volta (quando l'ui
    // viene disegnata) successivamente non viene ricreata un'istanza
    // ma viene utilizzata quella già creata (remember)
    val internalNavController = rememberNavController()

    MainScaffold(
        internalNavController = internalNavController,
        appNavController = appNavController,
    ) { paddingValues ->
        NavHost(
            navController = internalNavController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            val animationSpec = tween<IntOffset>(durationMillis = 350)
            val fadeSpec = tween<Float>(durationMillis = 150)

            // Schermate Principali
            composable(
                route = "home",
                exitTransition = { fadeOut(animationSpec = fadeSpec) },
                popEnterTransition = { fadeIn(animationSpec = fadeSpec) }
            ) {
                HomeScreen(navController = internalNavController)
            }

            composable("aggiungiSpesa") { AggiungiSpesaScreen(navController = internalNavController) }

            // Rotte per i Gruppi
            composable("gruppoCreaUnisciti") {
                GruppoCreaUniscitiScreen(navController = internalNavController)
            }

            composable(
                route = "gruppo/{gruppoId}",
                arguments = listOf(navArgument("gruppoId") {
                    type = NavType.StringType
                    nullable = true
                })
            ) { backStackEntry ->
                val gruppoId = backStackEntry.arguments?.getString("gruppoId")
                GruppoScreen(navController = internalNavController, gruppoId = gruppoId)
            }

            composable(
                route = "informazioniGruppo/{gruppoId}",
                arguments = listOf(navArgument("gruppoId") {
                    type = NavType.StringType
                    nullable = true
                })
            ) { backStackEntry ->
                backStackEntry.arguments?.getString("gruppoId")
                InformazioniGruppoScreen()
            }

            // Schermate di Dettaglio
            composable(
                route = "dettagli",
                enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = animationSpec) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = animationSpec) }
            ) {
                DettagliScreen(navController = internalNavController)
            }

            composable(
                route = "dettaglioSpesa/{spesaId}",
                arguments = listOf(navArgument("spesaId") { type = NavType.StringType })
            ) { backStackEntry ->
                val spesaId = backStackEntry.arguments?.getString("spesaId")
                requireNotNull(spesaId) { "L'ID della spesa non può essere nullo" }
                DettaglioSpesaScreen(navController = internalNavController, spesaId = spesaId)
            }

            composable(
                route = "dettagliGruppo/{gruppoId}",
                arguments = listOf(navArgument("gruppoId") { type = NavType.StringType }),
                enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = animationSpec) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = animationSpec) }
            ) {
                // Non serve l'id perché il DettagliGruppoViewModel lo ottiene
                // tramite Hilt e SavedStateHandle.
                DettagliGruppoScreen(navController = internalNavController)
            }
        }
    }
}