package com.coljuegos.sivo.ui.establecimiento.resumen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coljuegos.sivo.data.entity.ActaStateEnum
import com.coljuegos.sivo.data.repository.ActaSincronizacionRepository
import com.coljuegos.sivo.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ResumenActaViewModel @Inject constructor(
    private val actaSincronizacionRepository: ActaSincronizacionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResumenActaUiState())
    val uiState: StateFlow<ResumenActaUiState> = _uiState.asStateFlow()

    fun loadActa(actaUuid: UUID) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val acta = actaSincronizacionRepository.getActaByUuid(actaUuid)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    acta = acta,
                    estadoActa = acta?.stateActa ?: ActaStateEnum.ACTIVE,
                    latitud = acta?.latitudActa ?: 0.0,
                    longitud = acta?.longitudActa ?: 0.0
                )
            }
        }
    }

    fun actualizarUbicacion(latitud: Double, longitud: Double) {
        _uiState.update {
            it.copy(
                latitud = latitud,
                longitud = longitud,
                ubicacionObtenida = true
            )
        }
    }

    fun finalizarActa(actaUuid: UUID) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = actaSincronizacionRepository.marcarActaComoCompleta(
                actaUuid = actaUuid,
                latitud = _uiState.value.latitud,
                longitud = _uiState.value.longitud
            )

            when (result) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            estadoActa = ActaStateEnum.COMPLETE,
                            acta = result.data,
                            successMessage = "Acta finalizada correctamente"
                        )
                    }

                    // Intentar sincronizar automáticamente
                    sincronizarActa(actaUuid)
                }

                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }

                is NetworkResult.Loading -> {
                    // No hacer nada
                }
            }
        }
    }

    fun sincronizarActa(actaUuid: UUID) {
        viewModelScope.launch {
            actaSincronizacionRepository.sincronizarActaConBackend(actaUuid).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _uiState.update {
                            it.copy(
                                isSincronizando = true,
                                errorMessage = null
                            )
                        }
                    }

                    is NetworkResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isSincronizando = false,
                                estadoActa = ActaStateEnum.SINCRONIZADO,
                                successMessage = result.data
                            )
                        }
                    }

                    is NetworkResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isSincronizando = false,
                                errorMessage = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                successMessage = null
            )
        }
    }

}