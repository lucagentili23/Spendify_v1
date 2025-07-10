package com.example.spendify_v1.data.model

import com.google.firebase.Timestamp
import androidx.core.graphics.toColorInt

data class Spesa (
    val id: String = "",
    val idUtente: String = "",
    val nome: String = "",
    val tipologia: Tipologia? = null,
    val importo: Double = 0.0,
    val dataCreazione: Timestamp = Timestamp.now(),
    val periodica: Boolean = false,
    val frequenza: Frequenza? = null,
    val primaDataPagamento: Timestamp? = null,
    val prossimaDataPagamento: Timestamp? = null,
    val note: String? = null,
    val idGruppo: String? = null
)

enum class Tipologia(val color: Int) {
    SPESA("#0ad2ff".toColorInt()),        // Blu
    BOLLETTE("#2962ff".toColorInt()),     // Rosso
    AFFITTO("#9500ff".toColorInt()),      // Verde
    MUTUO("#ff0059".toColorInt()),        // Arancione
    ASSICURAZIONE("#ff8c00".toColorInt()),// Viola
    TASSE("#b4e600".toColorInt()),        // Grigio-Blu
    ALTRO("#0fffdb".toColorInt());        // Grigio chiaro
}

enum class Frequenza {
    SETTIMANALE,
    MENSILE,
    BIMESTRALE,
    TRIMESTRALE,
    SEMESTRALE,
    ANNUALE
}