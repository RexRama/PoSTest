package com.train.ramarai.postest.ui.auth.register

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.train.ramarai.postest.data.model.RoleModel

class RegisterViewModel : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    //LiveData for registration
    private val _registerStatus = MutableLiveData<Boolean>()
    val registerStatus: LiveData<Boolean> get() = _registerStatus

    private val _roles = MutableLiveData<List<RoleModel>>()
    val roles: LiveData<List<RoleModel>> get() = _roles

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val allRoles: MutableList<RoleModel> = mutableListOf()

    init {
        fetchRoles()
    }


    fun registerUser(email: String, password: String, username: String, role: String) {
        _isLoading.value = true

        val currentUserID = firebaseAuth.currentUser?.uid
        if (currentUserID != null) {
            firestore.collection("users").document(currentUserID)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val currentUserRole = document.getString("role")
                        if (currentUserRole == "Admin") {
                            createNewUser(email, password, username, role)
                        } else {
                            _isLoading.value = false
                            _registerStatus.value = false
                        }
                    } else {
                        _isLoading.value = false
                        _registerStatus.value = false
                        _errorMessage.value = "Data pengguna tidak ditemukan!"
                    }
                }
                .addOnFailureListener { e ->
                    _isLoading.value = false
                    _registerStatus.value = false
                    _errorMessage.value = e.message
                }
        } else {
            _isLoading.value = false
            _registerStatus.value = false
            _errorMessage.value = "Pengguna tidak terauntentikasi"
        }

    }

    private fun createNewUser(email: String, password: String, username: String, role: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = firebaseAuth.currentUser?.uid
                    val userMap = hashMapOf(
                        "username" to username,
                        "email" to email,
                        "role" to role
                    )

                    firestore.collection("users").document(userId!!)
                        .set(userMap)
                        .addOnSuccessListener {
                            _isLoading.value = false
                            _registerStatus.value = true
                        }
                        .addOnFailureListener { e ->
                            _isLoading.value = false
                            _registerStatus.value = false
                            _errorMessage.value = e.message
                        }
                } else {
                    _isLoading.value = false
                    _registerStatus.value = false
                    _errorMessage.value = task.exception?.message
                }
            }
    }

    fun fetchRoles() {
        firestore.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                allRoles.clear()
                for (document in documents) {
                    val userRole = RoleModel(
                        userRole = document.getString("role") ?: ""
                    )
                    allRoles.add(userRole)
                }
                _roles.value = allRoles
            }
            .addOnFailureListener { e ->
                Log.w("RegisterViewModel", "Error getting documents: ", e)
            }
    }
}