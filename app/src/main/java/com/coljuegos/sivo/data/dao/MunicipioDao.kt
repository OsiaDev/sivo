package com.coljuegos.sivo.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.coljuegos.sivo.data.entity.MunicipioEntity

@Dao
interface MunicipioDao {

    @Insert
    suspend fun insert(municipio: MunicipioEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(municipios: Collection<MunicipioEntity>)

}