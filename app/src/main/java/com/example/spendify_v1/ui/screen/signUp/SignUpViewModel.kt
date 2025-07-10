package com.example.spendify_v1.ui.screen.signUp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spendify_v1.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    var nomeError by mutableStateOf<String?>(null)
        private set
    var emailError by mutableStateOf<String?>(null)
        private set
    var passwordError by mutableStateOf<String?>(null)
        private set
    var confermaPasswordError by mutableStateOf<String?>(null)
        private set

    var showSuccessDialog by mutableStateOf(false)
        private set

    fun mostraDialogSuccesso() {
        showSuccessDialog = true
    }

    fun chiudiDialogSuccesso() {
        showSuccessDialog = false
    }

    fun resettaErrori() {
        nomeError = null
        emailError = null
        passwordError = null
        confermaPasswordError = null
    }

    var caricamento by mutableStateOf(false)
        private set

    fun signUp(nome: String, email: String, password: String, confermaPassword: String) {
        resettaErrori()

        var hasError = false

        if (nome.isBlank()) {
            nomeError = "Il nome non può essere vuoto"
            hasError = true
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Email non valida"
            hasError = true
        }
        if (password.length < 8) {
            passwordError = "La password deve avere almeno 8 caratteri"
            hasError = true
        }
        if (confermaPassword != password) {
            confermaPasswordError = "Le password non coincidono"
            hasError = true
        }

        if (!hasError) {
            viewModelScope.launch {
                caricamento = true
                try {
                    val result = authRepository.registerUser(nome, email, password)
                    result.getOrThrow()
                    mostraDialogSuccesso()
                } catch (e: Exception) {
                    if (e is FirebaseAuthUserCollisionException) {
                        emailError = "Email già in uso"
                    } else {
                        emailError = "Si è verificato un errore imprevisto"
                    }
                } finally {
                    caricamento = false
                }
            }
        }
    }

}