package com.coljuegos.sivo.ui.establecimiento.acta

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coljuegos.sivo.data.dao.MunicipioDao
import com.coljuegos.sivo.data.entity.MunicipioDisplayItem
import com.coljuegos.sivo.data.repository.ActasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ActaVisitaViewModel @Inject constructor(
    private val actasRepository: ActasRepository,
    private val municipioDao: MunicipioDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val actaUuid: UUID = checkNotNull(savedStateHandle.get<UUID>("actaUuid"))

    private val _uiState = MutableStateFlow(ActaVisitaUiState())

    val uiState: StateFlow<ActaVisitaUiState> = _uiState.asStateFlow()

    init {
        loadActaDetails()
        loadMunicipios()
    }

    private fun loadActaDetails() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                val acta = actasRepository.getActaByUuid(actaUuid)
                if (acta != null) {
                    val funcionarios = actasRepository.getFuncionariosByActa(actaUuid)
                    val inventarios = actasRepository.getInventariosByActa(actaUuid)

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        acta = acta,
                        funcionarios = funcionarios,
                        inventarios = inventarios,
                        errorMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "No se encontró el acta especificada"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar el acta: ${e.message}"
                )
            }
        }
    }

    private fun loadMunicipios() {
        viewModelScope.launch {
            try {
                val municipios = municipioDao.getAllMunicipiosWithDepartamento()
                _uiState.value = _uiState.value.copy(municipios = municipios)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al cargar municipios: ${e.message}"
                )
            }
        }
    }

    fun selectMunicipio(municipio: MunicipioDisplayItem) {
        _uiState.value = _uiState.value.copy(selectedMunicipio = municipio)
    }

    fun updateNombrePresente(nombre: String) {
        _uiState.value = _uiState.value.copy(nombrePresente = nombre)
    }

    fun updateCedulaPresente(cedula: String) {
        _uiState.value = _uiState.value.copy(cedulaPresente = cedula)
    }

    fun retry() {
        loadActaDetails()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

}