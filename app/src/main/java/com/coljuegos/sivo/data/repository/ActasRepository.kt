package com.coljuegos.sivo.data.repository

import com.coljuegos.sivo.data.dao.ActaDao
import com.coljuegos.sivo.data.dao.FuncionarioDao
import com.coljuegos.sivo.data.dao.InventarioDao
import com.coljuegos.sivo.data.entity.ActaEntity
import com.coljuegos.sivo.data.entity.ActaStateEnum
import com.coljuegos.sivo.data.entity.FuncionarioEntity
import com.coljuegos.sivo.data.entity.InventarioEntity
import com.coljuegos.sivo.data.remote.api.ApiService
import com.coljuegos.sivo.data.remote.model.ActaResponseDTO
import com.coljuegos.sivo.utils.NetworkResult
import com.coljuegos.sivo.utils.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActasRepository @Inject constructor(
    private val apiService: ApiService,
    private val actaDao: ActaDao,
    private val funcionarioDao: FuncionarioDao,
    private val inventarioDao: InventarioDao,
    private val sessionManager: SessionManager
) {

    fun getActasByCurrentUser(): Flow<NetworkResult<List<ActaEntity>>> = flow {
        try {
            emit(NetworkResult.Loading())

            // Obtener sesión actual
            val currentSession = sessionManager.getCurrentSession()
            if (currentSession == null) {
                emit(NetworkResult.Error("No hay sesión activa"))
                return@flow
            }

            // Primero obtener datos locales activos
            val localActas = actaDao.getActiveActasBySession(currentSession.uuidSession)
            if (localActas.isNotEmpty()) {
                emit(NetworkResult.Success(localActas))
            }

            // Obtener datos del servidor
            val authHeader = sessionManager.getAuthorizationHeader()
            if (authHeader != null) {
                val response = apiService.getActasByUserId(authHeader)

                if (response.isSuccessful) {
                    response.body()?.let { actaResponse ->
                        // Procesar y actualizar datos
                        updateActasWithStateManagement(actaResponse, currentSession.uuidSession)

                        // Emitir los datos actualizados
                        val updatedActas = actaDao.getActiveActasBySession(currentSession.uuidSession)
                        emit(NetworkResult.Success(updatedActas))
                    } ?: emit(NetworkResult.Error("Respuesta vacía del servidor"))
                } else {
                    // Si hay datos locales, los mantenemos aunque falle la actualización
                    if (localActas.isNotEmpty()) {
                        emit(NetworkResult.Success(localActas))
                    } else {
                        emit(NetworkResult.Error("Error al obtener datos: ${response.code()}"))
                    }
                }
            } else {
                emit(NetworkResult.Error("Error de autenticación"))
            }

        } catch (e: Exception) {
            // En caso de error, intentar devolver datos locales
            val currentSession = sessionManager.getCurrentSession()
            if (currentSession != null) {
                val localActas = actaDao.getActiveActasBySession(currentSession.uuidSession)
                if (localActas.isNotEmpty()) {
                    emit(NetworkResult.Success(localActas))
                } else {
                    emit(NetworkResult.Error("Error de conexión: ${e.message}"))
                }
            } else {
                emit(NetworkResult.Error("Error de conexión: ${e.message}"))
            }
        }
    }

    private suspend fun updateActasWithStateManagement(
        actaResponse: ActaResponseDTO,
        sessionId: UUID
    ) {
        val currentTime = LocalDateTime.now()

        // Obtener números de acta que vienen del servidor
        val serverNumActas = actaResponse.actas.mapNotNull { it.numActa }.toSet()

        // Obtener números de acta que ya existen en la base de datos para esta sesión
        val existingNumActas = actaDao.getNumActasBySession(sessionId).toSet()

        // Marcar como inactivas las actas que no vienen en el servidor
        val actasToDeactivate = existingNumActas - serverNumActas
        if (actasToDeactivate.isNotEmpty()) {
            actaDao.updateActasState(
                actasToDeactivate.toList(),
                ActaStateEnum.INACTIVE,
                currentTime
            )

            // Eliminar funcionarios e inventarios de actas inactivas
            funcionarioDao.deleteFuncionariosByNumActas(actasToDeactivate.toList())
            inventarioDao.deleteInventariosByNumActas(actasToDeactivate.toList())
        }

        // Procesar actas del servidor
        val actasToInsert = mutableListOf<ActaEntity>()
        val funcionariosToInsert = mutableListOf<FuncionarioEntity>()
        val inventariosToInsert = mutableListOf<InventarioEntity>()

        actaResponse.actas.forEach { actaDTO ->
            try {
                val actaEntity = mapActaToEntity(actaDTO, sessionId, currentTime)
                actasToInsert.add(actaEntity)

                // Eliminar funcionarios e inventarios existentes para esta acta
                val existingActa = actaDao.getActaByNumActa(actaDTO.numActa ?: 0)
                existingActa?.let {
                    funcionarioDao.deleteFuncionariosByActa(it.uuidActa)
                    inventarioDao.deleteInventariosByActa(it.uuidActa)
                }

                // Mapear funcionarios
                actaDTO.funcionarios?.forEach { funcionarioDTO ->
                    funcionariosToInsert.add(
                        mapFuncionarioToEntity(funcionarioDTO, actaEntity.uuidActa)
                    )
                }

                // Mapear inventarios
                actaDTO.inventarios?.forEach { inventarioDTO ->
                    inventariosToInsert.add(
                        mapInventarioToEntity(inventarioDTO, actaEntity.uuidActa)
                    )
                }

            } catch (e: Exception) {
                // Log error pero continúa con las otras actas
                println("Error mapeando acta ${actaDTO.numActa}: ${e.message}")
            }
        }

        // Insertar todas las actas actualizadas
        if (actasToInsert.isNotEmpty()) {
            actaDao.insertAll(actasToInsert)
        }

        // Insertar funcionarios e inventarios
        if (funcionariosToInsert.isNotEmpty()) {
            funcionarioDao.insertAll(funcionariosToInsert)
        }
        if (inventariosToInsert.isNotEmpty()) {
            inventarioDao.insertAll(inventariosToInsert)
        }

        // Limpiar actas inactivas antiguas (opcional - más de 30 días)
        val cutoffDate = currentTime.minusDays(30)
        actaDao.deleteOldInactiveActas(cutoffDate)
    }

    private fun mapActaToEntity(
        actaDTO: com.coljuegos.sivo.data.remote.model.ActaDTO,
        sessionId: UUID,
        currentTime: LocalDateTime
    ): ActaEntity {
        return ActaEntity(
            uuidSession = sessionId,
            numAucActa = actaDTO.numAuc ?: 0,
            fechaVisitaAucActa = try { LocalDate.parse(actaDTO.fechaVisitaAuc) } catch (_: Exception) { LocalDate.now() },
            numActa = actaDTO.numActa ?: 0,
            numContratoActa = actaDTO.numContrato ?: "",
            nitActa = actaDTO.nit ?: "",
            estCodigoActa = actaDTO.estCodigo?.toInt() ?: 0,
            conCodigoActa = actaDTO.conCodigo?.toInt() ?: 0,
            nombreOperadorActa = actaDTO.nombreOperador ?: "",
            fechaFinContratoActa = try { LocalDate.parse(actaDTO.fechaFinContrato) } catch (_: Exception) { LocalDate.now() },
            emailActa = actaDTO.email ?: "",
            tipoVisitaActa = actaDTO.tipoVisita ?: "Establecimiento",
            fechaCorteInventarioActa = try { LocalDateTime.parse(actaDTO.fechaCorteInventario) } catch (_: Exception) { LocalDateTime.now() },
            direccionActa = actaDTO.direccion?.direccion ?: "",
            establecimientoActa = actaDTO.direccion?.establecimiento ?: "",
            estCodigoInternoActa = actaDTO.direccion?.estCodigo ?: "",
            ciudadActa = actaDTO.direccion?.ciudad ?: "",
            departamentoActa = actaDTO.direccion?.departamento ?: "",
            latitudActa = actaDTO.direccion?.latitud ?: 0.0,
            longitudActa = actaDTO.direccion?.longitud ?: 0.0,
            stateActa = ActaStateEnum.ACTIVE,
            lastUpdatedActa = currentTime
        )
    }

    private fun mapFuncionarioToEntity(
        funcionarioDTO: com.coljuegos.sivo.data.remote.model.FuncionarioDTO,
        actaId: UUID
    ): FuncionarioEntity {
        return FuncionarioEntity(
            uuidActa = actaId,
            idUsuarioFuncionario = funcionarioDTO.idUsuario ?: "",
            nombreFuncionario = funcionarioDTO.nombre ?: "",
            cargoFuncionario = funcionarioDTO.cargo ?: "",
            emailFuncionario = funcionarioDTO.email ?: "",
            identificacionFuncionario = funcionarioDTO.identificacion ?: ""
        )
    }

    private fun mapInventarioToEntity(
        inventarioDTO: com.coljuegos.sivo.data.remote.model.InventarioDTO,
        actaId: UUID
    ): InventarioEntity {
        return InventarioEntity(
            uuidActa = actaId,
            nombreMarcaInventario = inventarioDTO.nombreMarca ?: "",
            metSerialInventario = inventarioDTO.metSerial ?: "",
            insCodigoInventario = inventarioDTO.insCodigo ?: "",
            invSillasInventario = inventarioDTO.invSillas ?: 0,
            tipoApuestaNombreInventario = inventarioDTO.tipoApuestaNombre ?: "",
            metOnlineInventario = inventarioDTO.metOnline ?: false,
            codigoTipoApuestaInventario = inventarioDTO.codigoTipoApuesta ?: "",
            nucInventario = inventarioDTO.nuc ?: "",
            conCodigoInventario = inventarioDTO.conCodigo?.toInt() ?: 0,
            aucNumeroInventario = inventarioDTO.aucNumero ?: 0,
            estCodigoInventario = inventarioDTO.estCodigo?.toInt() ?: 0
        )
    }

}