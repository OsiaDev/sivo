package com.coljuegos.sivo.data.remote.model

import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

data class ActaDTO(
    val numAuc: Int? = null,
    val fechaVisitaAuc: LocalDate? = null,
    val numActa: Int? = null,
    val numContrato: String? = null,
    val nit: String? = null,
    val estCodigo: Long? = null,
    val estCodigoInterno: String? = null,
    val conCodigo: Long? = null,
    val nombreOperador: String? = null,
    val fechaFinContrato: LocalDate? = null,
    val email: String? = null,
    val tipoVisita: String? = null,
    val fechaCorteInventario: LocalDateTime? = null,
    val direccion: DireccionDTO? = null,
    val funcionarios: List<FuncionarioDTO>? = null,
    val inventarios: List<InventarioDTO>? = null
) : Serializable