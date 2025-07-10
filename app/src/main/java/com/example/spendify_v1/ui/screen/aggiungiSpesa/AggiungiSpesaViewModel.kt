package com.example.spendify_v1.ui.screen.aggiungiSpesa

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spendify_v1.data.model.Frequenza
import com.example.spendify_v1.data.model.Tipologia
import com.example.spendify_v1.data.repository.GruppoRepository
import com.example.spendify_v1.data.repository.SpesaRepository
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class AggiungiSpesaViewModel @Inject constructor(
    private val spesaRepository: SpesaRepository,
    private val gruppoRepository: GruppoRepository
): ViewModel() {

    var nomeError by mutableStateOf<String?>(null)
        private set
    var importoError by mutableStateOf<String?>(null)
        private set
    var dataError by mutableStateOf<String?>(null)
        private set
    var gruppoError by mutableStateOf<String?>(null)
        private set

    var caricamento by mutableStateOf(false)
        private set
    var showSuccessDialog by mutableStateOf(false)
        private set

    fun resettaErroreNome() { nomeError = null }
    fun resettaErroreImporto() { importoError = null }
    fun resettaErroreData() { dataError = null }
    fun resettaErroreGruppo() { gruppoError = null }
    fun chiudiDialogSuccesso() { showSuccessDialog = false }

    fun aggiungiSpesa(
        nome: String,
        tipologia: Tipologia,
        importo: String,
        spesaDiGruppo: Boolean,
        periodica: Boolean,
        frequenza: Frequenza,
        note: String,
        primaDataPagamento: Long?,
    ) {
        var errori = false
        var primaDataPagamentoTimestamp: Timestamp? = null

        if (nome.isBlank()) {
            nomeError = "Il nome non può essere vuoto"
            errori = true
        }
        val importoDouble = importo.toDoubleOrNull()
        if (importoDouble == null || importoDouble <= 0.0) {
            importoError = "Inserire un importo valido"
            errori = true
        }
        if (periodica && primaDataPagamento == null) {
            dataError = "Inserire la prima data di pagamento"
            errori = true
        }
        if (periodica && primaDataPagamento != null) {
            primaDataPagamentoTimestamp = Timestamp(primaDataPagamento / 1000, ((primaDataPagamento % 1000) * 1_000_000).toInt())
            if (azzeraOrario(primaDataPagamentoTimestamp) < azzeraOrario(Timestamp.now())) {
                dataError = "Selezionare una data superiore o uguale a quella odierna"
                errori = true
            }
        }
        if (errori) return

        viewModelScope.launch {
            caricamento = true
            try {
                val result = spesaRepository.aggiungiSpesa(
                    nome = nome,
                    tipologia = tipologia,
                    importo = importoDouble!!,
                    spesaDiGruppo = spesaDiGruppo,
                    periodica = periodica,
                    frequenza = frequenza,
                    note = note,
                    primaDataPagamento = if (periodica) primaDataPagamentoTimestamp else null,
                )

                result.onSuccess {
                    showSuccessDialog = true
                }.onFailure { exception ->
                    if (exception.message == "L'utente non fa parte di nessun gruppo.") {
                        gruppoError = "Non puoi aggiungere una spesa di gruppo se non fai parte di un gruppo."
                    } else {
                        gruppoError = exception.message ?: "Si è verificato un errore imprevisto."
                    }
                }

            } catch (e: Exception) {
                gruppoError = "Errore di connessione o imprevisto."
            } finally {
                caricamento = false
            }
        }
    }

    private fun azzeraOrario(timestamp: Timestamp): Timestamp {
        val calendar = Calendar.getInstance().apply {
            time = timestamp.toDate()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return Timestamp(calendar.time)
    }

    fun getIdGruppoUtente(): Flow<String?> {
        return gruppoRepository.getGruppoIdUtenteFlow()
    }
}