package com.coljuegos.sivo.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coljuegos.sivo.data.repository.AuthRepository
import com.coljuegos.sivo.utils.NetworkResult
import com.coljuegos.sivo.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)

    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)

    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Verifica si el usuario ya tiene una sesión activa
     */
    suspend fun isAlreadyLoggedIn(): Boolean {
        return try {
            this.sessionManager.isLoggedIn()
        } catch (e: Exception) {
            false
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            authRepository.login(username, password).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _isLoading.value = true
                        _loginState.value = LoginState.Loading
                    }

                    is NetworkResult.Success -> {
                        _isLoading.value = false
                        result.data?.let { loginResponse ->
                            try {
                                // Guardar datos de sesión (esto desactiva automáticamente las sesiones anteriores)
                                sessionManager.saveUserSession(loginResponse)
                                _loginState.value = LoginState.Success(loginResponse)
                            } catch (e: Exception) {
                                _loginState.value = LoginState.Error("Error al guardar la sesión: ${e.message}")
                            }
                        }
                    }

                    is NetworkResult.Error -> {
                        _isLoading.value = false
                        _loginState.value = LoginState.Error(result.message ?: "Error desconocido")
                    }
                }
            }
        }
    }

    /**
     * Resetea el estado del login
     */
    fun resetLoginState() {
        _loginState.value = LoginState.Idle
        _isLoading.value = false
    }

    /**
     * Cierra la sesión actual
     */
    fun logout() {
        viewModelScope.launch {
            try {
                sessionManager.logout()
                _loginState.value = LoginState.Idle
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Error al cerrar sesión: ${e.message}")
            }
        }
    }


}