package com.coljuegos.sivo.ui.establecimiento.inventario

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.coljuegos.sivo.R
import com.coljuegos.sivo.data.entity.InventarioEntity
import com.coljuegos.sivo.databinding.FragmentInventarioReportadoBinding
import com.coljuegos.sivo.ui.establecimiento.verificacion.VerificacionContractualFragmentDirections
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InventarioReportadoFragment : Fragment() {

    private var _binding: FragmentInventarioReportadoBinding? = null

    private val binding get() = _binding!!

    private val args: InventarioReportadoFragmentArgs by navArgs()

    private val viewModel: InventarioReportadoViewModel by viewModels()

    private lateinit var inventarioAdapter: InventarioReportadoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("InventarioFragment", "Registrando listener")

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventarioReportadoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupButtons()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        // Registrar listener cuando el fragment es visible
        parentFragmentManager.setFragmentResultListener("camera_action", viewLifecycleOwner) { _, _ ->
            Log.d("ActaVisitaFragment", "Recibido evento de cámara")
            navigateToGallery()
        }
    }

    private fun setupRecyclerView() {
        inventarioAdapter = InventarioReportadoAdapter(
            onItemClick = { inventario ->
                onInventarioSelected(inventario)
            },
            onExpandClick = { inventarioUuid ->
                viewModel.toggleItemExpanded(inventarioUuid)
            },
            isItemExpanded = { inventarioUuid ->
                viewModel.uiState.value.expandedItems.contains(inventarioUuid)
            }
        )

        //binding.inventarioRecyclerView.adapter = inventarioAdapter
    }

    private fun setupButtons() {
        binding.btnAnterior.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSiguiente.setOnClickListener {
            // Navegar al siguiente fragment
            // TODO: Implementar navegación cuando se cree el siguiente fragment
            showSnackbar("Funcionalidad próximamente disponible")
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                updateUI(uiState)
            }
        }
    }

    private fun updateUI(uiState: InventarioUiState) {
        // Mostrar/ocultar loading
        //binding.progressIndicator.isVisible = uiState.isLoading

        // Actualizar total
        //binding.totalText.text = getString(R.string.inventario_total, uiState.totalInventarios)

        // Actualizar lista
        inventarioAdapter.submitList(uiState.inventarios)

        // Mostrar empty state si no hay inventarios
        //binding.emptyStateLayout.isVisible = uiState.inventarios.isEmpty() && !uiState.isLoading
        //binding.inventarioRecyclerView.isVisible = uiState.inventarios.isNotEmpty()

        // Mostrar errores
        uiState.errorMessage?.let { errorMessage ->
            showError(errorMessage)
            viewModel.clearError()
        }
    }

    private fun onInventarioSelected(inventario: InventarioEntity) {
        // Aquí navegarías a la pantalla de edición/verificación del inventario
        // Por ahora solo mostramos un mensaje
        showSnackbar("Seleccionado: ${inventario.metSerialInventario}")

        // TODO: Implementar navegación a InventarioEditFragment cuando se cree
        // val action = InventarioFragmentDirections
        //     .actionInventarioFragmentToInventarioEditFragment(
        //         args.actaUuid,
        //         inventario.uuidInventario,
        //         inventario.metSerialInventario,
        //         inventario.codigoTipoApuestaInventario
        //     )
        // findNavController().navigate(action)
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Reintentar") {
                viewModel.retry()
            }
            .show()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun navigateToGallery() {
        val currentState = viewModel.uiState.value
        currentState.actaUuid?.let { acta ->
            val action = InventarioReportadoFragmentDirections.actionInventarioFragmentToGalleryFragment(acta, "inventario_reportado")
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}