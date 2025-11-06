package com.coljuegos.sivo.ui.establecimiento.novedad

import com.coljuegos.sivo.data.entity.InventarioEntity
import com.coljuegos.sivo.data.entity.NovedadRegistradaEntity

data class NovedadConRegistro(
    val inventario: InventarioEntity,
    val novedad: NovedadRegistradaEntity?
)