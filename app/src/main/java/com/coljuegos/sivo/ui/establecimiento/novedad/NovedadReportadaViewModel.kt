package com.coljuegos.sivo.ui.establecimiento.novedad

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coljuegos.sivo.data.dao.InventarioDao
import com.coljuegos.sivo.data.dao.NovedadRegistradaDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class NovedadReportadaViewModel @Inject constructor(
    private val inventarioDao: InventarioDao,
    private val novedadRegistradaDao: NovedadRegistradaDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(NovedadReportadaUiState())
    val uiState: StateFlow<NovedadReportadaUiState> = _uiState.asStateFlow()

    fun loadNovedadesRegistradas(actaUuid: UUID) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                // Obtener novedades registradas con Flow
                novedadRegistradaDao.getNovedadesRegistradasByActa(actaUuid).collect { novedades ->
                    // Obtener todos los inventarios del acta
                    val todosInventarios = inventarioDao.getInventariosByActa(actaUuid)

                    // Crear lista de NovedadConRegistro
                    val novedadesConRegistro = novedades.mapNotNull { novedad ->
                        val inventario = todosInventarios.find { it.uuidInventario == novedad.uuidInventario }
                        inventario?.let {
                            NovedadConRegistro(
                                inventario = it,
                                novedad = novedad
                            )
                        }
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            novedadesRegistradas = novedadesConRegistro,
                            totalNovedadesRegistradas = novedadesConRegistro.size,
                            errorMessage = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar novedades: ${e.message}"
                    )
                }
            }
        }
    }

    fun eliminarNovedadRegistrada(uuidNovedadRegistrada: UUID) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                novedadRegistradaDao.deleteById(uuidNovedadRegistrada)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al eliminar novedad: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

}