package com.example.spendify_v1.data.repository

import android.util.Log
import com.example.spendify_v1.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val firebaseAuth: FirebaseAuth
) {

    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun getUserData(uid: String): Result<User> {
        return try {
            val user = firestore
                .collection("users")
                .document(uid)
                .get()
                .await()
                .toObject(User::class.java)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Utente non trovato"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerUser(nome: String, email: String, password: String): Result<FirebaseUser?> {
        return try {
            val authResult = firebaseAuth
                .createUserWithEmailAndPassword(email, password)
                .await()
            val user = authResult.user
            val utente = User(
                id = user!!.uid,
                nome = nome.replaceFirstChar { it.uppercaseChar() },
                email = email
            )
            firestore.collection("users")
                .document(user.uid)
                .set(utente)
                .await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<FirebaseUser?> {
        return try {
            val authResult = firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .await()
            Result.success(authResult.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logoutUser() {
        firebaseAuth.signOut()
    }

    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    suspend fun getNomiUtenti(uids: List<String>): Map<String, String> {
        if (uids.isEmpty()) return emptyMap()

        return try {
            val usersMap = mutableMapOf<String, String>()
            val jobs = uids.map { uid ->
                coroutineScope { // Creo uno scope per lanciare coroutine parallele
                    async {
                        val userDoc = firestore.collection("users").document(uid).get().await()
                        val userName = userDoc.getString("nome")
                        if (userName != null) {
                            usersMap[uid] = userName
                        }
                    }
                }
            }
            jobs.awaitAll() // Attende che tutte le chiamate siano terminate
            usersMap

        } catch (e: Exception) {
            Log.e("AuthRepository", "Errore nel recuperare i nomi degli utenti", e)
            emptyMap()
        }
    }
}
