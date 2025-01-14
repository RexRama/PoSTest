package com.train.ramarai.postest.utils

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.train.ramarai.postest.ui.auth.login.LoginViewModel
import com.train.ramarai.postest.ui.auth.register.RegisterViewModel
import com.train.ramarai.postest.ui.main.MainViewModel
import com.train.ramarai.postest.ui.main.home.HomeViewModel
import com.train.ramarai.postest.ui.main.income.report.IncomeReportViewmodel
import com.train.ramarai.postest.ui.main.income.IncomeViewmodel
import com.train.ramarai.postest.ui.main.inventory.InventoryViewModel
import com.train.ramarai.postest.ui.main.inventory.add.UpdateInventoryViewmodel
import com.train.ramarai.postest.ui.main.outcome.OutcomeViewmodel
import com.train.ramarai.postest.ui.main.outcome.report.OutcomeReportViewmodel
import com.train.ramarai.postest.ui.main.report.ReportViewmodel
import com.train.ramarai.postest.ui.main.transaction.AddTransactionViewModel

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(
    private val context: Context,
    private val preferences: SettingPreferences
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        when {
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                return RegisterViewModel() as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                return LoginViewModel(preferences) as T
            }
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                return  MainViewModel(preferences) as T
            }
            modelClass.isAssignableFrom(UpdateInventoryViewmodel::class.java) -> {
                return UpdateInventoryViewmodel() as T
            }
            modelClass.isAssignableFrom(InventoryViewModel::class.java) -> {
                return InventoryViewModel() as T
            }
            modelClass.isAssignableFrom(AddTransactionViewModel::class.java) -> {
                return AddTransactionViewModel() as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                return HomeViewModel() as T
            }
            modelClass.isAssignableFrom(IncomeReportViewmodel::class.java) -> {
                return IncomeReportViewmodel() as T
            }
            modelClass.isAssignableFrom(IncomeViewmodel::class.java) -> {
                return IncomeViewmodel() as T
            }
            modelClass.isAssignableFrom(OutcomeReportViewmodel::class.java) -> {
                return OutcomeReportViewmodel() as T
            }
            modelClass.isAssignableFrom(OutcomeViewmodel::class.java) -> {
                return OutcomeViewmodel() as T
            }
            modelClass.isAssignableFrom(ReportViewmodel::class.java) -> {
                return ReportViewmodel() as T
            }

        }

        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }
}