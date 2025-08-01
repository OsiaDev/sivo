package com.coljuegos.sivo.ui.establecimiento.acta

import com.coljuegos.sivo.data.entity.ActaEntity
import com.coljuegos.sivo.data.entity.FuncionarioEntity
import com.coljuegos.sivo.data.entity.InventarioEntity
import com.coljuegos.sivo.data.entity.MunicipioDisplayItem

data class ActaVisitaUiState(
    val isLoading: Boolean = false,
    val acta: ActaEntity? = null,
    val funcionarios: List<FuncionarioEntity> = emptyList(),
    val inventarios: List<InventarioEntity> = emptyList(),
    val errorMessage: String? = null,
    val municipios: List<MunicipioDisplayItem> = emptyList(),
    val selectedMunicipio: MunicipioDisplayItem? = null,
    val nombrePresente: String = "",
    val cedulaPresente: String = "",
)
