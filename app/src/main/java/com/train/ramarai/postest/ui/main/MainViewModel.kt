package com.train.ramarai.postest.ui.main

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.train.ramarai.postest.utils.SettingPreferences
import com.train.ramarai.postest.utils.Utils
import kotlinx.coroutines.launch


class MainViewModel(private val preferences: SettingPreferences) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun checkUserRole(context: Context) {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            db.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val role = document.getString("role") ?: "Unknown User"

                        if (role == "Admin") {
                            Utils().toRegister(context)
                        } else {
                            Toast.makeText(
                                context,
                                "Hanya Admin yang bisa menambahkan akun!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("MainViewmodel", "Error fetching user data", e)
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            preferences.clearLoginSession()
        }
    }
}