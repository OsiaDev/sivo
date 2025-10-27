package com.coljuegos.sivo.ui.establecimiento.inventario

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.coljuegos.sivo.databinding.FragmentRegistrarInventarioBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegistrarInventarioFragment : Fragment() {

    private var _binding: FragmentRegistrarInventarioBinding? = null

    private val binding get() = _binding!!

    private val args: RegistrarInventarioFragmentArgs by navArgs()

    private val viewModel: RegistrarInventarioViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrarInventarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupButtons()
        setupCheckboxListeners()
        observeViewModel()

        // Cargar datos del inventario
        viewModel.loadInventario(args.actaUuid, args.inventarioUuid, args.inventarioRegistradoUuid)
    }

    private fun setupUI() {
        // Ocultar inicialmente los campos de contadores
        hideContadoresFields()
    }

    private fun setupButtons() {
        binding.btnCancelar.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnRegistrar.setOnClickListener {
            guardarInventario()
        }
    }

    private fun setupCheckboxListeners() {
        // Listener para el checkbox de contadores
        binding.contadoresCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showContadoresFields()
            } else {
                hideContadoresFields()
            }
        }
    }

    private fun showContadoresFields() {
        binding.contadoresMetTitle.isVisible = true
        binding.layoutContadoresMet.isVisible = true
        binding.contadoresSclmTitle.isVisible = true
        binding.layoutContadoresSclm.isVisible = true
    }

    private fun hideContadoresFields() {
        binding.contadoresMetTitle.isVisible = false
        binding.layoutContadoresMet.isVisible = false
        binding.contadoresSclmTitle.isVisible = false
        binding.layoutContadoresSclm.isVisible = false
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                updateUI(uiState)
            }
        }
    }

    private fun updateUI(uiState: RegistrarInventarioUiState) {
        // Mostrar/ocultar progress bar
        binding.progressIndicator.isVisible = uiState.isLoading

        // Cargar datos del inventario
        uiState.inventario?.let { inventario ->
            binding.marcaValue.text = inventario.nombreMarcaInventario
            binding.serialValue.text = inventario.metSerialInventario
            binding.codigoApuestaValue.text = inventario.codigoTipoApuestaInventario
            binding.nucValue.text = inventario.nucInventario
        }

        // Si es edición, cargar datos del registro
        uiState.inventarioRegistrado?.let { registro ->
            binding.codigoApuestaEditText.setText(registro.codigoApuesta)
            binding.codigoApuestaDiferenteCheckbox.isChecked = registro.codigoApuestaDiferente
            binding.serialVerificadoCheckbox.isChecked = registro.serialVerificado
            binding.descripcionJuegoCheckbox.isChecked = registro.descripcionJuego
            binding.planPremiosCheckbox.isChecked = registro.planPremios
            binding.valorPremiosCheckbox.isChecked = registro.valorPremios
            binding.valorCreditoEditText.setText(registro.valorCredito ?: "")
            binding.contadoresCheckbox.isChecked = registro.contadoresVerificado

            // Contadores MET
            binding.coinInMetEditText.setText(registro.coinInMet ?: "")
            binding.coinOutMetEditText.setText(registro.coinOutMet ?: "")
            binding.jackpotMetEditText.setText(registro.jackpotMet ?: "")

            // Contadores SCLM
            binding.coinInSclmEditText.setText(registro.coinInSclm ?: "")
            binding.coinOutSclmEditText.setText(registro.coinOutSclm ?: "")
            binding.jackpotSclmEditText.setText(registro.jackpotSclm ?: "")

            // Observaciones
            binding.observacionesEditText.setText(registro.observaciones ?: "")
        }

        // Mostrar éxito y volver
        if (uiState.guardadoExitoso) {
            showSnackbar("Inventario registrado correctamente")
            findNavController().navigateUp()
        }

        // Mostrar errores
        uiState.errorMessage?.let { errorMessage ->
            showError(errorMessage)
            viewModel.clearError()
        }
    }

    private fun guardarInventario() {
        // Recopilar datos del formulario
        val codigoApuesta = binding.codigoApuestaEditText.text.toString()
        val codigoApuestaDiferente = binding.codigoApuestaDiferenteCheckbox.isChecked
        val serialVerificado = binding.serialVerificadoCheckbox.isChecked
        val descripcionJuego = binding.descripcionJuegoCheckbox.isChecked
        val planPremios = binding.planPremiosCheckbox.isChecked
        val valorPremios = binding.valorPremiosCheckbox.isChecked
        val valorCredito = binding.valorCreditoEditText.text.toString()
        val contadoresVerificado = binding.contadoresCheckbox.isChecked

        // Contadores MET
        val coinInMet = binding.coinInMetEditText.text.toString()
        val coinOutMet = binding.coinOutMetEditText.text.toString()
        val jackpotMet = binding.jackpotMetEditText.text.toString()

        // Contadores SCLM
        val coinInSclm = binding.coinInSclmEditText.text.toString()
        val coinOutSclm = binding.coinOutSclmEditText.text.toString()
        val jackpotSclm = binding.jackpotSclmEditText.text.toString()

        // Observaciones
        val observaciones = binding.observacionesEditText.text.toString()

        // Validar que el código de apuesta no esté vacío
        if (codigoApuesta.isEmpty()) {
            showError("El código de apuesta es obligatorio")
            return
        }

        // Guardar el inventario
        viewModel.guardarInventario(
            codigoApuesta = codigoApuesta,
            codigoApuestaDiferente = codigoApuestaDiferente,
            serialVerificado = serialVerificado,
            descripcionJuego = descripcionJuego,
            planPremios = planPremios,
            valorPremios = valorPremios,
            valorCredito = valorCredito.takeIf { it.isNotEmpty() },
            contadoresVerificado = contadoresVerificado,
            coinInMet = coinInMet.takeIf { it.isNotEmpty() },
            coinOutMet = coinOutMet.takeIf { it.isNotEmpty() },
            jackpotMet = jackpotMet.takeIf { it.isNotEmpty() },
            coinInSclm = coinInSclm.takeIf { it.isNotEmpty() },
            coinOutSclm = coinOutSclm.takeIf { it.isNotEmpty() },
            jackpotSclm = jackpotSclm.takeIf { it.isNotEmpty() },
            observaciones = observaciones.takeIf { it.isNotEmpty() }
        )
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