package com.coljuegos.sivo.data.repository

import com.coljuegos.sivo.data.remote.api.ApiService
import com.coljuegos.sivo.data.remote.model.LoginRequestDTO
import com.coljuegos.sivo.data.remote.model.LoginResponseDTO
import com.coljuegos.sivo.utils.EncryptionManager
import com.coljuegos.sivo.utils.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val encryptionManager: EncryptionManager
) {

    suspend fun login(username: String, password: String): Flow<NetworkResult<LoginResponseDTO>> = flow {
        try {
            emit(NetworkResult.Loading())

            val loginRequest = LoginRequestDTO(username, password)
            val response = apiService.login(loginRequest)

            emit(handleLoginResponse(response))

        } catch (e: Exception) {
            emit(NetworkResult.Error("Error de conexión: ${e.message}"))
        }
    }

    private fun handleLoginResponse(response: Response<LoginResponseDTO>): NetworkResult<LoginResponseDTO> {
        return when {
            response.isSuccessful -> {
                val loginResponse = response.body()
                if (loginResponse != null) {
                    NetworkResult.Success(loginResponse)
                } else {
                    NetworkResult.Error("Respuesta vacía del servidor")
                }
            }
            response.code() == 401 -> {
                NetworkResult.Error("Credenciales incorrectas")
            }
            response.code() == 403 -> {
                NetworkResult.Error("Acceso denegado")
            }
            response.code() == 500 -> {
                NetworkResult.Error("Error interno del servidor")
            }
            else -> {
                NetworkResult.Error("Error: ${response.code()} - ${response.message()}")
            }
        }
    }

}