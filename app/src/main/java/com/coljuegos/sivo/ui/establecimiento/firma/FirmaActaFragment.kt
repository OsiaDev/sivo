package com.coljuegos.sivo.ui.establecimiento.firma

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.coljuegos.sivo.R
import com.coljuegos.sivo.databinding.FragmentFirmaActaBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FirmaActaFragment : Fragment() {

    private var _binding: FragmentFirmaActaBinding? = null
    private val binding get() = _binding!!

    private val args: FirmaActaFragmentArgs by navArgs()
    private val viewModel: FirmaActaViewModel by viewModels()

    // Para identificar qué firma se está editando
    private var currentSignatureType: SignatureType = SignatureType.PRINCIPAL

    enum class SignatureType {
        PRINCIPAL, SECUNDARIO, OPERADOR
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirmaActaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTextWatchers()
        setupSignatureButtons()
        setupNavigationButtons()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()

        // Configurar listener para el resultado de SignatureFragment
        setFragmentResultListener("signature_request") { _, bundle ->
            val signatureSaved = bundle.getBoolean("signature_saved", false)
            if (signatureSaved) {
                handleSignatureSaved()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Remover el listener cuando el fragment no está visible
        setFragmentResultListener("signature_request") { _, _ -> }
    }

    private fun setupTextWatchers() {
        // Fiscalizador Principal
        binding.nombrePrincipalText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateNombreFiscalizadorPrincipal(text?.toString() ?: "")
        }

        binding.ccPrincipalText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateCcFiscalizadorPrincipal(text?.toString() ?: "")
        }

        binding.cargoPrincipalText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateCargoFiscalizadorPrincipal(text?.toString() ?: "")
        }

        // Fiscalizador Secundario
        binding.nombreSecundarioText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateNombreFiscalizadorSecundario(text?.toString() ?: "")
        }

        binding.ccSecundarioText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateCcFiscalizadorSecundario(text?.toString() ?: "")
        }

        binding.cargoSecundarioText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateCargoFiscalizadorSecundario(text?.toString() ?: "")
        }

        // Operador
        binding.nombreOperadorText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateNombreOperador(text?.toString() ?: "")
        }

        binding.ccOperadorText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateCcOperador(text?.toString() ?: "")
        }

        binding.cargoOperadorText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateCargoOperador(text?.toString() ?: "")
        }
    }

    private fun setupSignatureButtons() {
        // Fiscalizador Principal - Agregar firma
        binding.btnAddSignaturePrincipal.setOnClickListener {
            currentSignatureType = SignatureType.PRINCIPAL
            navigateToSignatureFragment()
        }

        // Fiscalizador Principal - Editar firma
        binding.btnEditSignaturePrincipal.setOnClickListener {
            currentSignatureType = SignatureType.PRINCIPAL
            navigateToSignatureFragment()
        }

        // Fiscalizador Secundario - Agregar firma
        binding.btnAddSignatureSecundario.setOnClickListener {
            currentSignatureType = SignatureType.SECUNDARIO
            navigateToSignatureFragment()
        }

        // Fiscalizador Secundario - Editar firma
        binding.btnEditSignatureSecundario.setOnClickListener {
            currentSignatureType = SignatureType.SECUNDARIO
            navigateToSignatureFragment()
        }

        // Operador - Agregar firma
        binding.btnAddSignatureOperador.setOnClickListener {
            currentSignatureType = SignatureType.OPERADOR
            navigateToSignatureFragment()
        }

        // Operador - Editar firma
        binding.btnEditSignatureOperador.setOnClickListener {
            currentSignatureType = SignatureType.OPERADOR
            navigateToSignatureFragment()
        }
    }

    private fun setupNavigationButtons() {
        binding.btnAnterior.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSiguiente.setOnClickListener {
            viewModel.saveFirmaActa(
                onSuccess = {
                    Snackbar.make(
                        binding.root,
                        "Firmas guardadas exitosamente",
                        Snackbar.LENGTH_SHORT
                    ).show()

                    // Navegar al siguiente fragment o finalizar
                    findNavController().navigateUp()
                },
                onError = { message ->
                    Snackbar.make(
                        binding.root,
                        message,
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            )
        }
    }

    private fun navigateToSignatureFragment() {
        val action = FirmaActaFragmentDirections.actionFirmaActaFragmentToSignatureFragment()
        findNavController().navigate(action)
    }

    private fun handleSignatureSaved() {
        // Obtener la firma guardada del ViewModel de SignatureFragment
        // y guardarla en el ViewModel de FirmaActa
        viewLifecycleOwner.lifecycleScope.launch {
            // Aquí asumimos que SignatureViewModel tiene un StateFlow con la firma
            // Esta es una simplificación, idealmente usarías un SharedViewModel o pasarías la firma como argumento
            val bitmap = when (currentSignatureType) {
                SignatureType.PRINCIPAL -> {
                    // La firma se guardará en el SignatureViewModel compartido
                    // y la recuperaremos aquí
                    Snackbar.make(
                        binding.root,
                        "Firma del fiscalizador principal guardada",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                SignatureType.SECUNDARIO -> {
                    Snackbar.make(
                        binding.root,
                        "Firma del fiscalizador acompañante guardada",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                SignatureType.OPERADOR -> {
                    Snackbar.make(
                        binding.root,
                        "Firma del operador guardada",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }
    }

    private fun updateUI(state: FirmaActaUiState) {
        // Mostrar loading
        // binding.progressBar.isVisible = state.isLoading (si tienes un progress bar)

        // Actualizar campos de texto - Fiscalizador Principal
        if (binding.nombrePrincipalText.text.toString() != state.nombreFiscalizadorPrincipal) {
            binding.nombrePrincipalText.setText(state.nombreFiscalizadorPrincipal)
        }
        if (binding.ccPrincipalText.text.toString() != state.ccFiscalizadorPrincipal) {
            binding.ccPrincipalText.setText(state.ccFiscalizadorPrincipal)
        }
        if (binding.cargoPrincipalText.text.toString() != state.cargoFiscalizadorPrincipal) {
            binding.cargoPrincipalText.setText(state.cargoFiscalizadorPrincipal)
        }

        // Actualizar vista de firma - Fiscalizador Principal
        if (state.firmaFiscalizadorPrincipal != null) {
            binding.layoutSignaturePrincipalPreview.isVisible = true
            binding.btnAddSignaturePrincipal.isVisible = false
            binding.ivSignaturePrincipal.setImageBitmap(state.firmaFiscalizadorPrincipal)
        } else {
            binding.layoutSignaturePrincipalPreview.isVisible = false
            binding.btnAddSignaturePrincipal.isVisible = true
        }

        // Actualizar campos de texto - Fiscalizador Secundario
        if (binding.nombreSecundarioText.text.toString() != state.nombreFiscalizadorSecundario) {
            binding.nombreSecundarioText.setText(state.nombreFiscalizadorSecundario)
        }
        if (binding.ccSecundarioText.text.toString() != state.ccFiscalizadorSecundario) {
            binding.ccSecundarioText.setText(state.ccFiscalizadorSecundario)
        }
        if (binding.cargoSecundarioText.text.toString() != state.cargoFiscalizadorSecundario) {
            binding.cargoSecundarioText.setText(state.cargoFiscalizadorSecundario)
        }

        // Actualizar vista de firma - Fiscalizador Secundario
        if (state.firmaFiscalizadorSecundario != null) {
            binding.layoutSignatureSecundarioPreview.isVisible = true
            binding.btnAddSignatureSecundario.isVisible = false
            binding.ivSignatureSecundario.setImageBitmap(state.firmaFiscalizadorSecundario)
        } else {
            binding.layoutSignatureSecundarioPreview.isVisible = false
            binding.btnAddSignatureSecundario.isVisible = true
        }

        // Actualizar campos de texto - Operador
        if (binding.nombreOperadorText.text.toString() != state.nombreOperador) {
            binding.nombreOperadorText.setText(state.nombreOperador)
        }
        if (binding.ccOperadorText.text.toString() != state.ccOperador) {
            binding.ccOperadorText.setText(state.ccOperador)
        }
        if (binding.cargoOperadorText.text.toString() != state.cargoOperador) {
            binding.cargoOperadorText.setText(state.cargoOperador)
        }

        // Actualizar vista de firma - Operador
        if (state.firmaOperador != null) {
            binding.layoutSignatureOperadorPreview.isVisible = true
            binding.btnAddSignatureOperador.isVisible = false
            binding.ivSignatureOperador.setImageBitmap(state.firmaOperador)
        } else {
            binding.layoutSignatureOperadorPreview.isVisible = false
            binding.btnAddSignatureOperador.isVisible = true
        }

        // Mostrar error si existe
        state.errorMessage?.let { message ->
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}