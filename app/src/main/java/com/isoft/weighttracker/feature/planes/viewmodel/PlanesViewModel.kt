package com.isoft.weighttracker.feature.planes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isoft.weighttracker.feature.planes.data.PlanesRepository
import com.isoft.weighttracker.feature.planes.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlanesViewModel : ViewModel() {

    private val repository = PlanesRepository()

    // Estados para solicitudes
    private val _solicitudesPendientes = MutableStateFlow<List<SolicitudPlan>>(emptyList())
    val solicitudesPendientes: StateFlow<List<SolicitudPlan>> = _solicitudesPendientes

    private val _solicitudesProfesional = MutableStateFlow<List<SolicitudPlan>>(emptyList())
    val solicitudesProfesional: StateFlow<List<SolicitudPlan>> = _solicitudesProfesional

    // Estados para planes
    private val _planesNutricion = MutableStateFlow<List<PlanNutricional>>(emptyList())
    val planesNutricion: StateFlow<List<PlanNutricional>> = _planesNutricion

    private val _planesEntrenamiento = MutableStateFlow<List<PlanEntrenamiento>>(emptyList())
    val planesEntrenamiento: StateFlow<List<PlanEntrenamiento>> = _planesEntrenamiento

    // Estados de UI
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje

    // ===== FUNCIONES PARA USUARIOS =====

    fun enviarSolicitudPlan(
        profesionalId: String,
        tipoPlan: TipoPlan,
        descripcion: String,
        nombreUsuario: String,
        emailUsuario: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            val success = repository.enviarSolicitudPlan(
                profesionalId, tipoPlan, descripcion, nombreUsuario, emailUsuario
            )

            if (success) {
                _mensaje.value = "Solicitud enviada correctamente"
                cargarSolicitudesUsuario()
            } else {
                _mensaje.value = "Error al enviar la solicitud"
            }

            _isLoading.value = false
        }
    }

    fun cargarSolicitudesUsuario() {
        viewModelScope.launch {
            _isLoading.value = true
            val solicitudes = repository.obtenerSolicitudesPendientesUsuario()
            _solicitudesPendientes.value = solicitudes
            _isLoading.value = false
        }
    }

    fun cargarPlanesUsuario() {
        viewModelScope.launch {
            _isLoading.value = true

            val planesNut = repository.obtenerPlanesNutricionUsuario()
            val planesEnt = repository.obtenerPlanesEntrenamientoUsuario()

            _planesNutricion.value = planesNut
            _planesEntrenamiento.value = planesEnt

            _isLoading.value = false
        }
    }

    fun activarPlanNutricional(planId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            val success = repository.activarPlanNutricional(planId)
            if (success) {
                _mensaje.value = "Plan nutricional activado"
                cargarPlanesUsuario()
            } else {
                _mensaje.value = "Error al activar el plan"
            }

            _isLoading.value = false
        }
    }

    fun desactivarPlanNutricional(planId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            val success = repository.desactivarPlanNutricional(planId)
            if (success) {
                _mensaje.value = "Plan nutricional desactivado"
                cargarPlanesUsuario()
            } else {
                _mensaje.value = "Error al desactivar el plan"
            }

            _isLoading.value = false
        }
    }

    fun activarPlanEntrenamiento(planId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            val success = repository.activarPlanEntrenamiento(planId)
            if (success) {
                _mensaje.value = "Plan de entrenamiento activado"
                cargarPlanesUsuario()
            } else {
                _mensaje.value = "Error al activar el plan"
            }

            _isLoading.value = false
        }
    }

    fun desactivarPlanEntrenamiento(planId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            val success = repository.desactivarPlanEntrenamiento(planId)
            if (success) {
                _mensaje.value = "Plan de entrenamiento desactivado"
                cargarPlanesUsuario()
            } else {
                _mensaje.value = "Error al desactivar el plan"
            }

            _isLoading.value = false
        }
    }

    // ===== FUNCIONES PARA PROFESIONALES =====

    fun cargarSolicitudesProfesional() {
        viewModelScope.launch {
            _isLoading.value = true
            val solicitudes = repository.obtenerSolicitudesPendientesProfesional()
            _solicitudesProfesional.value = solicitudes
            _isLoading.value = false
        }
    }

    fun crearPlanNutricional(
        solicitudId: String,
        plan: PlanNutricional
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            val planId = repository.crearPlanNutricional(plan)

            if (planId != null) {
                val completada = repository.marcarSolicitudComoCompletada(solicitudId, planId)
                if (completada) {
                    _mensaje.value = "Plan nutricional creado exitosamente"
                    cargarSolicitudesProfesional()
                } else {
                    _mensaje.value = "Plan creado pero error al actualizar solicitud"
                }
            } else {
                _mensaje.value = "Error al crear el plan nutricional"
            }

            _isLoading.value = false
        }
    }

    fun crearPlanEntrenamiento(
        solicitudId: String,
        plan: PlanEntrenamiento
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            val planId = repository.crearPlanEntrenamiento(plan)

            if (planId != null) {
                val completada = repository.marcarSolicitudComoCompletada(solicitudId, planId)
                if (completada) {
                    _mensaje.value = "Plan de entrenamiento creado exitosamente"
                    cargarSolicitudesProfesional()
                } else {
                    _mensaje.value = "Plan creado pero error al actualizar solicitud"
                }
            } else {
                _mensaje.value = "Error al crear el plan de entrenamiento"
            }

            _isLoading.value = false
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }
}