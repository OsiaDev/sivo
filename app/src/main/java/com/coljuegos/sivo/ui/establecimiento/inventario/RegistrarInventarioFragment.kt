package com.coljuegos.sivo.ui.establecimiento.inventario

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.coljuegos.sivo.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegistrarInventarioFragment : Fragment() {

    companion object {
        fun newInstance() = RegistrarInventarioFragment()
    }

    private val viewModel: RegistrarInventarioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_registrar_inventario, container, false)
    }
}