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
        emailUsuario: String,
        // PARÁMETROS DE NUTRICIÓN
        objetivoNutricion: String = "",
        nivelActividad: String = "",
        restricciones: List<String> = emptyList(),
        restriccionesOtras: String = "",
        restriccionesMedicas: String = "",
        // PARÁMETROS DE ENTRENAMIENTO
        objetivoEntrenamiento: String = "",
        experienciaPrevia: String = "",
        disponibilidadSemanal: String = "",
        equipamientoDisponible: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val resultado = repository.enviarSolicitudPlan(
                    profesionalId = profesionalId,
                    tipoPlan = tipoPlan,
                    descripcion = descripcion,
                    nombreUsuario = nombreUsuario,
                    emailUsuario = emailUsuario,
                    // TODOS LOS PARÁMETROS
                    objetivoNutricion = objetivoNutricion,
                    nivelActividad = nivelActividad,
                    restricciones = restricciones,
                    restriccionesOtras = restriccionesOtras,
                    restriccionesMedicas = restriccionesMedicas,
                    objetivoEntrenamiento = objetivoEntrenamiento,
                    experienciaPrevia = experienciaPrevia,
                    disponibilidadSemanal = disponibilidadSemanal,
                    equipamientoDisponible = equipamientoDisponible
                )

                if (resultado) {
                    _mensaje.value = "✅ Solicitud enviada correctamente"
                    cargarSolicitudesUsuario() // Recargar la lista
                } else {
                    _mensaje.value = "❌ Error al enviar la solicitud"
                }
            } catch (e: Exception) {
                _mensaje.value = "❌ Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
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

    // ===== FUNCIONES PARA PLANES NUTRICIONALES =====

    fun activarPlanNutricional(planId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            val activado = repository.activarPlanNutricional(planId)

            if (activado) {
                _mensaje.value = "✅ Plan nutricional activado"
                cargarPlanesUsuario() // Recargar para mostrar el estado actualizado
            } else {
                _mensaje.value = "❌ Error al activar el plan"
            }

            _isLoading.value = false
        }
    }

    fun desactivarPlanNutricional(planId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            val desactivado = repository.desactivarPlanNutricional(planId)

            if (desactivado) {
                _mensaje.value = "✅ Plan nutricional desactivado"
                cargarPlanesUsuario()
            } else {
                _mensaje.value = "❌ Error al desactivar el plan"
            }

            _isLoading.value = false
        }
    }

    fun obtenerPlanNutricionalPorId(planId: String, onComplete: (PlanNutricional?) -> Unit) {
        viewModelScope.launch {
            try {
                val plan = repository.obtenerPlanNutricionalPorId(planId)
                onComplete(plan)
            } catch (e: Exception) {
                _mensaje.value = "❌ Error al obtener plan: ${e.message}"
                onComplete(null)
            }
        }
    }

    // ===== FUNCIONES PARA PLANES DE ENTRENAMIENTO =====

    fun activarPlanEntrenamiento(planId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            val activado = repository.activarPlanEntrenamiento(planId)

            if (activado) {
                _mensaje.value = "✅ Plan de entrenamiento activado"
                cargarPlanesUsuario()
            } else {
                _mensaje.value = "❌ Error al activar el plan"
            }

            _isLoading.value = false
        }
    }

    fun desactivarPlanEntrenamiento(planId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            val desactivado = repository.desactivarPlanEntrenamiento(planId)

            if (desactivado) {
                _mensaje.value = "✅ Plan de entrenamiento desactivado"
                cargarPlanesUsuario()
            } else {
                _mensaje.value = "❌ Error al desactivar el plan"
            }

            _isLoading.value = false
        }
    }

    fun obtenerPlanEntrenamientoPorId(planId: String, onComplete: (PlanEntrenamiento?) -> Unit) {
        viewModelScope.launch {
            try {
                val plan = repository.obtenerPlanEntrenamientoPorId(planId)
                onComplete(plan)
            } catch (e: Exception) {
                _mensaje.value = "❌ Error al obtener plan: ${e.message}"
                onComplete(null)
            }
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

    /**
     * Rechaza una solicitud con un motivo específico
     */
    fun rechazarSolicitud(solicitudId: String, motivoRechazo: String) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val rechazada = repository.rechazarSolicitud(solicitudId, motivoRechazo)
                if (rechazada) {
                    _mensaje.value = "✅ Solicitud rechazada correctamente"
                    cargarSolicitudesProfesional() // Recargar la lista
                } else {
                    _mensaje.value = "❌ Error al rechazar la solicitud"
                }
            } catch (e: Exception) {
                _mensaje.value = "❌ Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Obtiene una solicitud específica por ID
     */
    fun obtenerSolicitudPorId(solicitudId: String, onComplete: (SolicitudPlan?) -> Unit) {
        viewModelScope.launch {
            try {
                val solicitud = repository.obtenerSolicitudPorId(solicitudId)
                onComplete(solicitud)
            } catch (e: Exception) {
                _mensaje.value = "❌ Error al obtener solicitud: ${e.message}"
                onComplete(null)
            }
        }
    }

    // ✅ METODO ACTUALIZADO: Para crear plan nutricional con categorías
    fun crearPlanNutricional(plan: PlanNutricional, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Activar el plan automáticamente
                val planConActivacion = plan.copy(
                    fechaActivacion = System.currentTimeMillis(),
                    estado = EstadoPlan.ACTIVO
                )

                // Usar el repository existente
                val planId = repository.crearPlanNutricional(planConActivacion)

                if (planId != null) {
                    _mensaje.value = "Plan nutricional creado exitosamente"
                    onComplete(true)
                } else {
                    _mensaje.value = "Error al crear el plan nutricional"
                    onComplete(false)
                }

            } catch (e: Exception) {
                _mensaje.value = "Error al crear el plan: ${e.message}"
                onComplete(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ✅ METODO ACTUALIZADO: Para crear plan nutricional desde solicitud (profesionales)
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
        plan: PlanEntrenamiento,
        onSuccess: () -> Unit = {},
        onError: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            val planId = repository.crearPlanEntrenamiento(plan)

            if (planId != null) {
                val completada = repository.marcarSolicitudComoCompletada(solicitudId, planId)
                if (completada) {
                    _mensaje.value = "✅ Plan de entrenamiento creado exitosamente"
                    cargarSolicitudesProfesional()
                    onSuccess()
                } else {
                    _mensaje.value = "⚠️ Plan creado pero error al actualizar solicitud"
                    onSuccess()
                }
            } else {
                _mensaje.value = "❌ Error al crear el plan de entrenamiento"
                onError()
            }

            _isLoading.value = false
        }
    }

    // ✅ NUEVO: Metodo para completar solicitud (usado por la nueva pantalla de categorías)
    fun completarSolicitud(solicitudId: String) {
        viewModelScope.launch {
            try {
                val completada = repository.marcarSolicitudComoCompletada(solicitudId, "")
                if (completada) {
                    cargarSolicitudesProfesional()
                }
            } catch (e: Exception) {
                _mensaje.value = "Error al completar la solicitud: ${e.message}"
            }
        }
    }

    /**
     * Limpia el mensaje actual
     */
    fun limpiarMensaje() {
        _mensaje.value = null
    }
}