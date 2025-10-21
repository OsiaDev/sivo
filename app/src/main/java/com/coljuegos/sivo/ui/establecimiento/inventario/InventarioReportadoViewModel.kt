package com.coljuegos.sivo.ui.establecimiento.inventario

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coljuegos.sivo.data.dao.InventarioDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class InventarioReportadoViewModel @Inject constructor(
    private val inventarioDao: InventarioDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val actaUuid: UUID = checkNotNull(savedStateHandle.get<UUID>("actaUuid"))

    private val _uiState = MutableStateFlow(InventarioUiState())
    val uiState: StateFlow<InventarioUiState> = _uiState.asStateFlow()

    init {
        loadInventarios()
    }

    private fun loadInventarios() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                val inventarios = inventarioDao.getInventariosByActa(actaUuid)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    actaUuid = actaUuid,
                    inventarios = inventarios,
                    totalInventarios = inventarios.size,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar inventarios: ${e.message}"
                )
            }
        }
    }

    fun toggleItemExpanded(inventarioUuid: UUID) {
        val currentExpanded = _uiState.value.expandedItems
        val newExpanded = if (currentExpanded.contains(inventarioUuid)) {
            currentExpanded - inventarioUuid
        } else {
            currentExpanded + inventarioUuid
        }
        _uiState.value = _uiState.value.copy(expandedItems = newExpanded)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun retry() {
        loadInventarios()
    }

}