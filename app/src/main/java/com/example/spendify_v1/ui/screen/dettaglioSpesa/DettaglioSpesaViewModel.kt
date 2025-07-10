package com.example.spendify_v1.ui.screen.dettaglioSpesa

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spendify_v1.data.repository.AuthRepository
import com.example.spendify_v1.data.repository.SpesaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DettaglioSpesaViewModel @Inject constructor(
    private val spesaRepository: SpesaRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DettaglioSpesaUiState>(DettaglioSpesaUiState.Loading)
    val uiState: StateFlow<DettaglioSpesaUiState> = _uiState

    var nomeCreatore by mutableStateOf<String?>(null)
        private set

    fun caricaDettagliSpesa(spesaId: String) {
        viewModelScope.launch {
            _uiState.value = DettaglioSpesaUiState.Loading
            try {
                val spesa = spesaRepository.getSpesaById(spesaId)
                if (spesa != null) {
                    _uiState.value = DettaglioSpesaUiState.Success(spesa)
                } else {
                    _uiState.value = DettaglioSpesaUiState.Error("Spesa non trovata.")
                }
            } catch (e: Exception) {
                _uiState.value = DettaglioSpesaUiState.Error(e.message ?: "Errore sconosciuto")
            }
        }
    }

    fun eliminaSpesa(spesaId: String) {
        viewModelScope.launch {
            try {
                spesaRepository.deleteSpesa(spesaId)
                _uiState.value = DettaglioSpesaUiState.Deleted
            } catch (e: Exception) {
                _uiState.value = DettaglioSpesaUiState.Error("Impossibile eliminare la spesa: ${e.message}")
            }
        }
    }

    fun nomeCreatoreSpesa(id: String) {
        viewModelScope.launch {
            val result = authRepository.getUserData(id)
            nomeCreatore = if (result.isSuccess) {
                result.getOrNull()?.nome
            } else {
                "Errore"
            }
        }
    }

}