package com.coljuegos.sivo.ui.login

import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.coljuegos.sivo.databinding.ActivityLoginBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.setupListeners()
    }

    private fun setupListeners() {
        // Listener para el checkbox de mostrar contraseña
        binding.showPassword.setOnCheckedChangeListener { _, isChecked ->
            togglePasswordVisibility(isChecked)
        }

        // Listener para el botón de login
        binding.loginButton.setOnClickListener {
            if (this.validateFields()) {
                this.login()
            }
        }
    }

    private fun togglePasswordVisibility(isVisible: Boolean) {
        if (isVisible) {
            // Mostrar contraseña
            binding.incomePassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            // Ocultar contraseña
            binding.incomePassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        // Mantener el cursor al final del texto
        binding.incomePassword.text?.length?.let { length ->
            binding.incomePassword.setSelection(length)
        }
    }

    private fun validateFields() : Boolean {
        var isValid = true
        val username = binding.incomeUsername.text.toString().trim()
        val password = binding.incomePassword.text.toString().trim()
        if (username.isEmpty()) {
            binding.layoutIncomeUsername.error = "El nombre de usuario es requerido"
            binding.incomeUsername.requestFocus()
            isValid = false
        } else {
            binding.layoutIncomeUsername.error = null
        }
        if (password.isEmpty()) {
            binding.layoutIncomePassword.error = "La contraseña es requerida"
            binding.incomePassword.requestFocus()
            isValid = false
        } else {
            binding.layoutIncomeUsername.error = null
        }
        return isValid
    }

    private fun login() {
        val username = binding.incomeUsername.text.toString().trim()
        val password = binding.incomePassword.text.toString().trim()

        // Limpiar errores previos
        binding.layoutIncomeUsername.error = null
        binding.layoutIncomePassword.error = null

        this.showLoadingOverlay()

    }

    private fun showLoadingOverlay() {
        binding.loadingOverlay.root.visibility = View.VISIBLE
        // Disable interaction with main content
        binding.mainLayout.isEnabled = false
        binding.bottomNavigation.isEnabled = false
    }

}