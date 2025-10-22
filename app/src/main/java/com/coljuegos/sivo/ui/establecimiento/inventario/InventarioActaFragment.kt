package com.coljuegos.sivo.ui.establecimiento.inventario

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.coljuegos.sivo.R
import com.coljuegos.sivo.databinding.FragmentInventarioActaBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InventarioActaFragment : Fragment() {

    private var _binding: FragmentInventarioActaBinding? = null
    private val binding get() = _binding!!

    private val args: InventarioActaFragmentArgs by navArgs()
    private val viewModel: InventarioActaViewModel by viewModels()

    private lateinit var inventarioAdapter: InventarioActaAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventarioActaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        setupButtons()
        observeViewModel()

        // Cargar inventario del acta
        viewModel.loadInventario(args.actaUuid)
    }

    private fun setupRecyclerView() {
        inventarioAdapter = InventarioActaAdapter()
        binding.inventarioRecyclerView.adapter = inventarioAdapter
    }

    private fun setupSearch() {
        binding.searchEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.filterInventario(text?.toString() ?: "")
        }
    }

    private fun setupButtons() {
        binding.btnCerrar.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnGuardar.setOnClickListener {
            // TODO: Implementar lógica de guardado
            showSnackbar("Funcionalidad de guardado próximamente")
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                updateUI(uiState)
            }
        }
    }

    private fun updateUI(uiState: InventarioActaUiState) {
        binding.progressIndicator.visibility = if (uiState.isLoading) View.VISIBLE else View.GONE
        binding.inventarioRecyclerView.visibility = if (uiState.inventarios.isNotEmpty()) View.VISIBLE else View.GONE
        binding.emptyStateLayout.visibility = if (uiState.inventarios.isEmpty() && !uiState.isLoading) View.VISIBLE else View.GONE

        inventarioAdapter.submitList(uiState.inventarios)

        uiState.errorMessage?.let { errorMessage ->
            showError(errorMessage)
            viewModel.clearError()
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}