package com.coljuegos.sivo.ui.establecimiento.acta

import android.net.Uri
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.coljuegos.sivo.R
import com.coljuegos.sivo.data.entity.ActaEntity
import com.coljuegos.sivo.databinding.FragmentActaVisitaBinding
import com.coljuegos.sivo.ui.base.BaseCameraFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@AndroidEntryPoint
class ActaVisitaFragment : BaseCameraFragment() {

    private var _binding: FragmentActaVisitaBinding? = null

    private val binding get() = _binding!!

    private val viewModel: ActaVisitaViewModel by viewModels()

    private lateinit var municipioAdapter: MunicipioAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActaVisitaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var expanded = false
        binding.textoLegal1Title.setOnClickListener {
            expanded = !expanded
            binding.textoLegal1Title.maxLines = if (expanded) Int.MAX_VALUE else 2
        }
        setupMunicipioSelector()
        setupFormListeners()
        observeViewModel()
    }

    override fun handleCapturedImage(imageUri: Uri) {
        TODO("Not yet implemented")
    }

    private fun setupMunicipioSelector() {
        municipioAdapter = MunicipioAdapter(requireContext(), emptyList())
        binding.municipioExpedicion.setAdapter(municipioAdapter)

        binding.municipioExpedicion.setOnItemClickListener { _, _, position, _ ->
            val selectedMunicipio = municipioAdapter.getItem(position)
            viewModel.selectMunicipio(selectedMunicipio)
        }
    }

    private fun setupFormListeners() {
        binding.nombrePresente.doOnTextChanged { text, _, _, _ ->
            viewModel.updateNombrePresente(text?.toString() ?: "")
        }

        binding.cedulaPresente.doOnTextChanged { text, _, _, _ ->
            viewModel.updateCedulaPresente(text?.toString() ?: "")
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                updateUI(uiState)
            }
        }
    }

    private fun updateUI(uiState: ActaVisitaUiState) {
        // Mostrar/ocultar loading
        if (uiState.isLoading) {
            showLoading()
        } else {
            hideLoading()
        }

        // Mostrar datos del acta
        uiState.acta?.let { acta ->
            populateActaData(acta)
        }

        // Mostrar errores
        uiState.errorMessage?.let { errorMessage ->
            showError(errorMessage)
            viewModel.clearError()
        }
        municipioAdapter.updateData(uiState.municipios)
        uiState.selectedMunicipio?.let { municipio ->
            binding.municipioExpedicion.setText(municipio.displayName, false)
        }
    }

    private fun populateActaData(actaEntity: ActaEntity) {
        with(binding) {
            val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
            val datetimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
            // Información del establecimiento
            nombreEst.text = actaEntity.establecimientoActa.takeIf { it.isNotEmpty() } ?: "N/A"
            departamentoEst.text = actaEntity.departamentoActa.takeIf { it.isNotEmpty() } ?: "N/A"
            municipioEst.text = actaEntity.ciudadActa.takeIf { it.isNotEmpty() } ?: "N/A"
            codigoEst.text = actaEntity.estCodigoInternoActa.takeIf { it.isNotEmpty() } ?: "N/A"
            direccionEst.text = actaEntity.direccionActa.takeIf { it.isNotEmpty() } ?: "N/A"

            // Fecha de inventario
            try {
                fechaEst.text = datetimeFormatter.format(actaEntity.fechaCorteInventarioActa)
            } catch (_: Exception) {
                fechaEst.text = "N/A"
            }

            try {
                fechaVisita.text = datetimeFormatter.format(LocalDateTime.now())
            } catch (_: Exception) {
                fechaVisita.text = "N/A"
            }

            // Información del operador
            nombreOpe.text = actaEntity.nombreOperadorActa.takeIf { it.isNotEmpty() } ?: "N/A"
            nitOpe.text = actaEntity.nitActa.takeIf { it.isNotEmpty() } ?: "N/A"
            emailOpe.text = actaEntity.emailActa.takeIf { it.isNotEmpty() } ?: "N/A"

            // Información del contrato
            acta.text = getString(R.string.acta_visita_ah, actaEntity.numActa.toString())
            auto.text = getString(R.string.acta_visita_ac, actaEntity.numActa.toString())
            autoComisorio.text = actaEntity.numActa.toString()
            numContrato.text = actaEntity.numContratoActa.takeIf { it.isNotEmpty() } ?: "N/A"

            // Fecha fin contrato
            try {
                fechaFin.text = dateFormatter.format(actaEntity.fechaFinContratoActa)
            } catch (_: Exception) {
                fechaFin.text = "N/A"
            }
            try {
                fechaAutoComisorio.text = dateFormatter.format(actaEntity.fechaVisitaAucActa)
            } catch (_: Exception) {
                fechaAutoComisorio.text = "N/A"
            }
        }
    }

    private fun showLoading() {
        // Si tienes un loading indicator, mostrarlo aquí
        binding.constraintLayout.alpha = 0.5f
    }

    private fun hideLoading() {
        binding.constraintLayout.alpha = 1.0f
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Reintentar") {
                viewModel.retry()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}