package com.example.spendify_v1.ui.screen.aggiungiSpesa

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.spendify_v1.data.model.Frequenza
import com.example.spendify_v1.data.model.Tipologia
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AggiungiSpesaScreen(
    navController: NavHostController,
    viewModel: AggiungiSpesaViewModel = hiltViewModel()
) {

    var nome by remember { mutableStateOf("") }
    var tipologia by remember { mutableStateOf(Tipologia.SPESA) }
    var importo by remember { mutableStateOf("") }
    var spesaDiGruppo by remember { mutableStateOf(false) }
    var periodica by remember { mutableStateOf(false) }
    var frequenza by remember { mutableStateOf(Frequenza.SETTIMANALE) }
    var note by remember { mutableStateOf("") }

    var espansoTipologia by remember { mutableStateOf(false) }
    var espansoFrequenza by remember { mutableStateOf(false) }
    val opzioniTipologia = Tipologia.entries
    val opzioniFrequenza = Frequenza.entries

    val gruppoIdState by viewModel.getIdGruppoUtente().collectAsState(initial = null)

    val scrollState = rememberScrollState()

    var dataSelezionata by remember { mutableStateOf<Long?>(null) }
    var mostraDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Aggiungi spesa",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(30.dp))

        OutlinedTextField(
            value = nome,
            onValueChange = {
                if (it.length <= 30) {
                    nome = it
                    viewModel.resettaErroreNome()
                }
                            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nome") },
            shape = RoundedCornerShape(16.dp),
            isError = viewModel.nomeError != null,
            singleLine = true
        )
        if (viewModel.nomeError != null) {
            Text(
                viewModel.nomeError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start).padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = espansoTipologia,
            onExpandedChange = {
                espansoTipologia = !espansoTipologia
            }
        ) {
            OutlinedTextField(
                value = tipologia.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Tipologia") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = espansoTipologia)
                               },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.menuAnchor().fillMaxWidth())
            ExposedDropdownMenu(
                expanded = espansoTipologia,
                onDismissRequest = { espansoTipologia = false }
            ) {
                opzioniTipologia.forEach { tipo ->
                    DropdownMenuItem(
                        text = { Text(tipo.name) },
                        onClick = {
                            tipologia = tipo;
                            espansoTipologia = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = importo,
            onValueChange = { nuovoValore ->
                val valoreConPunto = nuovoValore.replace(',', '.')
                val regex = Regex("^\\d{1,6}(\\.\\d{0,2})?$")
                if (valoreConPunto.isEmpty() || regex.matches(valoreConPunto)) {
                    importo = valoreConPunto
                    viewModel.resettaErroreImporto()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Importo") },
            shape = RoundedCornerShape(16.dp),
            isError = viewModel.importoError != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            prefix = { Text("€ ") }
        )
        if (viewModel.importoError != null) {
            Text(viewModel.importoError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.Start).padding(top = 4.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = spesaDiGruppo,
                onCheckedChange = {
                    spesaDiGruppo = it
                    // Se seleziona/deseleziona resetta l'eventuale errore precedente
                    viewModel.resettaErroreGruppo()
                }
            )
            Text("Spesa di gruppo")
        }
        if (viewModel.gruppoError != null) {
            Text(
                text = viewModel.gruppoError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 4.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = periodica,
                onCheckedChange = {
                    periodica = it
                }
            )
            Text("Spesa periodica")
        }

        if (periodica) {
            Spacer(modifier = Modifier.height(10.dp))
            ExposedDropdownMenuBox(
                expanded = espansoFrequenza,
                onExpandedChange = {
                    espansoFrequenza = !espansoFrequenza
                }
            ) {
                OutlinedTextField(
                    value = frequenza.name.lowercase().replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Frequenza") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = espansoFrequenza)
                                   },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.menuAnchor().fillMaxWidth())
                ExposedDropdownMenu(
                    expanded = espansoFrequenza,
                    onDismissRequest = { espansoFrequenza = false }
                ) {
                    opzioniFrequenza.forEach { tipo ->
                        DropdownMenuItem(
                            text = { Text(tipo.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                frequenza = tipo
                                espansoFrequenza = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth().clickable { mostraDatePicker = true }) {
                OutlinedTextField(
                    value = if (dataSelezionata != null) dateFormatter.format(Date(dataSelezionata!!)) else "",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Prima data di pagamento") },
                    shape = RoundedCornerShape(16.dp),
                    readOnly = true,
                    enabled = false,
                    isError = viewModel.dataError != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = if (viewModel.dataError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    trailingIcon = { Icon(Icons.Default.DateRange, "Seleziona data") }
                )
            }
            if (viewModel.dataError != null) {
                Text(
                    viewModel.dataError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start).padding(top = 4.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = note,
            onValueChange = { if (it.length <= 300) note = it },
            label = { Text("Note (facoltativo)") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            singleLine = false,
            shape = RoundedCornerShape(16.dp),
            maxLines = 5,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (!viewModel.caricamento) {
                    viewModel.aggiungiSpesa(
                        nome,
                        tipologia,
                        importo,
                        spesaDiGruppo,
                        periodica,
                        frequenza,
                        note,
                        if (periodica) dataSelezionata else null,
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            enabled = !viewModel.caricamento
        ) {
            if (viewModel.caricamento) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(2.dp),
                    strokeWidth = 3.dp
                )
            } else {
                Text("Aggiungi spesa")
            }
        }
    }

    // Dialog di successo
    if (viewModel.showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.chiudiDialogSuccesso() },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.chiudiDialogSuccesso()
                    if (spesaDiGruppo) {
                        navController.navigate("gruppo/$gruppoIdState") {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    } else {
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                }) {
                    Text("Continua")
                }
            },
            title = { Text("Spesa registrata") },
            text = { Text("La spesa è stata registrata con successo!") }
        )
    }

    // Dialog per la selezione della data
    if (mostraDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { mostraDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        dataSelezionata = datePickerState.selectedDateMillis
                        viewModel.resettaErroreData()
                        mostraDatePicker = false
                    },
                    enabled = datePickerState.selectedDateMillis != null
                ) { Text("Conferma") }
            },
            dismissButton = { TextButton(onClick = { mostraDatePicker = false }) { Text("Annulla") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}