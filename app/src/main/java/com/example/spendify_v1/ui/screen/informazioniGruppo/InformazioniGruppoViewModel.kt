// in /ui/screen/informazioniGruppo/InformazioniGruppoViewModel.kt
package com.example.spendify_v1.ui.screen.informazioniGruppo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spendify_v1.data.model.Gruppo
import com.example.spendify_v1.data.model.User // <-- Importa il modello User
import com.example.spendify_v1.data.repository.GruppoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InformazioniGruppoViewModel @Inject constructor(
    private val gruppoRepository: GruppoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _gruppo = MutableStateFlow<Gruppo?>(null)
    val gruppo = _gruppo.asStateFlow()

    // --- NUOVO STATE PER I DETTAGLI DEI MEMBRI ---
    private val _membersDetails = MutableStateFlow<List<User>>(emptyList())
    val membersDetails = _membersDetails.asStateFlow()

    // IsLoading ora rappresenta il caricamento di gruppo E membri
    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        val gruppoId: String? = savedStateHandle["gruppoId"]
        if (gruppoId != null) {
            loadGruppoAndMembers(gruppoId)
        } else {
            _isLoading.value = false
        }
    }

    // --- LOGICA MODIFICATA PER CARICARE GRUPPO E MEMBRI ---
    private fun loadGruppoAndMembers(gruppoId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            // 1. Carica il gruppo
            val gruppoResult = gruppoRepository.getGruppoById(gruppoId)
            _gruppo.value = gruppoResult

            // 2. Se il gruppo esiste e ha membri, carica i loro dettagli
            if (gruppoResult != null && gruppoResult.membri.isNotEmpty()) {
                val members = gruppoRepository.getUsersByIds(gruppoResult.membri)
                _membersDetails.value = members
            }

            _isLoading.value = false
        }
    }
}