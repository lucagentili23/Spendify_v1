package com.example.spendify_v1.di

import android.content.Context
import android.content.SharedPreferences
import com.example.spendify_v1.data.repository.AuthRepository
import com.example.spendify_v1.data.repository.GruppoRepository
import com.example.spendify_v1.data.repository.SpesaRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// file che insegna a hilt come creare le istanze dei repositoty o firebaseAuth ecc
// da passare ai viewModel senza che debbano essere creati dentro tali viewModel

//Identifica l'oggetto come un "modulo" (un contenitore di istruzioni per Hilt)
@Module
// Specifica che le dipendenze definite qui avranno un ciclo di vita a livello
// di applicazione (Saranno create una volta e vivranno finché l'app è in esecuzione)
@InstallIn(SingletonComponent::class)
object AppModule {

    // Dice a Hilt che quando qualcuno chiede questo tipo di oggetto,
    // va eseguita questa funzione per crearlo
    @Provides
    // Significa che Hilt eseguirà questa funzione una sola volta e poi
    // riutilizzerà sempre la stessa istanza ogni volta che gli verrà richiesta
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth
    ): AuthRepository = AuthRepository(firebaseAuth)

    @Provides
    @Singleton
    fun provideSpesaRepository(
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth,
        guppoRepository: GruppoRepository
    ): SpesaRepository = SpesaRepository(firestore, firebaseAuth, guppoRepository)

    @Provides
    @Singleton
    fun provideGruppoRepository(
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth
    ): GruppoRepository = GruppoRepository(firebaseAuth, firestore)

    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("preferenze_spese", Context.MODE_PRIVATE)
    }
}