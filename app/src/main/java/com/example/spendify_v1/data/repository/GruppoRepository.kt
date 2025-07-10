package com.example.spendify_v1.data.repository

import android.util.Log
import com.example.spendify_v1.data.model.Gruppo
import com.example.spendify_v1.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldPath
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class GruppoRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    suspend fun aggiungiGruppo(nome: String): Result<String> {
        return try {
            val uid = firebaseAuth.currentUser?.uid
                ?: return Result.failure(Exception("Utente non autenticato"))

            val docRef = firestore.collection("gruppi").document()
            val membriIniziali = listOf(uid)

            val gruppo = Gruppo(
                id = docRef.id,
                codiceInvito = docRef.id,
                nome = nome,
                idAdmin = uid,
                membri = membriIniziali
                // `dataCreazione` sarà gestita da @ServerTimestamp nel modello
            )

            docRef.set(gruppo).await()
            Result.success(docRef.id)

        } catch (e: Exception) {
            Log.e("GruppoRepository", "Errore durante la creazione del gruppo", e)
            Result.failure(e)
        }
    }

    suspend fun uniscitiAGruppo(codiceInvito: String): Result<String> {
        return try {
            val uid = firebaseAuth.currentUser?.uid
                ?: return Result.failure(Exception("Utente non autenticato"))

            val gruppoRef = firestore.collection("gruppi").document(codiceInvito)
            val gruppoDoc = gruppoRef.get().await()

            if (!gruppoDoc.exists()) {
                return Result.failure(Exception("Codice invito non valido. Nessun gruppo trovato."))
            }

            val gruppo = gruppoDoc.toObject(Gruppo::class.java)

            if (gruppo!!.membri.size >= 6) {
                return Result.failure(Exception("Questo gruppo è pieno."))
            }

            if (gruppo.membri.contains(uid)) {
                return Result.failure(Exception("Fai già parte di questo gruppo."))
            }

            gruppoRef.update("membri", FieldValue.arrayUnion(uid)).await()
            Result.success(codiceInvito)

        } catch (e: Exception) {
            Log.e("GruppoRepository", "Errore durante l'unione al gruppo", e)
            Result.failure(e)
        }
    }

    suspend fun getGruppoById(gruppoId: String): Gruppo? {
        return try {
            firestore.collection("gruppi")
                .document(gruppoId)
                .get()
                .await()
                .toObject(Gruppo::class.java)
        } catch (e: Exception) {
            Log.e("GruppoRepository", "Errore nel recuperare il gruppo con ID: $gruppoId", e)
            null
        }
    }

    suspend fun getUsersByIds(userIds: List<String>): List<User> {
        if (userIds.isEmpty()) {
            return emptyList()
        }
        return try {
            firestore.collection("users")
                .whereIn(FieldPath.documentId(), userIds)
                .get()
                .await()
                .toObjects(User::class.java)
        } catch (e: Exception) {
            Log.e("GruppoRepository", "Errore nel recuperare gli utenti per IDs", e)
            emptyList() // Restituisce una lista vuota per evitare un cragsh
        }
    }

    fun getGruppoIdUtenteFlow(): Flow<String?> {
        val uid = firebaseAuth.currentUser?.uid
        if (uid == null) {
            return flowOf(null)
        }

        // callbackFlow è un costruttore di Flow
        return callbackFlow {
            val listener = firestore.collection("gruppi")
                .whereArrayContains("membri", uid)
                .limit(1)
                // Invece di ottenere i dati una sola volta (.get()), uso un listener
                // persistente. Questo listener verrà notificato da firestore ogni
                // volta che qualcosa cambia nei dati che corrispondono alla query
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)    // Il flow viene chuso con un errore
                        // Serve per uscire da questo blocco di codice (la lambda
                        // addSnapshotListener) e non dalle funzioni più esterne
                        return@addSnapshotListener
                    }
                    // Se è stato trovato un gruppo
                    if (snapshot != null && !snapshot.isEmpty) {
                        // trySend: comando usato all'interno di un callbackFlow
                        // per emettere un valore nel Flow
                        trySend(snapshot.documents.first().id)
                    } else {
                        trySend(null)
                    }
                }
            // Alla fine il listener di firebase viene rimosso
            awaitClose { listener.remove() }
        }
    }

    suspend fun abbandonaGruppo() {
        val idGruppo = getGruppoIdUtenteFlow().firstOrNull()
        val idUtente = firebaseAuth.currentUser?.uid ?: return
        var gruppoRef: DocumentReference? = null

        if (idGruppo != null) {
            gruppoRef = firestore
                .collection("gruppi")
                .document(idGruppo)
        } else {
            return
        }

        try {
            val snapshot = firestore
                .collection("spese")
                .whereEqualTo("idUtente", idUtente)
                .whereEqualTo("idGruppo", idGruppo)
                .get()
                .await()

            for (document in snapshot.documents) {

                if (document.getBoolean("periodica") == true) {
                    document.reference.delete().await()
                }

                document.reference.update("idUtente", null).await()
            }

            gruppoRef.update("membri", FieldValue.arrayRemove(idUtente)).await()

        } catch (e: Exception) {
            Log.e("GruppoRepository", "Errore nell'abbandono del gruppo", e)
        }
    }
}