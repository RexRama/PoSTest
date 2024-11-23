package com.train.ramarai.postest.ui.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.train.ramarai.postest.R
import com.train.ramarai.postest.data.model.ReportItem
import com.train.ramarai.postest.databinding.FragmentHomeBinding
import com.train.ramarai.postest.ui.main.home.adapter.ReportsAdapter
import com.train.ramarai.postest.utils.SettingPreferences
import com.train.ramarai.postest.utils.ViewModelFactory
import com.train.ramarai.postest.utils.dataStore


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController
    private lateinit var preferences: SettingPreferences
    private lateinit var reportsAdapter: ReportsAdapter
    private lateinit var homeViewModel: HomeViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        navController = findNavController()

        setupViewModel()
        homeViewModel.fetchReports()
        setupNavigation(navController)





        return view
    }

    private fun setupViewModel() {
        preferences = SettingPreferences.getInstance(requireContext().dataStore)
        val factory = ViewModelFactory(requireContext(), preferences)
        homeViewModel = ViewModelProvider(this, factory)[HomeViewModel::class]

        homeViewModel.reportItems.observe(viewLifecycleOwner) { items ->
            setupRecyclerView(items)
        }
        homeViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            showLoading(isLoading)
        }
    }

    private fun setupRecyclerView(items: List<ReportItem>) {
        reportsAdapter = ReportsAdapter(items)
        binding.rvNewestTransaction.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = reportsAdapter
        }

    }

    private fun showLoading(loading: Boolean) {
        binding.pbHome.visibility = if (loading) View.VISIBLE else View.GONE

    }

    private fun setupNavigation(navController: NavController) {
        val cvInventory = binding.cvInventory
        val fabAddTransaction = binding.fabAddTransaction
        val cvIncome = binding.cvIncome
        val cvOutcome = binding.cvOutcome
        val fabAddIncome = binding.fabAddIncome
        val fabAddOutcome = binding.fabAddOutcome

        cvInventory.setOnClickListener {
            navController.navigate(R.id.homeFragment_to_inventoryFragment)
        }

        fabAddTransaction.setOnClickListener {
            if (fabAddIncome.visibility == View.VISIBLE && fabAddOutcome.visibility == View.VISIBLE) {
                fabAddIncome.visibility = View.GONE
                fabAddOutcome.visibility = View.GONE
            } else {
                fabAddIncome.visibility = View.VISIBLE
                fabAddOutcome.visibility = View.VISIBLE
            }
        }

        fabAddOutcome.setOnClickListener {
            navController.navigate(R.id.action_nav_home_to_nav_outcome)
        }

        fabAddIncome.setOnClickListener {
            navController.navigate(R.id.action_nav_home_to_nav_income)
        }

        cvIncome.setOnClickListener {
            navController.navigate(R.id.action_nav_home_to_nav_incomeReport)
        }

        cvOutcome.setOnClickListener {
            navController.navigate(R.id.action_nav_home_to_nav_outcomeReport)
        }


    }


}