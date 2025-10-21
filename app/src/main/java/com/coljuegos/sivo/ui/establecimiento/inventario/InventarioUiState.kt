package com.coljuegos.sivo.ui.establecimiento.inventario

import com.coljuegos.sivo.data.entity.InventarioEntity
import java.util.UUID

data class InventarioUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val actaUuid: UUID? = null,
    val inventarios: List<InventarioEntity> = emptyList(),
    val totalInventarios: Int = 0,
    val expandedItems: Set<UUID> = emptySet() // Para controlar qué items están expandidos
)