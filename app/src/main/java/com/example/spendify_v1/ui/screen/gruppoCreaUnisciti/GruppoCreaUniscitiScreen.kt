package com.example.spendify_v1.ui.screen.gruppoCreaUnisciti

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController

@Composable
fun GruppoCreaUniscitiScreen(
    viewModel: GruppoCreaUniscitiViewModel = hiltViewModel(),
    navController: NavHostController
) {
    var codiceInvito by remember { mutableStateOf("") }
    var nomeGruppo by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()
    val isLoading = uiState is GruppoUiState.Loading

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Gruppo",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, top = 16.dp)
            )

            JoinGroupCard(
                codiceInvito = codiceInvito,
                onCodiceChange = { codiceInvito = it },
                onJoinClick = { viewModel.uniscitiAGruppo(codiceInvito) },
                isButtonEnabled = codiceInvito.isNotBlank() && !isLoading
            )

            CreateGroupCard(
                nomeGruppo = nomeGruppo,
                onNomeChange = { nomeGruppo = it },
                onCreateClick = { viewModel.aggiungiGruppo(nomeGruppo) },
                isButtonEnabled = nomeGruppo.isNotBlank() && !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (isLoading) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f)
            ) {
                CircularProgressIndicator(modifier = Modifier.wrapContentSize(Alignment.Center))
            }
        }
    }

    when (val state = uiState) {
        is GruppoUiState.Success -> {
            SuccessDialog(
                nuovoGruppoId = state.nuovoGruppoId,
                navController = navController,
                viewModel = viewModel
            )
        }
        is GruppoUiState.Error -> {
            ErrorDialog(
                errorMessage = state.message,
                onDismiss = { viewModel.resetState() }
            )
        }
        else -> {}
    }
}


@Composable
private fun SuccessDialog(
    nuovoGruppoId: String,
    navController: NavHostController,
    viewModel: GruppoCreaUniscitiViewModel
) {
    AlertDialog(
        onDismissRequest = { viewModel.resetState() },
        confirmButton = {
            TextButton(onClick = {
                navController.navigate("gruppo/$nuovoGruppoId") {
                    popUpTo("gruppoCreaUnisciti") { inclusive = true }
                }
                viewModel.resetState()
            }) {
                Text("OK")
            }
        },
        title = { Text("Operazione completata!") },
        text = { Text("Sei stato aggiunto al gruppo. Ora puoi iniziare a gestire le spese insieme.") }
    )
}


@Composable
private fun ErrorDialog(errorMessage: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Errore") },
        text = { Text(errorMessage) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}


@Composable
private fun JoinGroupCard(
    codiceInvito: String,
    onCodiceChange: (String) -> Unit,
    onJoinClick: () -> Unit,
    isButtonEnabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Entra in un Gruppo",
                style = MaterialTheme.typography.titleLarge
            )
            OutlinedTextField(
                value = codiceInvito,
                onValueChange = onCodiceChange,
                label = { Text("Codice di invito") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Button(
                onClick = onJoinClick,
                enabled = isButtonEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ENTRA NEL GRUPPO")
            }
        }
    }
}


@Composable
private fun CreateGroupCard(
    nomeGruppo: String,
    onNomeChange: (String) -> Unit,
    onCreateClick: () -> Unit,
    isButtonEnabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Crea un nuovo Gruppo",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Crea un gruppo e invita i tuoi amici per gestire le spese insieme.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            OutlinedTextField(
                value = nomeGruppo,
                onValueChange = onNomeChange,
                label = { Text("Nome del gruppo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Button(
                onClick = onCreateClick,
                enabled = isButtonEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("CREA GRUPPO")
            }
        }
    }
}