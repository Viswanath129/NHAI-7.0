package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AuthRepository
import com.example.domain.models.VerificationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val userId: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun loginWithPin(employeeId: String, pin: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.loginWithPin(employeeId, pin)
            result.onSuccess {
                _authState.value = AuthState.Success(employeeId)
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Authentication failed")
            }
        }
    }

    fun loginWithBiometric(employeeId: String, probeEmbedding: FloatArray) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val verification = authRepository.loginWithBiometric(employeeId, probeEmbedding)
            if (verification.isValid && verification.matchedEmployeeId != null) {
                 _authState.value = AuthState.Success(verification.matchedEmployeeId)
            } else {
                 _authState.value = AuthState.Error(verification.error ?: "Biometric verification failed")
            }
        }
    }

    fun checkSession() {
        if (authRepository.isSessionValid()) {
            _authState.value = AuthState.Success(authRepository.getLoggedInUserId() ?: "")
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _authState.value = AuthState.Idle
        }
    }
}
