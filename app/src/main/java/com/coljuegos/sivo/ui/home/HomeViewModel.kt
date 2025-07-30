package com.coljuegos.sivo.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coljuegos.sivo.data.entity.ActaEntity
import com.coljuegos.sivo.data.repository.ActasRepository
import com.coljuegos.sivo.utils.NetworkResult
import com.coljuegos.sivo.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val actasRepository: ActasRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())

    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUserData()
        loadActas()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            try {
                val currentSession = sessionManager.getCurrentSession()
                val fullName = currentSession?.fullNameUserSession

                _uiState.value = _uiState.value.copy(
                    userFullName = fullName
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al cargar datos del usuario: ${e.message}"
                )
            }
        }
    }

    fun loadActas() {
        viewModelScope.launch {
            actasRepository.getActasByCurrentUser().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            errorMessage = null
                        )
                    }

                    is NetworkResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            actas = result.data ?: emptyList(),
                            errorMessage = null,
                            isRefreshing = false
                        )
                    }

                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message,
                            isRefreshing = false
                        )
                    }
                }
            }
        }
    }

    fun refreshActas() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadActas()
    }

    fun onActaClick(acta: ActaEntity) {
        // TODO: Navegar al detalle del acta
        // Aquí podrías implementar la navegación al fragmento de detalle
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

}