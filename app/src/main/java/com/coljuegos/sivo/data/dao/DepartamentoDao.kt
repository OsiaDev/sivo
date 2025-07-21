package com.coljuegos.sivo.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.coljuegos.sivo.data.entity.DepartamentoEntity

@Dao
interface DepartamentoDao {

    @Insert
    suspend fun insert(departamento: DepartamentoEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(departamentos: Collection<DepartamentoEntity>)

}