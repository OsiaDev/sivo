package com.coljuegos.sivo.ui.common

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.coljuegos.sivo.R
import com.coljuegos.sivo.databinding.FragmentSignatureBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignatureFragment : Fragment() {

    private var _binding: FragmentSignatureBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SignatureViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignatureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBackPressHandler()
        setupButtons()
        loadExistingSignature()
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    handleCancelAction()
                }
            }
        )
    }

    private fun setupButtons() {
        binding.btnGuardar.setOnClickListener {
            if (binding.signatureView.hasSignature()) {
                val bitmap = binding.signatureView.getSignatureBitmap()
                viewModel.saveSignature(bitmap)

                // Enviar resultado al fragment anterior
                setFragmentResult(
                    SIGNATURE_REQUEST_KEY,
                    Bundle().apply {
                        putBoolean(SIGNATURE_SAVED_KEY, true)
                    }
                )

                findNavController().navigateUp()
            } else {
                Snackbar.make(
                    binding.root,
                    "R.string.signature_empty_error",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

        binding.btnReiniciar.setOnClickListener {
            if (binding.signatureView.hasSignature()) {
                AlertDialog.Builder(requireContext())
                    .setTitle("R.string.signature_clear_title")
                    .setMessage("R.string.signature_clear_message")
                    .setPositiveButton("R.string.signature_clear_confirm") { _, _ ->
                        binding.signatureView.clear()
                    }
                    .setNegativeButton("R.string.signature_clear_cancel", null)
                    .show()
            }
        }

        binding.btnCancelar.setOnClickListener {
            handleCancelAction()
        }
    }

    private fun handleCancelAction() {
        if (binding.signatureView.hasSignature()) {
            AlertDialog.Builder(requireContext())
                .setTitle("R.string.signature_cancel_title")
                .setMessage("R.string.signature_cancel_message")
                .setPositiveButton("R.string.signature_cancel_confirm") { _, _ ->
                    findNavController().navigateUp()
                }
                .setNegativeButton("R.string.signature_cancel_no", null)
                .show()
        } else {
            findNavController().navigateUp()
        }
    }

    private fun loadExistingSignature() {
        viewModel.signatureBitmap.value?.let { bitmap ->
            binding.signatureView.setSignatureBitmap(bitmap)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val SIGNATURE_REQUEST_KEY = "signature_request"
        const val SIGNATURE_SAVED_KEY = "signature_saved"
    }

}