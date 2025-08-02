package com.coljuegos.sivo.ui.imagen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coljuegos.sivo.data.dao.ImagenDao
import com.coljuegos.sivo.data.entity.ImagenEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ImagenViewModel @Inject constructor(
    private val imagenDao: ImagenDao
) : ViewModel() {

    private val _imagenes = MutableStateFlow<List<ImagenEntity>>(emptyList())
    val imagenes: StateFlow<List<ImagenEntity>> = _imagenes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadImagenesByActa(uuidActa: UUID) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val imagenesActa = imagenDao.getImagenesByActa(uuidActa)
                _imagenes.value = imagenesActa
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar imágenes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveImagen(uuidActa: UUID, rutaImagen: String, nombreImagen: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Obtener tamaño del archivo
                val file = File(rutaImagen)
                val tamanoBytesImagen = if (file.exists()) file.length() else 0L

                val nuevaImagen = ImagenEntity(
                    uuidActa = uuidActa,
                    rutaImagen = rutaImagen,
                    nombreImagen = nombreImagen,
                    tamanoBytesImagen = tamanoBytesImagen
                )

                imagenDao.insertImagen(nuevaImagen)

                // Recargar las imágenes
                loadImagenesByActa(uuidActa)
            } catch (e: Exception) {
                _errorMessage.value = "Error al guardar imagen: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteImagen(imagen: ImagenEntity) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Eliminar archivo físico
                val file = File(imagen.rutaImagen)
                if (file.exists()) {
                    file.delete()
                }

                // Eliminar de la base de datos
                imagenDao.deleteImagen(imagen)

                // Recargar las imágenes
                loadImagenesByActa(imagen.uuidActa)
            } catch (e: Exception) {
                _errorMessage.value = "Error al eliminar imagen: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

}