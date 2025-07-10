package com.example.spendify_v1.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.spendify_v1.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    internalNavController: NavHostController,
    appNavController: NavHostController,
    viewModel: MainViewModel = hiltViewModel(),
    content: @Composable (PaddingValues) -> Unit
) {
    // Prendo la rotta della schermata corrente
    val navBackStackEntry by internalNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isTopLevelRoute = currentRoute in listOf("home", "aggiungiSpesa") ||
            currentRoute?.startsWith("gruppo") == true ||
            currentRoute == "gruppoCreaUnisciti"

    val showBottomBar = isTopLevelRoute
    val showBackButton = !isTopLevelRoute

    val gruppoId by viewModel.gruppoIdState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spendify") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = { internalNavController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Torna indietro"
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.logout()
                        appNavController.navigate("login") {
                            popUpTo("main") { inclusive = true }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout"
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = currentRoute == "home",
                        onClick = {
                            internalNavController.navigate("home") {
                                popUpTo(internalNavController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Add, contentDescription = "Aggiungi spesa") },
                        label = { Text("Aggiungi spesa") },
                        selected = currentRoute == "aggiungiSpesa",
                        onClick = {
                            internalNavController.navigate("aggiungiSpesa") {
                                popUpTo(internalNavController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_group),
                                contentDescription = "Gruppo"
                            )
                        },
                        label = { Text("Gruppo") },
                        // Il pulsante viene evidenziato se si è in una di queste rotte
                        selected = currentRoute?.startsWith("gruppo") == true || currentRoute == "gruppoCreaUnisciti",
                        onClick = {
                            val destinazione = if (gruppoId != null) {
                                "gruppo/$gruppoId"
                            } else {
                                "gruppoCreaUnisciti"
                            }

                            internalNavController.navigate(destinazione) {
                                popUpTo(internalNavController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}