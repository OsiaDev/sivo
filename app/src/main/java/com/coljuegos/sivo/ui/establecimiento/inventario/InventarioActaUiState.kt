package com.coljuegos.sivo.ui.establecimiento.inventario

import com.coljuegos.sivo.data.entity.InventarioEntity

data class InventarioActaUiState(
    val inventarios: List<InventarioEntity> = emptyList(),
    val filteredInventarios: List<InventarioEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = ""
)
