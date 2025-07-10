package com.example.spendify_v1.ui.screen.gruppo

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spendify_v1.data.model.Spesa
import com.example.spendify_v1.data.repository.AuthRepository
import com.example.spendify_v1.data.repository.GruppoRepository
import com.example.spendify_v1.data.repository.SpesaRepository
import com.example.spendify_v1.ui.screen.home.LegendItem
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class GruppoViewModel @Inject constructor(
    private val gruppoRepository: GruppoRepository,
    private val spesaRepository: SpesaRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val gruppoId: String = checkNotNull(savedStateHandle["gruppoId"])

    var isLoading by mutableStateOf(true)
        private set

    var nomeGruppo by mutableStateOf("")
        private set

    var chartDataTipologie by mutableStateOf<List<PieEntry>>(emptyList())
        private set
    var legendDataTipologie by mutableStateOf<List<LegendItem>>(emptyList())
        private set
    var totaleSpese by mutableStateOf(0.0)
        private set
    var chartColors by mutableStateOf<List<Int>>(emptyList())
        private set

    var lineChartData by mutableStateOf<List<Entry>>(emptyList())
        private set
    var lineChartXAxisLabels by mutableStateOf<List<String>>(emptyList())
        private set

    var chartDataMembri by mutableStateOf<List<PieEntry>>(emptyList())
        private set
    var legendDataMembri by mutableStateOf<List<LegendItem>>(emptyList())
        private set


    init {
        caricaDatiGruppo()
    }

    private fun caricaDatiGruppo() {
        viewModelScope.launch {
            isLoading = true
            try {
                val gruppo = gruppoRepository.getGruppoById(gruppoId)

                if (gruppo != null) {
                    nomeGruppo = gruppo.nome
                    val speseGruppo = spesaRepository.getSpeseGruppoSeparate(gruppo.id)
                    val speseGruppo2 = speseGruppo.first

                    if (speseGruppo2.isNotEmpty()) {
                        processaDatiGraficoTortaTipologie(speseGruppo2)
                        processaDatiGraficoTortaMembri(speseGruppo2)
                        processaDatiGraficoLineare(speseGruppo2)
                    } else {
                        svuotaDatiGrafici()
                    }
                } else {
                    nomeGruppo = "Gruppo non trovato"
                    svuotaDatiGrafici()
                }
            } catch (e: Exception) {
                Log.e("GruppoViewModel", "Errore nel caricamento dei dati del gruppo", e)
                nomeGruppo = "Errore nel caricamento"
                svuotaDatiGrafici()
            } finally {
                isLoading = false
            }
        }
    }

    private fun svuotaDatiGrafici() {
        chartDataTipologie = emptyList()
        legendDataTipologie = emptyList()
        totaleSpese = 0.0
        chartColors = emptyList()
        lineChartData = emptyList()
        lineChartXAxisLabels = emptyList()
        chartDataMembri = emptyList()
        legendDataMembri = emptyList()
    }

    private fun processaDatiGraficoTortaTipologie(spese: List<Spesa>) {
        totaleSpese = spese.sumOf { it.importo }
        val spesePerTipologia = spese.groupBy { it.tipologia }
            .mapValues { entry -> entry.value.sumOf { it.importo } }
        val speseList = spesePerTipologia.toList().sortedByDescending { it.second }

        chartDataTipologie = speseList.mapNotNull { (tipologia, importoTotale) ->
            tipologia?.let {
                PieEntry(importoTotale.toFloat(), it.name.replaceFirstChar { char -> char.titlecase() })
            }
        }
        legendDataTipologie = speseList.mapNotNull { (tipologia, _) ->
            tipologia?.let {
                LegendItem(label = it.name.replaceFirstChar { char -> char.titlecase() }, color = it.color)
            }
        }
        chartColors = speseList.mapNotNull { (tipologia, _) ->
            tipologia?.color
        }
    }

    private suspend fun processaDatiGraficoTortaMembri(spese: List<Spesa>) {
        // Chiave speciale per raggruppare le spese senza un idUtente valido
        val MEMBRI_PASSATI_KEY = "membri_passati_special_key"

        // Raggruppa le spese, se idUtente è nullo o vuoto, usa la chiave speciale
        val spesePerUtente = spese.groupBy {
            if (it.idUtente.isNullOrBlank()) MEMBRI_PASSATI_KEY else it.idUtente
        }.mapValues { entry -> entry.value.sumOf { it.importo } }

        if (spesePerUtente.isEmpty()) return

        // Recupera i nomi solo per gli id utente validi (escludendo la chiave speciale)
        val userIds = spesePerUtente.keys.filter { it != MEMBRI_PASSATI_KEY }
        val nomiUtentiMap = if (userIds.isNotEmpty()) {
            authRepository.getNomiUtenti(userIds)
        } else {
            emptyMap()
        }

        // Combina i dati (nome, importo) e assegna l'etichettga speciale
        val speseUtentiList = spesePerUtente.map { (userId, importoTotale) ->
            val nomeUtente = if (userId == MEMBRI_PASSATI_KEY) {
                "Membri Passati" // Etichetta per le spese senza utente
            } else {
                nomiUtentiMap[userId] ?: "Utente Sconosciuto"
            }
            Pair(nomeUtente, importoTotale)
        }.sortedByDescending { it.second }

        // Prepara i dati per le fette del grafico
        chartDataMembri = speseUtentiList.map { (nome, importo) ->
            PieEntry(importo.toFloat(), nome)
        }

        // Prepara i dati per la legenda, riutilizzando i colori
        val coloriDisponibili = chartColors.ifEmpty { listOf(android.graphics.Color.LTGRAY) }
        legendDataMembri = speseUtentiList.mapIndexed { index, (nome, _) ->
            val color = coloriDisponibili[index % coloriDisponibili.size]
            LegendItem(label = nome, color = color)
        }
    }

    private fun processaDatiGraficoLineare(spese: List<Spesa>) {
        val spesePerGiorno = spese
            .groupBy { spesa ->
                Instant.ofEpochSecond(spesa.dataCreazione.seconds).atZone(ZoneId.systemDefault()).toLocalDate()
            }
            .mapValues { entry -> entry.value.sumOf { it.importo } }
            .toSortedMap()

        val formatter = DateTimeFormatter.ofPattern("dd/MM")
        lineChartXAxisLabels = spesePerGiorno.keys.map { it.format(formatter) }
        lineChartData = spesePerGiorno.values.mapIndexed { index, importo ->
            Entry(index.toFloat(), importo.toFloat())
        }
    }

    fun abbandonaGruppo() {
        viewModelScope.launch {
            gruppoRepository.abbandonaGruppo()
        }
    }
}