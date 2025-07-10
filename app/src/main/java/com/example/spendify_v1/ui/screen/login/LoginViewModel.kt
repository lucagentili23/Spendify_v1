package com.example.spendify_v1.ui.screen.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spendify_v1.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    var credenzialiError by mutableStateOf<String?>(null)
        private set
    var caricamento by mutableStateOf(false)
        private set
    var loginSuccess by mutableStateOf(false)
        private set

    fun resettaErrori() {
        credenzialiError = null
    }

    fun onLoginSuccessHandled() {
        loginSuccess = false
    }

    fun login(email: String, password: String) {
        resettaErrori()
        loginSuccess = false

        viewModelScope.launch {
            caricamento = true
            try {
                val result = authRepository.loginUser(email, password)
                result.getOrThrow()
                loginSuccess = true
            } catch (e: Exception) {
                credenzialiError = "Le credenziali inserite non sono corrette"
            } finally {
                caricamento = false
            }
        }
    }
}