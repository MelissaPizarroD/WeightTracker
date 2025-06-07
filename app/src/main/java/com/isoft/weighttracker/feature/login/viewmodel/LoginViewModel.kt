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

    // ✅ MEJOR SOLUCIÓN: Simple y directa
    fun updateUserRole(role: String) {
        viewModelScope.launch {
            val user = userRepo.getUser() ?: run {
                Log.e("LoginVM", "❌ Usuario no encontrado")
                return@launch
            }

            // Actualizar role siempre
            userRepo.updateRole(role)
            Log.d("LoginVM", "✅ Role actualizado a: $role")

            // Generar ID profesional SOLO si no lo tiene
            if (role in listOf("entrenador", "nutricionista") && user.idProfesional.isNullOrEmpty()) {
                try {
                    val nuevoId = generateUniqueCode()
                    userRepo.setProfessionalId(user.uid, nuevoId)
                    Log.d("LoginVM", "✅ ID profesional generado: $nuevoId para ${user.uid}")
                } catch (e: Exception) {
                    Log.e("LoginVM", "❌ Error generando ID profesional: ${e.message}")
                }
            } else if (role in listOf("entrenador", "nutricionista")) {
                Log.d("LoginVM", "✅ Usuario ya tiene ID profesional: ${user.idProfesional}")
            }

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

    private suspend fun handleUserNavigation() {
        val user = userRepo.getUser() ?: run {
            Log.e("LoginVM", "❌ Usuario no encontrado en handleUserNavigation")
            return
        }

        // Generar ID profesional si falta
        if (user.role in listOf("entrenador", "nutricionista") &&
            user.idProfesional.isNullOrEmpty()) {

            try {
                val nuevoId = generateUniqueCode()
                userRepo.setProfessionalId(user.uid, nuevoId)
                Log.d("LoginVM", "✅ ID profesional generado en navegación: $nuevoId para ${user.uid}")
            } catch (e: Exception) {
                Log.e("LoginVM", "❌ Error generando ID profesional en navegación: ${e.message}")
            }
        }

        // Navegar según el estado del usuario
        if (user.role.isNullOrEmpty()) {
            _navigationEvent.value = NavigationEvent.SelectRole
        } else {
            _navigationEvent.value = NavigationEvent.GoToHome(user.role)
        }
    }

    // ✅ FUNCIÓN PARA GENERAR CÓDIGOS ÚNICOS
    private suspend fun generateUniqueCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        var attempts = 0
        val maxAttempts = 10

        while (attempts < maxAttempts) {
            val code = (1..6).map { chars.random() }.joinToString("")

            // Verificar que el código no exista
            val existing = db.collection("users")
                .whereEqualTo("idProfesional", code)
                .get()
                .await()

            if (existing.isEmpty) {
                Log.d("LoginVM", "🆔 Código único generado: $code")
                return code
            }

            attempts++
            Log.w("LoginVM", "⚠️ Código $code ya existe, intento $attempts/$maxAttempts")
        }

        throw Exception("No se pudo generar código único después de $maxAttempts intentos")
    }
}

sealed class NavigationEvent {
    object SelectRole : NavigationEvent()
    data class GoToHome(val role: String) : NavigationEvent()
}