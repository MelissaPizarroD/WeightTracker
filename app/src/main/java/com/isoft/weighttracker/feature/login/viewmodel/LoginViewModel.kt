package com.isoft.weighttracker.feature.login.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.isoft.weighttracker.core.auth.AuthenticationManager
import com.isoft.weighttracker.core.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val authManager = AuthenticationManager(application.applicationContext)
    private val userRepo = UserRepository()
    private val db = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthenticationManager.AuthResponse?>(null)
    val authState: StateFlow<AuthenticationManager.AuthResponse?> = _authState

    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent

    fun clearNavigation() {
        _navigationEvent.value = null
    }

    fun loginWithGoogle() {
        authManager.signInWithGoogle()
            .onEach { response ->
                _authState.value = response

                if (response is AuthenticationManager.AuthResponse.Success) {
                    viewModelScope.launch {
                        userRepo.createUserIfNotExists()
                        handleUserNavigation()
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    // ✅ SIMPLIFICADO - Solo actualiza role, NO genera ID automáticamente
    fun updateUserRole(role: String) {
        viewModelScope.launch {
            val user = userRepo.getUser() ?: run {
                Log.e("LoginVM", "❌ Usuario no encontrado")
                return@launch
            }

            // Solo actualizar role, el ID se generará cuando complete el perfil profesional
            userRepo.updateRole(role)
            Log.d("LoginVM", "✅ Role actualizado a: $role")

            _navigationEvent.value = NavigationEvent.GoToHome(role)
        }
    }

    fun checkSession() {
        viewModelScope.launch {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            if (firebaseUser != null) {
                userRepo.createUserIfNotExists()
                handleUserNavigation()
            }
        }
    }

    // ✅ SIMPLIFICADO - Solo navega según role, sin generar IDs
    private suspend fun handleUserNavigation() {
        val user = userRepo.getUser() ?: run {
            Log.e("LoginVM", "❌ Usuario no encontrado en handleUserNavigation")
            return
        }

        // Navegar según el estado del usuario
        if (user.role.isNullOrEmpty()) {
            _navigationEvent.value = NavigationEvent.SelectRole
        } else {
            _navigationEvent.value = NavigationEvent.GoToHome(user.role)
        }
    }
}

sealed class NavigationEvent {
    object SelectRole : NavigationEvent()
    data class GoToHome(val role: String) : NavigationEvent()
}
