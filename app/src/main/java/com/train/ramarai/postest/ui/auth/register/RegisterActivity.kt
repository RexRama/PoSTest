package com.train.ramarai.postest.ui.auth.register

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.train.ramarai.postest.R
import com.train.ramarai.postest.databinding.ActivityRegisterBinding
import com.train.ramarai.postest.utils.DialogUtils
import com.train.ramarai.postest.utils.SettingPreferences
import com.train.ramarai.postest.utils.Utils
import com.train.ramarai.postest.utils.ViewModelFactory
import com.train.ramarai.postest.utils.dataStore


class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var registerViewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupRoleDropDown()
        setupButtonAction()


    }

    private fun setupRoleDropDown() {
        registerViewModel.roles.observe(this) {roleList ->
            val roleNames = roleList.map { it.userRole }.toTypedArray()

            val adapter = ArrayAdapter(this, R.layout.dropdown_item, roleNames)
            binding.registRoleList.setAdapter(adapter)
        }

    }

    private fun setupButtonAction() {
        binding.btRegister.setOnClickListener {
            val username = binding.editRegisterUsername.text.toString().trim()
            val email = binding.editRegistEmail.text.toString().trim()
            val password = binding.editRegistPassword.text.toString().trim()
            val role = binding.registRoleList.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || role.isEmpty()) {
                Toast.makeText(this, "Semua Field Harus Diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerViewModel.registerUser(email, password, username, role)
        }
    }

    private fun setupViewModel() {
        val pref = SettingPreferences.getInstance(dataStore)
        val factory = ViewModelFactory(this, pref)
        registerViewModel = ViewModelProvider(this, factory)[RegisterViewModel::class.java]

        registerViewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }

        registerViewModel.registerStatus.observe(this) { isSuccess ->
            if (isSuccess) {
                DialogUtils(this).dialogSuccess(
                    getString(R.string.registrasi_berhasil),
                    getString(R.string.success_regist_message)
                ) {
                    Utils().redirectToMainActivity(this)
                    finish()
                }
            } else {
                val errorMessage =
                    registerViewModel.errorMessage.value
                DialogUtils(this).dialogError(
                    "Registrasi Gagal",
                    errorMessage
                )
            }
        }

    }

    private fun showLoading(loading: Boolean) {
        binding.pbRegister.visibility = if (loading) View.VISIBLE else View.GONE
    }
}