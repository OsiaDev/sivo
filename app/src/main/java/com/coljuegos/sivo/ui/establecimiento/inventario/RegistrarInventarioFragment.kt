package com.coljuegos.sivo.ui.establecimiento.inventario

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
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

            // Si los contadores están verificados, cargar los valores
            if (registro.contadoresVerificado) {
                showContadoresFields()
                binding.coinInMetEditText.setText(registro.coinInMet ?: "")
                binding.coinOutMetEditText.setText(registro.coinOutMet ?: "")
                binding.jackpotMetEditText.setText(registro.jackpotMet ?: "")
                binding.coinInSclmEditText.setText(registro.coinInSclm ?: "")
                binding.coinOutSclmEditText.setText(registro.coinOutSclm ?: "")
                binding.jackpotSclmEditText.setText(registro.jackpotSclm ?: "")
            }

            binding.observacionesEditText.setText(registro.observaciones ?: "")
        }

        // Si se guardó exitosamente, navegar de vuelta
        if (uiState.guardadoExitoso) {
            Snackbar.make(binding.root, "Inventario guardado exitosamente", Snackbar.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }

        // Mostrar errores
        uiState.errorMessage?.let { errorMessage ->
            Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun guardarInventario() {
        // Validar que el código de apuesta no esté vacío
        val codigoApuesta = binding.codigoApuestaEditText.text?.toString()?.trim()
        if (codigoApuesta.isNullOrEmpty()) {
            Snackbar.make(binding.root, "Por favor ingrese el código de apuesta", Snackbar.LENGTH_SHORT).show()
            return
        }

        // Recopilar datos del formulario
        val codigoApuestaDiferente = binding.codigoApuestaDiferenteCheckbox.isChecked
        val serialVerificado = binding.serialVerificadoCheckbox.isChecked
        val descripcionJuego = binding.descripcionJuegoCheckbox.isChecked
        val planPremios = binding.planPremiosCheckbox.isChecked
        val valorPremios = binding.valorPremiosCheckbox.isChecked
        val valorCredito = binding.valorCreditoEditText.text?.toString()?.trim()
        val contadoresVerificado = binding.contadoresCheckbox.isChecked

        // Datos de contadores (solo si están verificados)
        val coinInMet = if (contadoresVerificado) binding.coinInMetEditText.text?.toString()?.trim() else null
        val coinOutMet = if (contadoresVerificado) binding.coinOutMetEditText.text?.toString()?.trim() else null
        val jackpotMet = if (contadoresVerificado) binding.jackpotMetEditText.text?.toString()?.trim() else null
        val coinInSclm = if (contadoresVerificado) binding.coinInSclmEditText.text?.toString()?.trim() else null
        val coinOutSclm = if (contadoresVerificado) binding.coinOutSclmEditText.text?.toString()?.trim() else null
        val jackpotSclm = if (contadoresVerificado) binding.jackpotSclmEditText.text?.toString()?.trim() else null

        val observaciones = binding.observacionesEditText.text?.toString()?.trim()

        // Guardar en el ViewModel
        viewModel.guardarInventario(
            codigoApuesta = codigoApuesta,
            codigoApuestaDiferente = codigoApuestaDiferente,
            serialVerificado = serialVerificado,
            descripcionJuego = descripcionJuego,
            planPremios = planPremios,
            valorPremios = valorPremios,
            valorCredito = valorCredito,
            contadoresVerificado = contadoresVerificado,
            coinInMet = coinInMet,
            coinOutMet = coinOutMet,
            jackpotMet = jackpotMet,
            coinInSclm = coinInSclm,
            coinOutSclm = coinOutSclm,
            jackpotSclm = jackpotSclm,
            observaciones = observaciones
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}