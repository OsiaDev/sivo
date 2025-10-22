package com.coljuegos.sivo.ui.establecimiento.inventario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coljuegos.sivo.data.dao.InventarioDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class InventarioActaViewModel @Inject constructor(
    private val inventarioDao: InventarioDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventarioActaUiState())
    val uiState: StateFlow<InventarioActaUiState> = _uiState.asStateFlow()

    fun loadInventario(actaUuid: UUID) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                inventarioDao.getInventarioByActa(actaUuid).collect { inventarios ->
                    _uiState.update {
                        it.copy(
                            inventarios = inventarios,
                            filteredInventarios = inventarios,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar inventario: ${e.message}"
                    )
                }
            }
        }
    }

    fun filterInventario(query: String) {
        _uiState.update { currentState ->
            val filtered = if (query.isBlank()) {
                currentState.inventarios
            } else {
                currentState.inventarios.filter { inventario ->
                    inventario.metSerialInventario.contains(query, ignoreCase = true) ||
                            inventario.marcaInventario.contains(query, ignoreCase = true)
                }
            }

            currentState.copy(
                searchQuery = query,
                filteredInventarios = filtered
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

}