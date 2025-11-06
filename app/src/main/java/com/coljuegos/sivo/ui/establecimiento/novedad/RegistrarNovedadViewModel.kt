package com.coljuegos.sivo.ui.establecimiento.novedad

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coljuegos.sivo.data.dao.InventarioDao
import com.coljuegos.sivo.data.dao.NovedadRegistradaDao
import com.coljuegos.sivo.data.entity.NovedadRegistradaEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RegistrarNovedadViewModel @Inject constructor(
    private val inventarioDao: InventarioDao,
    private val novedadRegistradaDao: NovedadRegistradaDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val actaUuid: UUID = checkNotNull(savedStateHandle.get<UUID>("actaUuid"))
    private val inventarioUuid: UUID = checkNotNull(savedStateHandle.get<UUID>("inventarioUuid"))
    private val novedadRegistradaUuid: UUID? = savedStateHandle.get<UUID>("novedadRegistradaUuid")

    private val _uiState = MutableStateFlow(RegistrarNovedadUiState())
    val uiState: StateFlow<RegistrarNovedadUiState> = _uiState.asStateFlow()

    fun loadInventario(actaUuid: UUID, inventarioUuid: UUID, novedadRegistradaUuid: UUID?) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                // Obtener el inventario
                val todosInventarios = inventarioDao.getInventariosByActa(actaUuid)
                val inventario = todosInventarios.find { it.uuidInventario == inventarioUuid }

                // Si es edición, obtener el registro de novedad
                val novedad = novedadRegistradaUuid?.let { uuid ->
                    novedadRegistradaDao.getNovedadRegistradaById(uuid)
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        inventario = inventario,
                        novedadRegistrada = novedad,
                        esEdicion = novedad != null,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar inventario: ${e.message}"
                    )
                }
            }
        }
    }

    fun guardarNovedad(
        descripcionNovedad: String,
        tipoNovedad: String,
        observaciones: String?
    ) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                val novedadRegistrada = NovedadRegistradaEntity(
                    uuidNovedadRegistrada = novedadRegistradaUuid ?: UUID.randomUUID(),
                    uuidActa = actaUuid,
                    uuidInventario = inventarioUuid,
                    descripcionNovedad = descripcionNovedad,
                    tipoNovedad = tipoNovedad,
                    observaciones = observaciones
                )

                // Insertar o actualizar
                novedadRegistradaDao.insert(novedadRegistrada)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        guardadoExitoso = true,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        guardadoExitoso = false,
                        errorMessage = "Error al guardar novedad: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

}