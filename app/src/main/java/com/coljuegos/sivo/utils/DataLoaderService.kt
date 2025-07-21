package com.coljuegos.sivo.utils

import android.content.Context
import com.coljuegos.sivo.data.dao.DepartamentoDao
import com.coljuegos.sivo.data.dao.MunicipioDao
import com.coljuegos.sivo.data.entity.DepartamentoEntity
import com.coljuegos.sivo.data.entity.MunicipioEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataLoaderService @Inject constructor(
    private val departamentoDao: DepartamentoDao,
    private val municipioDao: MunicipioDao,
    @ApplicationContext private val context: Context
) {

    suspend fun loadLocationData() {
        val json = readJsonFromAssets("lugares.json")
        val lugares = JSONArray(json)

        val departamentosMap = mutableMapOf<String, UUID>()
        val municipios = mutableListOf<MunicipioEntity>()
        val departamentos = mutableListOf<DepartamentoEntity>()

        (0 until lugares.length()).forEach { i ->
            val item = lugares.getJSONObject(i)
            val nombreDep = item.getString("departamento")
            val nombreMun = item.getString("municipio")

            val uuidDep = departamentosMap.getOrPut(nombreDep) {
                val departamentoEntity = DepartamentoEntity(nombreDepartamento = nombreDep)
                departamentos.add(departamentoEntity)
                departamentoEntity.uuidDepartamento
            }

            municipios.add(MunicipioEntity(nombreMunicipio = nombreMun, uuidDepartamento = uuidDep))
        }
        departamentoDao.insertAll(departamentos)
        municipioDao.insertAll(municipios)
    }

    fun readJsonFromAssets(filename: String): String {
        return context.assets.open(filename).bufferedReader().use { it.readText() }
    }

}