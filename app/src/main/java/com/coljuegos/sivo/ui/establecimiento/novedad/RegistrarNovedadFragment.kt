package com.coljuegos.sivo.ui.establecimiento.novedad

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.coljuegos.sivo.R
import com.coljuegos.sivo.databinding.FragmentRegistrarNovedadBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegistrarNovedadFragment : Fragment() {

    private var _binding: FragmentRegistrarNovedadBinding? = null

    private val binding get() = _binding!!

    private val args: RegistrarNovedadFragmentArgs by navArgs()

    private val viewModel: RegistrarNovedadViewModel by viewModels()

    private lateinit var adapterOperando: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrarNovedadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()
        setupSpinners()
    }

    private fun setupAdapters() {
        val opcionesOperando = resources.getStringArray(R.array.operando_options)
        adapterOperando = ArrayAdapter(
            requireContext(),
            R.layout.item_dropdown,
            opcionesOperando
        )
    }

    private fun setupSpinners() {
        binding.operandoSpinner.setAdapter(adapterOperando)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}