package com.coljuegos.sivo.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.coljuegos.sivo.data.dao.DepartamentoDao
import com.coljuegos.sivo.data.dao.MunicipioDao
import com.coljuegos.sivo.data.entity.DepartamentoEntity
import com.coljuegos.sivo.data.entity.MunicipioEntity
import com.coljuegos.sivo.utils.UUIDConverter
import com.osiadeveloper.loan.util.BigDecimalConverter
import com.osiadeveloper.loan.util.LocalDateConverter
import com.osiadeveloper.loan.util.LocalDateTimeConverter

@Database(
    entities = [
        DepartamentoEntity::class,
        MunicipioEntity::class
    ], version = 1, exportSchema = false
)
@TypeConverters(
    BigDecimalConverter::class,
    LocalDateConverter::class,
    LocalDateTimeConverter::class,
    UUIDConverter::class
)
abstract class SivoDatabase : RoomDatabase() {

    abstract fun departamentoDao(): DepartamentoDao

    abstract fun municipioDao(): MunicipioDao

}