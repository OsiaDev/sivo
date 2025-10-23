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



    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

}