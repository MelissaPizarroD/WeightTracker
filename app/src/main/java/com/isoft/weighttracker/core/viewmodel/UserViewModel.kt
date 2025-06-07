package com.isoft.weighttracker.shared

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.isoft.weighttracker.core.data.UserRepository
import com.isoft.weighttracker.core.model.PersonaProfile
import com.isoft.weighttracker.core.notifications.recordatorios.AlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LoggedUser(
    val uid: String,
    val email: String,
    val role: String,
    val name: String,
    val photoUrl: String? = null,
    val profesionales: Map<String, String>? = null
)

class UserViewModel : ViewModel() {

    private val userRepo = UserRepository()

    private val _currentUser = MutableStateFlow<LoggedUser?>(null)
    val currentUser: StateFlow<LoggedUser?> = _currentUser

    private val _personaProfile = MutableStateFlow<PersonaProfile?>(null)
    val personaProfile: StateFlow<PersonaProfile?> = _personaProfile

    fun loadUser() {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                userRepo.createUserIfNotExists()
                val base = userRepo.getUser()
                if (base != null) {
                    _currentUser.value = LoggedUser(
                        uid = base.uid,
                        email = base.email,
                        role = base.role,
                        name = base.name,
                        photoUrl = base.photoUrl,
                        profesionales = base.profesionales
                    )
                }
            }
        }
    }

    fun loadPersonaProfile() {
        viewModelScope.launch {
            val profile = userRepo.getPersonaProfile()
            if (profile != null) {
                _personaProfile.value = profile
            }
        }
    }

    fun updatePersonaProfile(profile: PersonaProfile, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val ok = userRepo.updatePersonaProfile(profile)
            if (ok) {
                _personaProfile.value = profile
                onSuccess()
            }
        }
    }

    fun updateFrecuenciaMedicion(nueva: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val actual = _personaProfile.value ?: return@launch
            val actualizado = actual.copy(frecuenciaMedicion = nueva)
            val ok = userRepo.updatePersonaProfile(actualizado)
            if (ok) {
                _personaProfile.value = actualizado
                onSuccess()
            }
        }
    }

    fun updateHoraNotificacion(context: Context, hora: Int, minutos: Int) {
        viewModelScope.launch {
            val actual = _personaProfile.value ?: return@launch
            val actualizado = actual.copy(horaRecordatorio = hora, minutoRecordatorio = minutos)
            val ok = userRepo.updatePersonaProfile(actualizado)
            if (ok) {
                _personaProfile.value = actualizado
            }
        }
    }

    fun cambiarEstadoRecordatorio(context: Context, activo: Boolean) {
        viewModelScope.launch {
            val perfil = _personaProfile.value ?: return@launch
            val actualizado = perfil.copy(recordatorioActivo = activo)
            userRepo.actualizarEstadoRecordatorio(activo)
            _personaProfile.value = actualizado

            if (activo) {
                AlarmScheduler.programarRepeticion(
                    context,
                    perfil.frecuenciaMedicion,
                    perfil.horaRecordatorio ?: 10,
                    perfil.minutoRecordatorio ?: 0
                )
            } else {
                AlarmScheduler.cancelarAlarma(context)
            }
        }
    }

    fun clearUser() {
        _currentUser.value = null
        _personaProfile.value = null
        userRepo.signOut()
    }
}