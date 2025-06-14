package com.isoft.weighttracker.feature.login.viewmodel

import android.app.Application
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.isoft.weighttracker.core.auth.AuthenticationManager
import com.isoft.weighttracker.core.data.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class LoginViewModel(
    application: Application,
    private val activity: ComponentActivity
) : AndroidViewModel(application) {

    private val authManager = AuthenticationManager(application.applicationContext, activity)
    private val userRepo = UserRepository()

    private val _authState = MutableStateFlow<AuthenticationManager.AuthResponse?>(null)
    val authState: StateFlow<AuthenticationManager.AuthResponse?> = _authState

    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent

    // ✅ Flag para prevenir navegaciones duplicadas
    private var navigationInProgress = false

    fun clearNavigation() {
        _navigationEvent.value = null
        navigationInProgress = false // ✅ Reset del flag
    }

    fun loginWithGoogle() {
        Log.d("LoginVM", "🔄 Iniciando login con Google...")
        authManager.signInWithGoogle()
            .onEach { response ->
                _authState.value = response
                Log.d("LoginVM", "📱 Estado de auth: $response")

                if (response is AuthenticationManager.AuthResponse.Success) {
                    Log.d("LoginVM", "✅ Login exitoso, creando/verificando usuario...")
                    viewModelScope.launch {
                        // Pequeño delay para asegurar que Firebase esté listo
                        delay(500)

                        val userCreated = userRepo.createUserIfNotExists()
                        Log.d("LoginVM", "👤 Usuario creado/verificado: $userCreated")

                        // Otro pequeño delay para asegurar la sincronización
                        delay(300)

                        handleUserNavigation()
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun updateUserRole(role: String) {
        viewModelScope.launch {
            val user = userRepo.getUser() ?: run {
                Log.e("LoginVM", "❌ Usuario no encontrado")
                return@launch
            }

            userRepo.updateRole(role)
            Log.d("LoginVM", "✅ Role actualizado a: $role")

            _navigationEvent.value = NavigationEvent.GoToHome(role)
        }
    }

    fun checkSession() {
        viewModelScope.launch {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            if (firebaseUser != null) {
                Log.d("LoginVM", "🔄 Verificando sesión existente para: ${firebaseUser.email}")
                userRepo.createUserIfNotExists()
                delay(300) // Pequeño delay para sincronización
                handleUserNavigation()
            }
        }
    }

    private suspend fun handleUserNavigation() {
        // ✅ Prevenir ejecuciones duplicadas
        if (navigationInProgress) {
            Log.d("LoginVM", "⏭️ Navegación ya en progreso, omitiendo...")
            return
        }

        navigationInProgress = true
        Log.d("LoginVM", "🧭 Iniciando navegación...")

        val user = userRepo.getUser()

        if (user == null) {
            Log.e("LoginVM", "❌ Usuario no encontrado en Firestore después de login")
            navigationInProgress = false
            return
        }

        Log.d("LoginVM", "👤 Usuario encontrado:")
        Log.d("LoginVM", "   - UID: ${user.uid}")
        Log.d("LoginVM", "   - Email: ${user.email}")
        Log.d("LoginVM", "   - Name: ${user.name}")
        Log.d("LoginVM", "   - Role: '${user.role}' (isEmpty: ${user.role.isEmpty()})")

        // Verificar si el role está vacío o nulo
        if (user.role.isNullOrEmpty()) {
            Log.d("LoginVM", "🎭 Role vacío, navegando a SelectRole")
            _navigationEvent.value = NavigationEvent.SelectRole
        } else {
            Log.d("LoginVM", "🏠 Role encontrado: ${user.role}, navegando a Home")
            _navigationEvent.value = NavigationEvent.GoToHome(user.role)
        }
    }
}

sealed class NavigationEvent {
    data object SelectRole : NavigationEvent()
    data class GoToHome(val role: String) : NavigationEvent()
}