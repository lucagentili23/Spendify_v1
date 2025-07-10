package com.example.spendify_v1.ui.screen.splash

import androidx.lifecycle.ViewModel
import com.example.spendify_v1.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }
}
