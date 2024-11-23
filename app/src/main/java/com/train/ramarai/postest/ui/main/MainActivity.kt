package com.train.ramarai.postest.ui.main

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.navigation.NavigationView
import com.train.ramarai.postest.R
import com.train.ramarai.postest.databinding.ActivityMainBinding
import com.train.ramarai.postest.utils.DialogUtils
import com.train.ramarai.postest.utils.SettingPreferences
import com.train.ramarai.postest.utils.Utils
import com.train.ramarai.postest.utils.ViewModelFactory
import com.train.ramarai.postest.utils.dataStore
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel

    private lateinit var preferences: SettingPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navigationView = binding.navigationDrawer

        setupViewModel()
        setupNavigation(navigationView)
        setupButtonAction()

    }

    private fun setupNavigation(navigationView: NavigationView) {
        val headerView = navigationView.getHeaderView(0)
        val tvUsername = headerView.findViewById<TextView>(R.id.tv_drawer_username)
        val tvEmail = headerView.findViewById<TextView>(R.id.tv_drawer_email)
        val tvRole = headerView.findViewById<TextView>(R.id.tv_drawer_roles)
        lifecycleScope.launch {
            preferences.getUserDetails().collect { userDetails ->
                tvUsername.text = userDetails["username"]
                tvEmail.text = userDetails["email"]
                tvRole.text = userDetails["role"]
            }
        }
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.register_account -> {
                    mainViewModel.checkUserRole(this)
                    true
                }

                R.id.logout -> {
                    DialogUtils(this).dialogDefault(
                        "Logout",
                        "Anda yakin ingin LogOut?"
                    ) {
                        mainViewModel.logout()
                        Utils().redirectToLogin(this)
                        finish()
                    }
                    return@setNavigationItemSelectedListener true
                }

                else -> false
            }
        }
    }

    private fun setupButtonAction() {
        binding.btMenu.setOnClickListener {
            binding.main.openDrawer(GravityCompat.START)
        }
    }

    private fun setupViewModel() {
        preferences = SettingPreferences.getInstance(dataStore)
        val pref = preferences
        val factory = ViewModelFactory(this, pref)
        mainViewModel = ViewModelProvider(this, factory)[MainViewModel::class]

    }
}