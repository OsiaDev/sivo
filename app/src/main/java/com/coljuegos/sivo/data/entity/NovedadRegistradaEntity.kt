package com.coljuegos.sivo.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "novedades_registradas",
    foreignKeys = [
        ForeignKey(
            entity = ActaEntity::class,
            parentColumns = arrayOf("uuidActa"),
            childColumns = arrayOf("uuidActa"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = InventarioEntity::class,
            parentColumns = arrayOf("uuidInventario"),
            childColumns = arrayOf("uuidInventario"),
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["uuidNovedadRegistrada"]),
        Index(value = ["uuidActa"]),
        Index(value = ["uuidInventario"])
    ]
)
data class NovedadRegistradaEntity(
    @PrimaryKey
    val uuidNovedadRegistrada: UUID = UUID.randomUUID(),
    val uuidActa: UUID,
    val uuidInventario: UUID,

    // Datos del formulario de registro
    val descripcionNovedad: String,
    val tipoNovedad: String, // "TÉCNICA", "OPERATIVA", "ADMINISTRATIVA", "OTRA"
    val observaciones: String? = null
)