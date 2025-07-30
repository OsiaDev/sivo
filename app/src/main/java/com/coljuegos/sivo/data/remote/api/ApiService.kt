package com.coljuegos.sivo.data.remote.api

import com.coljuegos.sivo.data.remote.model.ActaResponseDTO
import com.coljuegos.sivo.data.remote.model.LoginRequestDTO
import com.coljuegos.sivo.data.remote.model.LoginResponseDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequestDTO): Response<LoginResponseDTO>

    @GET("acta/obtenerActas")
    suspend fun getActasByUserId(
        @Header("Authorization") authorization: String
    ): Response<ActaResponseDTO>

}