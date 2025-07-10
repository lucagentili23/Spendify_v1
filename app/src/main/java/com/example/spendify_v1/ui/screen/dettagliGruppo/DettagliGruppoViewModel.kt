package com.example.spendify_v1.ui.screen.dettagliGruppo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spendify_v1.data.model.Spesa
import com.example.spendify_v1.data.repository.SpesaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DettagliGruppoViewModel @Inject constructor(
    private val spesaRepository: SpesaRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val gruppoId: String = checkNotNull(savedStateHandle["gruppoId"])

    // logica degli stati è uguale a DettagliViewModel
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var allSpeseRegistrate: List<Spesa> = emptyList()
    private var allSpeseFuture: List<Spesa> = emptyList()

    private var visibleSpeseRegistrateCount = 10
    private var visibleSpeseFutureCount = 10
    private val PAGE_SIZE = 10

    private val _speseEntrate = MutableStateFlow<List<Spesa>>(emptyList())
    val speseEntrate: StateFlow<List<Spesa>> = _speseEntrate.asStateFlow()

    private val _speseInEntrata = MutableStateFlow<List<Spesa>>(emptyList())
    val speseInEntrata: StateFlow<List<Spesa>> = _speseInEntrata.asStateFlow()

    private val _canShowMoreRegistrate = MutableStateFlow(false)
    val canShowMoreRegistrate: StateFlow<Boolean> = _canShowMoreRegistrate.asStateFlow()

    private val _canShowMoreFuture = MutableStateFlow(false)
    val canShowMoreFuture: StateFlow<Boolean> = _canShowMoreFuture.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            // chiama la funzione spoecifica per il gruppo
            val (registrate, future) = spesaRepository.getSpeseGruppoSeparate(gruppoId)

            allSpeseRegistrate = registrate
            allSpeseFuture = future
            updatePaginatedLists()
            _isLoading.value = false
        }
    }

    fun mostraAltreSpeseRegistrate() {
        visibleSpeseRegistrateCount += PAGE_SIZE
        updatePaginatedLists()
    }

    fun mostraAltreSpeseFuture() {
        visibleSpeseFutureCount += PAGE_SIZE
        updatePaginatedLists()
    }


    private fun updatePaginatedLists() {
        // Aggiorna la lista di spese registrate visibili
        _speseEntrate.update { allSpeseRegistrate.take(visibleSpeseRegistrateCount) }
        // Controlla se ci sono altri elementi da mostrare
        _canShowMoreRegistrate.update { visibleSpeseRegistrateCount < allSpeseRegistrate.size }

        // Aggiorna la lista di spese future visibili
        _speseInEntrata.update { allSpeseFuture.take(visibleSpeseFutureCount) }
        // Controlla se ci sono altri elementi da mostrare
        _canShowMoreFuture.update { visibleSpeseFutureCount < allSpeseFuture.size }
    }
}