package com.coljuegos.sivo.ui.establecimiento.resumeninventario

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.coljuegos.sivo.databinding.FragmentResumenInventarioBinding
import com.coljuegos.sivo.ui.common.SignatureViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ResumenInventarioFragment : Fragment() {

    private var _binding: FragmentResumenInventarioBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ResumenInventarioViewModel by viewModels()
    private val signatureViewModel: SignatureViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResumenInventarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupSignatureObserver()
        setupSignature()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()

        // Registrar listener para cuando se guarda la firma
        parentFragmentManager.setFragmentResultListener(
            "signature_request",
            viewLifecycleOwner
        ) { _, bundle ->
            val saved = bundle.getBoolean("signature_saved", false)
            if (saved) {
                // Actualizar la vista con la firma guardada
                signatureViewModel.signatureBitmap.value?.let { bitmap ->
                    updateSignatureUI(bitmap)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Limpiar listener cuando el fragment no es visible
        parentFragmentManager.clearFragmentResultListener("signature_request")
    }

    private fun setupSignatureObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                signatureViewModel.signatureBitmap.collect { bitmap ->
                    updateSignatureUI(bitmap)
                }
            }
        }
    }

    private fun updateSignatureUI(bitmap: android.graphics.Bitmap?) {
        if (bitmap != null) {
            binding.ivSignature.setImageBitmap(bitmap)
            binding.layoutSignaturePreview.isVisible = true
            binding.btnAddSignature.isVisible = false
        } else {
            binding.layoutSignaturePreview.isVisible = false
            binding.btnAddSignature.isVisible = true
        }
    }

    private fun setupSignature() {
        binding.btnAddSignature.setOnClickListener {
            navigateToSignature()
        }

        binding.btnEditSignature.setOnClickListener {
            navigateToSignature()
        }
    }

    private fun navigateToSignature() {
        val action = ResumenInventarioFragmentDirections
            .actionTuFragmentToSignatureFragment()
        findNavController().navigate(action)
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUI(state)
                }
            }
        }
    }

    private fun updateUI(state: ResumenInventarioUiState) {
        // Mostrar/ocultar loading
        binding.progressBar.isVisible = state.isLoading

        // Actualizar estadísticas
        binding.tvInventariosOperandoApagado.text = state.inventariosOperandoApagado.toString()
        binding.tvInventariosNoEncontrados.text = state.inventariosNoEncontrados.toString()
        binding.tvNovedadesSinPlaca.text = state.novedadesSinPlaca.toString()
        binding.tvNovedadesConPlaca.text = state.novedadesConPlaca.toString()
        binding.tvTotalInventariosEncontrados.text = state.totalInventariosEncontrados.toString()
        binding.tvCodigoApuestaDiferente.text = state.codigoApuestaDiferente.toString()

        // Mostrar error si existe
        state.errorMessage?.let { errorMsg ->
            Snackbar.make(binding.root, errorMsg, Snackbar.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    private fun setupListeners() {
        binding.btnAnterior.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSiguiente.setOnClickListener {
            // TODO: Navegar al siguiente fragmento (Verificación Contractual u otro)
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}