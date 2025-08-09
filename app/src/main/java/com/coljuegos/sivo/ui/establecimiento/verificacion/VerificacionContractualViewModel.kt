package com.coljuegos.sivo.ui.establecimiento.verificacion

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coljuegos.sivo.data.dao.VerificacionContractualDao
import com.coljuegos.sivo.data.entity.VerificacionContractualEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class VerificacionContractualViewModel @Inject constructor(
    private val verificacionContractualDao: VerificacionContractualDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val actaUuid: UUID = checkNotNull(savedStateHandle.get<UUID>("actaUuid"))

    private val _uiState = MutableStateFlow(VerificacionContractualUiState())
    val uiState: StateFlow<VerificacionContractualUiState> = _uiState.asStateFlow()

    init {
        loadVerificacionContractual()
    }

    private fun loadVerificacionContractual() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                val verificacion = verificacionContractualDao.getVerificacionContractualByActaId(actaUuid)

                if (verificacion != null) {
                    val desarrollaActividades = verificacion.desarrollaActividadesDiferentes == "Si"
                    val esTipoOtros = verificacion.tipoActividad == "Otros"

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        actaUuid = actaUuid,
                        avisoAutorizacion = verificacion.avisoAutorizacion ?: "",
                        direccionCorresponde = verificacion.direccionCorresponde ?: "",
                        nombreEstablecimientoCorresponde = verificacion.nombreEstablecimientoCorresponde ?: "",
                        desarrollaActividadesDiferentes = verificacion.desarrollaActividadesDiferentes ?: "",
                        tipoActividad = verificacion.tipoActividad ?: "",
                        especificacionOtros = verificacion.especificacionOtros ?: "",
                        cuentaRegistrosMantenimiento = verificacion.cuentaRegistrosMantenimiento ?: "",
                        mostrarSeccionActividadesDiferentes = desarrollaActividades,
                        mostrarCampoOtros = esTipoOtros
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar verificación contractual: ${e.message}"
                )
            }
        }
    }

    fun updateAvisoAutorizacion(value: String) {
        _uiState.value = _uiState.value.copy(avisoAutorizacion = value)
        saveVerificacionContractual()
    }

    fun updateDireccionCorresponde(value: String) {
        _uiState.value = _uiState.value.copy(direccionCorresponde = value)
        saveVerificacionContractual()
    }

    fun updateNombreEstablecimientoCorresponde(value: String) {
        _uiState.value = _uiState.value.copy(nombreEstablecimientoCorresponde = value)
        saveVerificacionContractual()
    }

    fun updateDesarrollaActividadesDiferentes(value: String) {
        val mostrarSeccion = value == "Si"
        _uiState.value = _uiState.value.copy(
            desarrollaActividadesDiferentes = value,
            mostrarSeccionActividadesDiferentes = mostrarSeccion
        )

        // Si se oculta la sección, limpiar los valores relacionados
        if (!mostrarSeccion) {
            _uiState.value = _uiState.value.copy(
                tipoActividad = "",
                especificacionOtros = "",
                mostrarCampoOtros = false
            )
        }

        saveVerificacionContractual()
    }

    fun updateTipoActividad(value: String) {
        val mostrarOtros = value == "Otros"
        _uiState.value = _uiState.value.copy(
            tipoActividad = value,
            mostrarCampoOtros = mostrarOtros
        )

        // Si no es "Otros", limpiar la especificación
        if (!mostrarOtros) {
            _uiState.value = _uiState.value.copy(especificacionOtros = "")
        }

        saveVerificacionContractual()
    }

    fun updateEspecificacionOtros(value: String) {
        _uiState.value = _uiState.value.copy(especificacionOtros = value)
        saveVerificacionContractual()
    }

    fun updateCuentaRegistrosMantenimiento(value: String) {
        _uiState.value = _uiState.value.copy(cuentaRegistrosMantenimiento = value)
        saveVerificacionContractual()
    }

    private fun saveVerificacionContractual() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value

                // Buscar si ya existe un registro
                val existingVerificacion = verificacionContractualDao.getVerificacionContractualByActaId(actaUuid)

                val verificacionToSave = existingVerificacion?.copy(
                    avisoAutorizacion = currentState.avisoAutorizacion.takeIf { it.isNotBlank() },
                    direccionCorresponde = currentState.direccionCorresponde.takeIf { it.isNotBlank() },
                    nombreEstablecimientoCorresponde = currentState.nombreEstablecimientoCorresponde.takeIf { it.isNotBlank() },
                    desarrollaActividadesDiferentes = currentState.desarrollaActividadesDiferentes.takeIf { it.isNotBlank() },
                    tipoActividad = currentState.tipoActividad.takeIf { it.isNotBlank() },
                    especificacionOtros = currentState.especificacionOtros.takeIf { it.isNotBlank() },
                    cuentaRegistrosMantenimiento = currentState.cuentaRegistrosMantenimiento.takeIf { it.isNotBlank() }
                ) ?: VerificacionContractualEntity(
                    uuidActa = actaUuid,
                    avisoAutorizacion = currentState.avisoAutorizacion.takeIf { it.isNotBlank() },
                    direccionCorresponde = currentState.direccionCorresponde.takeIf { it.isNotBlank() },
                    nombreEstablecimientoCorresponde = currentState.nombreEstablecimientoCorresponde.takeIf { it.isNotBlank() },
                    desarrollaActividadesDiferentes = currentState.desarrollaActividadesDiferentes.takeIf { it.isNotBlank() },
                    tipoActividad = currentState.tipoActividad.takeIf { it.isNotBlank() },
                    especificacionOtros = currentState.especificacionOtros.takeIf { it.isNotBlank() },
                    cuentaRegistrosMantenimiento = currentState.cuentaRegistrosMantenimiento.takeIf { it.isNotBlank() }
                )

                verificacionContractualDao.insert(verificacionToSave)
            } catch (e: Exception) {
                // Error silencioso para no interrumpir la experiencia del usuario
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun retry() {
        loadVerificacionContractual()
    }

}