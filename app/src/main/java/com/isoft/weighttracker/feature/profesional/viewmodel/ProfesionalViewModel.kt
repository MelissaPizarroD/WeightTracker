package com.isoft.weighttracker.feature.profesional.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.isoft.weighttracker.core.data.UserRepository
import com.isoft.weighttracker.core.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfesionalViewModel(
    private val userRepo: UserRepository = UserRepository()
) : ViewModel() {

    private val _usuariosAsociados = MutableStateFlow<List<User>>(emptyList())
    val usuariosAsociados: StateFlow<List<User>> = _usuariosAsociados

    fun cargarUsuariosAsociados(tipo: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        viewModelScope.launch {
            val usuarios = userRepo.getUsuariosAsociados(tipo, uid)
            _usuariosAsociados.value = usuarios
        }
    }
}