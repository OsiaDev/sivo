package com.coljuegos.sivo.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coljuegos.sivo.data.repository.AuthRepository
import com.coljuegos.sivo.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

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
                            // Guardar datos de sesión
                            //sharedPreferencesManager.saveLoginResponse(loginResponse)
                            _loginState.value = LoginState.Success(loginResponse)
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

    fun resetLoginState() {
        _loginState.value = LoginState.Idle
    }

}