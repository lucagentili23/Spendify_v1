package com.example.spendify_v1.data.repository

import android.util.Log
import com.example.spendify_v1.data.model.Frequenza
import com.example.spendify_v1.data.model.Spesa
import com.example.spendify_v1.data.model.Tipologia
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import javax.inject.Inject

class SpesaRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val gruppoRepository: GruppoRepository
) {

    suspend fun aggiungiSpesa(
        nome: String,
        tipologia: Tipologia,
        importo: Double,
        spesaDiGruppo: Boolean,
        periodica: Boolean,
        frequenza: Frequenza,
        note: String,
        primaDataPagamento: Timestamp?,
    ): Result<Unit> {
        return try {
            val uid = firebaseAuth.currentUser?.uid ?: return Result.failure(Exception("Utente non autenticato"))
            val docRef = firestore.collection("spese").document()
            var prossimaDataPagamento: Timestamp? = null
            val ora = Timestamp.now()
            var idGruppoSpesa: String? = null

            if (spesaDiGruppo) {
                val idGruppoCorrente = gruppoRepository.getGruppoIdUtenteFlow().firstOrNull()
                if (idGruppoCorrente == null) {
                    return Result.failure(Exception("L'utente non fa parte di nessun gruppo."))
                }
                idGruppoSpesa = idGruppoCorrente
            }

            if (periodica) {
                val oggiCalendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0);
                    set(Calendar.MINUTE, 0);
                    set(Calendar.SECOND, 0);
                    set(Calendar.MILLISECOND, 0)
                }
                val oggi = Timestamp(oggiCalendar.time)

                val primaDataPagamentoCalendar = Calendar.getInstance().apply {
                    time = primaDataPagamento!!.toDate()
                    set(Calendar.HOUR_OF_DAY, 0);
                    set(Calendar.MINUTE, 0);
                    set(Calendar.SECOND, 0);
                    set(Calendar.MILLISECOND, 0)
                }
                val primaDataPagamento2 = Timestamp(primaDataPagamentoCalendar.time)
                val calendar = Calendar.getInstance().apply { time = primaDataPagamento!!.toDate() }

                when (frequenza) {
                    Frequenza.SETTIMANALE -> calendar.add(Calendar.DAY_OF_YEAR, 7)
                    Frequenza.MENSILE -> calendar.add(Calendar.MONTH, 1)
                    Frequenza.BIMESTRALE -> calendar.add(Calendar.MONTH, 2)
                    Frequenza.TRIMESTRALE -> calendar.add(Calendar.MONTH, 3)
                    Frequenza.SEMESTRALE -> calendar.add(Calendar.MONTH, 6)
                    Frequenza.ANNUALE -> calendar.add(Calendar.YEAR, 1)
                }

                 if (primaDataPagamento2 == oggi) {
                     val nuovaSpesaDocRef = firestore.collection("spese").document()
                     val nuovaSpesa = Spesa(
                         id = nuovaSpesaDocRef.id,
                         idUtente = uid,
                         nome = nome,
                         tipologia = tipologia,
                         importo = importo,
                         dataCreazione = ora,
                         periodica = false,
                         frequenza = null,
                         primaDataPagamento = null,
                         prossimaDataPagamento = null,
                         note = "Pagamento periodico della spesa \"$nome\"",
                         idGruppo = idGruppoSpesa
                     )
                     nuovaSpesaDocRef.set(nuovaSpesa).await()
                     prossimaDataPagamento = Timestamp(calendar.time)
                 } else {
                     prossimaDataPagamento = primaDataPagamento2
                }
            }

            val spesa = Spesa(
                id = docRef.id,
                idUtente = uid,
                nome = nome,
                tipologia = tipologia,
                importo = importo,
                dataCreazione = ora,
                periodica = periodica,
                frequenza = if (periodica) frequenza else null,
                primaDataPagamento = if (periodica) primaDataPagamento else null,
                prossimaDataPagamento = if (periodica) prossimaDataPagamento else null,
                note = if (note.isBlank()) null else note,
                idGruppo = idGruppoSpesa
            )
            docRef.set(spesa).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTutteLeSpeseSeparate(): Pair<List<Spesa>, List<Spesa>> {
        try {
            val uid = firebaseAuth.currentUser?.uid ?: return Pair(emptyList(), emptyList())
            val oggi = getDataOdiernaTimestamp()

            val tutteLeSpese = firestore.collection("spese")
                .whereEqualTo("idUtente", uid)
                .whereEqualTo("idGruppo", null)
                .get()
                .await()
                .toObjects(Spesa::class.java)

            val (future, registrate) = tutteLeSpese.partition { spesa ->
                spesa.periodica && spesa.prossimaDataPagamento != null && spesa.prossimaDataPagamento > oggi
            }

            val registrateOrdinate = registrate.sortedByDescending { it.dataCreazione }
            val futureOrdinate = future.sortedBy { it.prossimaDataPagamento }

            return Pair(registrateOrdinate, futureOrdinate)

        } catch (e: Exception) {
            Log.e("SpesaRepository", "Errore nel recuperare le spese", e)
            return Pair(emptyList(), emptyList())
        }
    }

    suspend fun controllaSpesePeriodiche() {
        try {
            val uid = firebaseAuth.currentUser?.uid ?: return

            // Controlla le spese periodiche personali
            val queryPersonali = firestore.collection("spese")
                .whereEqualTo("idUtente", uid)
                .whereEqualTo("idGruppo", null)
            controllaSpesePerQuery(queryPersonali)

            // Controlla le spese periodiche del gruppo
            val idGruppoCorrente = gruppoRepository.getGruppoIdUtenteFlow().firstOrNull()
            if (idGruppoCorrente != null) {
                val queryGruppo = firestore.collection("spese")
                    .whereEqualTo("idGruppo", idGruppoCorrente)
                controllaSpesePerQuery(queryGruppo)
            }
        } catch (e: Exception) {
            Log.e("SpesaRepository", "Errore nel controllo generale delle spese periodiche", e)
        }
    }

    private suspend fun controllaSpesePerQuery(baseQuery: Query) {
        try {
            val calInizio = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            val calFine = Calendar.getInstance().apply {
                time = calInizio.time
                add(Calendar.DATE, 1)
            }
            val inizioGiorno = Timestamp(calInizio.time)
            val fineGiorno = Timestamp(calFine.time)

            val spese = baseQuery
                .whereEqualTo("periodica", true)
                .whereGreaterThanOrEqualTo("prossimaDataPagamento", inizioGiorno)
                .whereLessThan("prossimaDataPagamento", fineGiorno)
                .get()
                .await()
                .toObjects(Spesa::class.java)

            for (spesa in spese) {
                val nuovaSpesaDocRef = firestore.collection("spese").document()
                val nuovaSpesa = Spesa(
                    id = nuovaSpesaDocRef.id,
                    idUtente = spesa.idUtente,
                    nome = spesa.nome,
                    tipologia = spesa.tipologia,
                    importo = spesa.importo,
                    dataCreazione = Timestamp.now(),
                    periodica = false,
                    frequenza = null,
                    primaDataPagamento = null,
                    prossimaDataPagamento = null,
                    note = "Pagamento periodico della spesa \"${spesa.nome}\"",
                    idGruppo = spesa.idGruppo
                )
                nuovaSpesaDocRef.set(nuovaSpesa).await()

                val calendar = Calendar.getInstance().apply { time = spesa.prossimaDataPagamento!!.toDate() }
                when (spesa.frequenza) {
                    Frequenza.SETTIMANALE -> calendar.add(Calendar.DAY_OF_YEAR, 7)
                    Frequenza.MENSILE -> calendar.add(Calendar.MONTH, 1)
                    Frequenza.BIMESTRALE -> calendar.add(Calendar.MONTH, 2)
                    Frequenza.TRIMESTRALE -> calendar.add(Calendar.MONTH, 3)
                    Frequenza.SEMESTRALE -> calendar.add(Calendar.MONTH, 6)
                    else -> calendar.add(Calendar.YEAR, 1)
                }
                firestore.collection("spese").document(spesa.id)
                    .update("prossimaDataPagamento", Timestamp(calendar.time))
                    .await()
            }
        } catch (e: Exception) {
            Log.e("SpesaRepository", "Errore in controllaSpesePerQuery", e)
        }
    }

    fun getDataOdiernaTimestamp(): Timestamp {
        val oggiCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        return Timestamp(oggiCalendar.time)
    }

    suspend fun getSpesaById(spesaId: String): Spesa? {
        return try {
            val uid = firebaseAuth.currentUser?.uid ?: throw Exception("Utente non autenticato")
            val documentSnapshot = firestore.collection("spese").document(spesaId).get().await()
            val spesa = documentSnapshot.toObject(Spesa::class.java)

            // Se la spesa è di gruppo, qualsiasi membro può vederla, a
            // ltrimenti solo il creatore (utente autenticato al omento)
            val isMembro = spesa?.idGruppo?.let { gruppoRepository.getGruppoById(it)?.membri?.contains(uid) } ?: false
            if ((spesa?.idGruppo == null && spesa?.idUtente == uid) || isMembro) {
                spesa
            } else {
                Log.w("SpesaRepository", "Tentativo di accesso a spesa non autorizzata: $spesaId")
                null
            }
        } catch (e: Exception) {
            Log.e("SpesaRepository", "Errore nel recuperare la spesa con ID: $spesaId", e)
            null
        }
    }

    suspend fun deleteSpesa(spesaId: String): Result<Unit> {
        return try {
            firestore.collection("spese").document(spesaId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("SpesaRepository", "Errore nell'eliminare la spesa con ID: $spesaId", e)
            Result.failure(e)
        }
    }

    suspend fun getSpeseDelGruppo(gruppoId: String): List<Spesa> {
        if (gruppoId.isBlank()) return emptyList()
        return try {
            firestore.collection("spese")
                .whereEqualTo("idGruppo", gruppoId)
                .get()
                .await()
                .toObjects(Spesa::class.java)
        } catch (e: Exception) {
            Log.e("SpesaRepository", "Errore nel recuperare le spese per il gruppo ID: $gruppoId", e)
            emptyList()
        }
    }

    suspend fun getSpeseGruppoSeparate(gruppoId: String): Pair<List<Spesa>, List<Spesa>> {
        if (gruppoId.isBlank()) {
            return Pair(emptyList(), emptyList())
        }

        try {
            val oggi = getDataOdiernaTimestamp()
            val tutteLeSpese = getSpeseDelGruppo(gruppoId) // Riutilizza la funzione

            val (future, registrate) = tutteLeSpese.partition { spesa ->
                spesa.periodica && spesa.prossimaDataPagamento != null && spesa.prossimaDataPagamento > oggi
            }

            val registrateOrdinate = registrate.sortedByDescending { it.dataCreazione }
            val futureOrdinate = future.sortedBy { it.prossimaDataPagamento } // Ordina per la data corretta

            return Pair(registrateOrdinate, futureOrdinate)

        } catch (e: Exception) {
            Log.e("SpesaRepository", "Errore nel recuperare le spese separate del gruppo", e)
            return Pair(emptyList(), emptyList())
        }
    }
}