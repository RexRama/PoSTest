package com.train.ramarai.postest.ui.auth.login

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.train.ramarai.postest.databinding.ActivityLoginBinding
import com.train.ramarai.postest.utils.DialogUtils
import com.train.ramarai.postest.utils.SettingPreferences
import com.train.ramarai.postest.utils.Utils
import com.train.ramarai.postest.utils.ViewModelFactory
import com.train.ramarai.postest.utils.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var preferences: SettingPreferences

    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = SettingPreferences.getInstance(dataStore)
        setupViewModel()
        setupButtonAction()

        lifecycleScope.launch {
            val isLoggedIn = preferences.isUserLoggedIn().first()
            val isSessionExpired = preferences.isSessionExpired().first()

            if (isLoggedIn && !isSessionExpired) {
                Utils().redirectToMainActivity(this@LoginActivity)
                finish()
            } else if (isSessionExpired) {
                preferences.clearLoginSession()
                Toast.makeText(
                    this@LoginActivity,
                    "Sesi berakhir, Harap masuk kembali",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    private fun setupButtonAction() {
        binding.btLogin.setOnClickListener {
            val email = binding.editLoginEmail.text.toString().trim()
            val password = binding.editLoginPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan Password harus diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginViewModel.loginUser(email,password)
        }
    }

    private fun setupViewModel() {
        val factory = ViewModelFactory(this, preferences)
        loginViewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]

        loginViewModel.isLoading.observe(this) {isLoading ->
            showLoading(isLoading)
        }

        loginViewModel.loginStatus.observe(this) { isSuccess ->
            if (isSuccess) {
                lifecycleScope.launch {
                    preferences.setLoginSession()
                }
                DialogUtils(this).dialogSuccess(
                    "Login Berhasil",
                    "Anda berhasil masuk"
                ) {
                    Utils().redirectToMainActivity(this@LoginActivity)
                    finish()
                }
            } else {
                DialogUtils(this).dialogError(
                    "Login Gagal",
                    loginViewModel.errorMessage.value
                )
            }
        }
    }

    private fun showLoading(loading: Boolean) {
        binding.pbLogin.visibility = if (loading) View.VISIBLE else View.GONE
    }
}