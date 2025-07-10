package com.example.spendify_v1.ui.screen.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spendify_v1.data.model.Spesa
import com.example.spendify_v1.data.repository.AuthRepository
import com.example.spendify_v1.data.repository.SpesaRepository
import com.example.spendify_v1.data.model.User
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// Data class per passare i dati alla legenda personalizzata
data class LegendItem(
    val label: String,
    val color: Int
)

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val spesaRepository: SpesaRepository,
) : ViewModel() {

    var currentUser by mutableStateOf<User?>(null)
        private set

    var isLoading by mutableStateOf(true)
        private set

    var chartData by mutableStateOf<List<PieEntry>>(emptyList())
        private set

    var legendData by mutableStateOf<List<LegendItem>>(emptyList())
        private set

    var totaleSpese by mutableDoubleStateOf(0.0)
        private set

    var chartColors by mutableStateOf<List<Int>>(emptyList())
        private set

    var lineChartData by mutableStateOf<List<Entry>>(emptyList())
        private set

    var lineChartXAxisLabels by mutableStateOf<List<String>>(emptyList())
        private set

    init {
        caricaDatiIniziali()
    }

    private fun caricaDatiIniziali() {
        viewModelScope.launch {
            isLoading = true

            spesaRepository.controllaSpesePeriodiche()

            val uid = authRepository.currentUser?.uid
            if (uid == null) {
                isLoading = false
                return@launch
            }

            try {
                val userDeferred = async { authRepository.getUserData(uid) }
                val speseDeferred = async { spesaRepository.getTutteLeSpeseSeparate() }

                val userResult = userDeferred.await()
                // L'underscore indica che per ora ignoro il secondo valore (spese future)
                val (speseRegistrate, _) = speseDeferred.await()

                if (userResult.isSuccess) {
                    currentUser = userResult.getOrNull()
                }

                // Uso la lista delle spese registrate per popolare i grafici
                processaDatiGraficoTorta(speseRegistrate)
                processaDatiGraficoLineare(speseRegistrate)

            } catch (e: Exception) {

            } finally {
                isLoading = false
            }
        }
    }

    private fun processaDatiGraficoTorta(spese: List<Spesa>) {
        if (spese.isEmpty()) {
            chartData = emptyList()
            legendData = emptyList()
            totaleSpese = 0.0
            chartColors = emptyList()
            return
        }
        totaleSpese = spese.sumOf { it.importo }
        val spesePerTipologia = spese.groupBy { it.tipologia }
            .mapValues { entry -> entry.value.sumOf { it.importo } }
        val speseList = spesePerTipologia.toList().sortedByDescending { it.second } // Ordina per importo decrescente

        chartData = speseList.map { (tipologia, importoTotale) ->
            PieEntry(importoTotale.toFloat(), tipologia!!.name.replaceFirstChar { it.titlecase() })
        }
        legendData = speseList.map { (tipologia, _) ->
            LegendItem(label = tipologia!!.name.replaceFirstChar { it.titlecase() }, color = tipologia.color)
        }
        chartColors = speseList.map { (tipologia, _) -> tipologia!!.color }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun processaDatiGraficoLineare(spese: List<Spesa>) {
        if (spese.isEmpty()) {
            lineChartData = emptyList()
            lineChartXAxisLabels = emptyList()
            return
        }

        val spesePerGiorno = spese
            .groupBy { spesa ->
                // Usa la `dataCreazione` per il grafico lineare
                Instant.ofEpochSecond(spesa.dataCreazione.seconds).atZone(ZoneId.systemDefault()).toLocalDate()
            }
            .mapValues { entry -> entry.value.sumOf { it.importo } }
            .toSortedMap() // Ordina le date automaticamente.

        // Formatta le etichette per l'asse x
        val formatter = DateTimeFormatter.ofPattern("dd/MM")
        lineChartXAxisLabels = spesePerGiorno.keys.map { it.format(formatter) }

        // Crea le entry per il grafico (coppie indice-valore).
        lineChartData = spesePerGiorno.values.mapIndexed { index, importo ->
            Entry(index.toFloat(), importo.toFloat())
        }
    }
}