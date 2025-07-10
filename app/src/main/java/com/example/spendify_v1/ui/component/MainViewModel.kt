package com.example.spendify_v1.ui.component

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spendify_v1.data.repository.AuthRepository
import com.example.spendify_v1.data.repository.GruppoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    gruppoRepository: GruppoRepository
): ViewModel() {

    // Uno StateFlow che contiene l'ID del gruppo dell'utente (o null).
    // La UI osserverà questo stato (è sempre aggiornato in tempo reale).
    // StateFlow è una classe per rappresentare uno flusso osservabile di stato, che
    // emette aggiornamenti a degli osservatori
    val gruppoIdState: StateFlow<String?> = gruppoRepository.getGruppoIdUtenteFlow()
        // Converte un Flow (no dati persistenti) in uno StateFlow (dati persistenti)
        .stateIn(
            // Il flusso viene collezionato nel viewModelScope
            // (quindi segue il ciclo di vita del ViewModel)
            scope = viewModelScope,
            // Colleziona il Flow solo mentre c’è almeno un osservatore attivo.
            // Se nessuno lo osserva, smette dopo 5 secondi
            started = SharingStarted.WhileSubscribed(5000),
            // Imposta null come valore iniziale finché non arriva il primo
            // valore dal Flow originale
            initialValue = null
        )

    fun logout() {
        authRepository.logoutUser()
    }
}