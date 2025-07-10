package com.example.spendify_v1.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Gruppo (
    val id: String = "",
    val nome: String = "",
    val codiceInvito: String = "",
    val idAdmin: String = "",
    val membri: List<String> = emptyList(),
    @ServerTimestamp    // Per evitare di usare l'orologio del dispsitivo ma quello di firebase
    val dataCreazione: Timestamp? = null
)