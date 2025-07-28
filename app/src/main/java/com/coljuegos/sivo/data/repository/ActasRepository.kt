package com.coljuegos.sivo.data.repository

import com.coljuegos.sivo.data.remote.api.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActasRepository @Inject constructor(
    private val apiService: ApiService
) {
}