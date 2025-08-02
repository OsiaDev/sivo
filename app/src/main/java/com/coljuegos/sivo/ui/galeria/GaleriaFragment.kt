package com.coljuegos.sivo.ui.galeria

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.coljuegos.sivo.databinding.FragmentGaleriaBinding
import com.coljuegos.sivo.ui.base.BaseCameraFragment
import com.coljuegos.sivo.ui.imagen.ImagenViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@AndroidEntryPoint
class GaleriaFragment : BaseCameraFragment() {

    private var _binding: FragmentGaleriaBinding? = null
    private val binding get() = _binding!!

    private val imagenViewModel: ImagenViewModel by viewModels()

    private lateinit var galeriaAdapter: GaleriaAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGaleriaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        loadImagenes()
    }

    private fun setupRecyclerView() {
        galeriaAdapter = GaleriaAdapter(
            onImageClick = { imagen ->
                // TODO: Abrir imagen en pantalla completa
            },
            onDeleteClick = { imagen ->
                imagenViewModel.deleteImagen(imagen)
            }
        )

        binding.recyclerViewImagenes.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = galeriaAdapter
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            imagenViewModel.imagenes.collect { imagenes ->
                galeriaAdapter.submitList(imagenes)

                // Mostrar mensaje si no hay imágenes
                if (imagenes.isEmpty()) {
                    binding.textViewNoImagenes.visibility = View.VISIBLE
                    binding.recyclerViewImagenes.visibility = View.GONE
                } else {
                    binding.textViewNoImagenes.visibility = View.GONE
                    binding.recyclerViewImagenes.visibility = View.VISIBLE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            imagenViewModel.errorMessage.collect { error ->
                error?.let {
                    Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                    imagenViewModel.clearErrorMessage()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            imagenViewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }

    private fun loadImagenes() {
        //val uuidActa = UUID.fromString(args.uuidActa)
        //imagenViewModel.loadImagenesByActa(uuidActa)
    }

    override fun handleCapturedImage(imageUri: Uri) {
        // Copiar imagen a directorio interno de la app
        val fileName = "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.jpg"
        val internalFile = File(requireContext().filesDir, "images/$fileName").apply {
            parentFile?.mkdirs()
        }

        try {
            requireContext().contentResolver.openInputStream(imageUri)?.use { input ->
                internalFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // Guardar referencia en base de datos
            //val uuidActa = UUID.fromString(args.uuidActa)
            //imagenViewModel.saveImagen(uuidActa, internalFile.absolutePath, fileName)

        } catch (e: Exception) {
            Snackbar.make(binding.root, "Error al guardar imagen: ${e.message}", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}