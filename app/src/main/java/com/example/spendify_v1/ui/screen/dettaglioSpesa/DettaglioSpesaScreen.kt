package com.example.spendify_v1.ui.screen.dettaglioSpesa

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.spendify_v1.data.model.Spesa
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

sealed interface DettaglioSpesaUiState {
    data class Success(val spesa: Spesa) : DettaglioSpesaUiState
    data class Error(val message: String) : DettaglioSpesaUiState
    object Loading : DettaglioSpesaUiState
    object Deleted : DettaglioSpesaUiState
}

@Composable
fun DettaglioSpesaScreen(
    navController: NavHostController,
    spesaId: String,
    viewModel: DettaglioSpesaViewModel = hiltViewModel()
) {
    LaunchedEffect(spesaId) {
        viewModel.caricaDettagliSpesa(spesaId)
    }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is DettaglioSpesaUiState.Deleted) {
            navController.popBackStack()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Gestisce i diversi stati della ui
        when (val state = uiState) {
            is DettaglioSpesaUiState.Loading -> {
                CircularProgressIndicator()
            }
            is DettaglioSpesaUiState.Error -> {
                Text(
                    text = "Errore: ${state.message}",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
            is DettaglioSpesaUiState.Success -> {
                DettaglioSpesaContent(
                    spesa = state.spesa,
                    onDelete = { viewModel.eliminaSpesa(spesaId) },
                    viewModel = viewModel
                )
            }
            is DettaglioSpesaUiState.Deleted -> {
                // Non mostra nulla mentre si torna indietro
            }
        }
    }
}

@Composable
private fun DettaglioSpesaContent(
    spesa: Spesa,
    onDelete: () -> Unit,
    viewModel: DettaglioSpesaViewModel
) {
    // Stato per mostrare/nascondere il dialogo di conferma eliminazione
    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Conferma Eliminazione") },
            text = {
                if (spesa.periodica) {
                    Text("Sei sicuro di voler eliminare la spesa \"${spesa.nome}\"?\nEliminando questa spesa periodica non verranno più generate ulteriori spese secondo la sua frequenza.\nL'azione è irreversibile.")
                } else {
                    Text("Sei sicuro di voler eliminare la spesa \"${spesa.nome}\"? L'azione è irreversibile.")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Elimina")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Annulla")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Sezione dei dettagli principali
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Dettaglio spesa",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            )
            Text(
                text = String.format(Locale.ITALY, "%.2f €", spesa.importo),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = spesa.nome,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(24.dp))

            // Altri dettagli
            InfoRow(label = "Tipologia", value = spesa.tipologia?.name ?: "Non specificata")
            InfoRow(
                label = "Data registrazione",
                value = timestampToDataString(spesa.dataCreazione)
            )
            if (spesa.idGruppo != null) {
                if (spesa.idUtente != null) {
                    LaunchedEffect(spesa.idUtente) {
                        viewModel.nomeCreatoreSpesa(spesa.idUtente)
                    }

                    viewModel.nomeCreatore?.let { nomeUtente ->
                        InfoRow(
                            label = "Creatore spesa",
                            value = nomeUtente
                        )
                    }
                } else {
                    InfoRow(
                        label = "Creatore spesa",
                        value = "Membro passato"
                    )
                }
            }
            InfoRow(
                label = "Spesa periodica",
                value = if (spesa.periodica) "Si" else "No"
            )
            if (spesa.periodica) {
                InfoRow(
                    label = "Prima data di pagamento",
                    value = timestampToDataString(spesa.primaDataPagamento!!)
                )
                InfoRow(
                    label = "Frequenza",
                    value = spesa.frequenza!!.name
                )
                InfoRow(
                    label = "Prossima data di pagamento",
                    value = timestampToDataString(spesa.prossimaDataPagamento!!)
                )
            }
            InfoRow(
                label = "Note",
                value = spesa.note ?: "Nessuna"
            )
        }

        Button(
            onClick = { showConfirmDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Elimina Spesa")
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun timestampToDataString(timestamp: Timestamp): String {
    val date = timestamp.toDate()
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(date)
}