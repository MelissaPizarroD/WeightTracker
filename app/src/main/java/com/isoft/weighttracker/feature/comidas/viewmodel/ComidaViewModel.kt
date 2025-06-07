package com.isoft.weighttracker.feature.comidas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isoft.weighttracker.feature.comidas.model.Comida
import com.isoft.weighttracker.feature.comidas.data.ComidasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ComidaViewModel : ViewModel() {

    private val repository = ComidasRepository()

    private val _comidas = MutableStateFlow<List<Comida>>(emptyList())
    val comidas: StateFlow<List<Comida>> = _comidas

    fun cargarComidas() {
        viewModelScope.launch {
            _comidas.value = repository.obtenerComidas()
        }
    }

    fun registrarNuevaComida(
        comida: Comida,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val nueva = comida.copy(
                fecha = comida.fecha ?: System.currentTimeMillis()
            )

            val ok = repository.registrarComida(nueva)
            if (ok) {
                onSuccess()
                cargarComidas()
            }
        }
    }

    fun actualizarComida(
        comida: Comida,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            if (comida.id != null) {
                val ok = repository.actualizarComida(comida)
                if (ok) {
                    onSuccess()
                    cargarComidas()
                }
            }
        }
    }

    suspend fun eliminarComida(id: String): Boolean {
        return repository.eliminarComida(id).also {
            if (it) cargarComidas()
        }
    }
}