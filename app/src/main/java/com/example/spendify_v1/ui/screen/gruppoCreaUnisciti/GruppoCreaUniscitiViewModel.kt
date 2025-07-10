package com.example.spendify_v1.ui.screen.gruppoCreaUnisciti

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spendify_v1.data.repository.GruppoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface GruppoUiState {
    object Idle : GruppoUiState // Stato di partenza
    object Loading : GruppoUiState
    data class Success(val nuovoGruppoId: String) : GruppoUiState // Contiene l'id del nuovo gruppo
    data class Error(val message: String) : GruppoUiState
}

@HiltViewModel
class GruppoCreaUniscitiViewModel @Inject constructor(
    private val gruppoRepository: GruppoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<GruppoUiState>(GruppoUiState.Idle)
    val uiState: StateFlow<GruppoUiState> = _uiState

    fun aggiungiGruppo(nome: String) {
        viewModelScope.launch {
            _uiState.value = GruppoUiState.Loading // Imposta lo stato su caricamento

            gruppoRepository.aggiungiGruppo(nome)
                .onSuccess { nuovoId ->
                    // aggiorna lo stato con l'id ricevuto
                    _uiState.value = GruppoUiState.Success(nuovoId)
                }
                .onFailure { error ->
                    // aggiorna lo stato con un messaggio di errore
                    _uiState.value = GruppoUiState.Error(error.message ?: "Errore sconosciuto")
                }
        }
    }

    fun uniscitiAGruppo(codiceInvito: String) {
        if (codiceInvito.isBlank()) return

        viewModelScope.launch {
            _uiState.value = GruppoUiState.Loading
            gruppoRepository.uniscitiAGruppo(codiceInvito)
                .onSuccess { gruppoId ->
                    // aggiorna lo stato con l'id ricevuto
                    _uiState.value = GruppoUiState.Success(gruppoId)
                }
                .onFailure { error ->
                    // aggiorna lo stato con un messaggio di errore
                    _uiState.value = GruppoUiState.Error(error.message ?: "Codice invito non valido.")
                }
        }
    }

    // Funzione per resettare lo stato
    fun resetState() {
        _uiState.value = GruppoUiState.Idle
    }
}