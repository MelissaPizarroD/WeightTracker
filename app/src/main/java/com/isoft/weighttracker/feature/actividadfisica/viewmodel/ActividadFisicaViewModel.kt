package com.isoft.weighttracker.feature.actividadfisica.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.isoft.weighttracker.MainActivity
import com.isoft.weighttracker.core.data.UserRepository
import com.isoft.weighttracker.feature.actividadfisica.data.*
import com.isoft.weighttracker.feature.actividadfisica.model.ActividadFisica
import com.isoft.weighttracker.feature.actividadfisica.model.RegistroPasos
import com.isoft.weighttracker.feature.actividadfisica.sensorPasos.service.PasoSensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

class ActividadFisicaViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val repository = ActividadFisicaRepository()
    private val userRepo = UserRepository()

    // StateFlows para actividades físicas
    private val _actividades = MutableStateFlow<List<ActividadFisica>>(emptyList())
    val actividades: StateFlow<List<ActividadFisica>> = _actividades

    // StateFlows para pasos
    private val _pasos = MutableStateFlow(0)
    val pasos: StateFlow<Int> = _pasos

    private val _historialPasos = MutableStateFlow<List<RegistroPasos>>(emptyList())
    val historialPasos: StateFlow<List<RegistroPasos>> = _historialPasos

    private val _contadorPasosActivo = MutableStateFlow(false)
    val contadorPasosActivo: StateFlow<Boolean> = _contadorPasosActivo

    private val _pasosSincronizados = MutableStateFlow(true)
    val pasosSincronizados: StateFlow<Boolean> = _pasosSincronizados

    private val _sensorDisponible = MutableStateFlow(true)
    val sensorDisponible: StateFlow<Boolean> = _sensorDisponible

    // StateFlow para errores
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Manejo de sensores
    private var pasoSensorManager: PasoSensorManager? = null
    private var ultimoValorGuardado = -1

    init {
        prepararContadorSiEsNecesario()
    }

    // ===== FUNCIONES PARA PASOS =====

    fun recargarEstadoContador() {
        prepararContadorSiEsNecesario()
    }

    fun prepararContadorSiEsNecesario() {
        viewModelScope.launch {
            try {
                val perfil = userRepo.getPersonaProfile()
                val activoRemoto = perfil?.contadorPasosActivo == true
                val tienePermiso = MainActivity.hasActivityRecognitionPermission(context)

                // Solo activar localmente si tenemos permiso, independientemente del estado remoto
                _contadorPasosActivo.value = activoRemoto && tienePermiso

                // Si está activo remoto pero no tenemos permiso, corregir en Firestore
                if (activoRemoto && !tienePermiso) {
                    userRepo.updatePersonaProfile(perfil.copy(contadorPasosActivo = false))
                }

                if (tienePermiso && activoRemoto) {
                    verificarYActivarSensor()
                }
            } catch (e: Exception) {
                Log.e("PasosVM", "Error al preparar contador", e)
                _contadorPasosActivo.value = false
                _error.value = "Error al obtener configuración remota"
            }
        }
    }

    fun toggleContadorPasos(activo: Boolean, onPermissionDenied: () -> Unit = {}) {
        if (activo && !MainActivity.hasActivityRecognitionPermission(context)) {
            onPermissionDenied()
            return
        }

        viewModelScope.launch {
            try {
                // Actualizar estado local primero para feedback inmediato
                _contadorPasosActivo.value = activo

                // DETENER INMEDIATAMENTE si se está desactivando
                if (!activo) {
                    detenerSensorPasos()
                }

                val perfil = userRepo.getPersonaProfile()
                if (perfil != null) {
                    val resultado = userRepo.updatePersonaProfile(perfil.copy(contadorPasosActivo = activo))

                    if (resultado) {
                        if (activo) {
                            iniciarSensorPasos()
                        } else {
                            // Ya se detuvo arriba, pero asegurar que esté detenido
                            detenerSensorPasos()
                        }
                    } else {
                        // Revertir estado local si falla
                        _contadorPasosActivo.value = !activo
                        // Si falló y era desactivación, reactivar sensor
                        if (!activo) {
                            iniciarSensorPasos()
                        }
                        _error.value = "Error al guardar configuración"
                    }
                } else {
                    // Si no hay perfil, revertir estado
                    _contadorPasosActivo.value = !activo
                    if (!activo) {
                        iniciarSensorPasos()
                    }
                    _error.value = "Error al obtener perfil de usuario"
                }
            } catch (e: Exception) {
                Log.e("PasosVM", "Error al actualizar estado", e)
                _contadorPasosActivo.value = !activo
                // Revertir estado del sensor también
                if (!activo) {
                    iniciarSensorPasos()
                } else {
                    detenerSensorPasos()
                }
                _error.value = "Error al guardar configuración: ${e.message}"
            }
        }
    }

    private fun verificarYActivarSensor() {
        viewModelScope.launch {
            try {
                val tieneSensor = PasoSensorManager(context) {}.tieneSensor()
                _sensorDisponible.value = tieneSensor

                if (!tieneSensor) {
                    _contadorPasosActivo.value = false
                    _error.value = "Dispositivo no compatible con contador de pasos"
                } else if (MainActivity.hasActivityRecognitionPermission(context)) {
                    iniciarSensorPasos()
                }
            } catch (e: Exception) {
                Log.e("PasosVM", "Error al verificar sensor", e)
                _error.value = "Error al verificar sensores"
                _contadorPasosActivo.value = false
            }
        }
    }

    private fun iniciarSensorPasos() {
        pasoSensorManager = PasoSensorManager(context) { pasosHoy ->
            Log.d("PasosVM", "Nuevos pasos detectados: $pasosHoy")
            _pasos.value = pasosHoy

            // Guardar cada 10 pasos para no saturar la base de datos
            if (pasosHoy > 0 && (pasosHoy - ultimoValorGuardado).absoluteValue >= 10) {
                _pasosSincronizados.value = false
                ultimoValorGuardado = pasosHoy
                registrarPasos(pasosHoy)
            } else {
                _pasosSincronizados.value = true
            }
        }
        pasoSensorManager?.iniciar()
    }

    private fun detenerSensorPasos() {
        try {
            Log.d("PasosVM", "Iniciando detención del sensor...")
            pasoSensorManager?.detener()
            pasoSensorManager = null
            _pasosSincronizados.value = true
            // Opcional: resetear contador a 0 cuando se desactiva
            // _pasos.value = 0
            Log.d("PasosVM", "Sensor de pasos detenido correctamente")
        } catch (e: Exception) {
            Log.e("PasosVM", "Error al detener sensor", e)
        }
    }

    fun registrarPasos(pasos: Int) {
        viewModelScope.launch {
            try {
                Log.d("PasosVM", "Registrando $pasos pasos")
                val ok = repository.registrarPasosHoy(pasos)
                if (ok) {
                    _pasosSincronizados.value = true
                    cargarHistorialPasos()
                } else {
                    throw Exception("Error al guardar en Firestore")
                }
            } catch (e: Exception) {
                Log.e("PasosVM", "Error al registrar pasos", e)
                _error.value = "Error al guardar pasos"
                _pasosSincronizados.value = true
            }
        }
    }

    fun sincronizarPasosManualmente() {
        viewModelScope.launch {
            try {
                val pasosActuales = _pasos.value
                if (pasosActuales > 0) {
                    val ok = repository.registrarPasosHoy(pasosActuales)
                    if (ok) {
                        _pasosSincronizados.value = true
                        cargarHistorialPasos()
                    } else {
                        _error.value = "Error al sincronizar pasos"
                    }
                }
            } catch (e: Exception) {
                Log.e("PasosVM", "Error en sincronización manual", e)
                _error.value = "Error al sincronizar: ${e.message}"
            }
        }
    }

    fun cargarHistorialPasos() {
        viewModelScope.launch {
            try {
                _historialPasos.value = repository.obtenerHistorialPasos()
            } catch (e: Exception) {
                Log.e("PasosVM", "Error al cargar historial", e)
                _error.value = "Error al cargar historial"
            }
        }
    }

    // ===== FUNCIONES PARA ACTIVIDADES FÍSICAS =====

    fun cargarActividades() {
        viewModelScope.launch {
            try {
                _actividades.value = repository.obtenerActividades()
            } catch (e: Exception) {
                Log.e("ActividadVM", "Error al cargar actividades", e)
                _error.value = "Error al cargar actividades"
            }
        }
    }

    fun registrarNuevaActividad(actividad: ActividadFisica, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val ok = repository.registrarActividad(actividad)
                if (ok) {
                    onSuccess()
                    cargarActividades()
                } else {
                    throw Exception("Error al registrar actividad")
                }
            } catch (e: Exception) {
                Log.e("ActividadVM", "Error al registrar actividad", e)
                _error.value = "Error al registrar actividad"
            }
        }
    }

    fun actualizarActividad(actividad: ActividadFisica, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val ok = repository.actualizarActividad(actividad)
                if (ok) {
                    onSuccess()
                    cargarActividades()
                } else {
                    throw Exception("Error al actualizar actividad")
                }
            } catch (e: Exception) {
                Log.e("ActividadVM", "Error al actualizar actividad", e)
                _error.value = "Error al actualizar actividad"
            }
        }
    }

    suspend fun eliminarActividad(id: String): Boolean {
        return try {
            val eliminada = repository.eliminarActividad(id)
            if (eliminada) {
                cargarActividades()
            }
            eliminada
        } catch (e: Exception) {
            Log.e("ActividadVM", "Error al eliminar actividad", e)
            _error.value = "Error al eliminar actividad"
            false
        }
    }

    // ===== FUNCIONES DE UTILIDAD =====

    // Función para verificar si el sensor está realmente activo
    fun isSensorActivo(): Boolean {
        return pasoSensorManager?.estaActivo() == true && _contadorPasosActivo.value
    }

    // Función para forzar detención completa (útil para debugging)
    fun forzarDetenerContador() {
        viewModelScope.launch {
            try {
                _contadorPasosActivo.value = false
                detenerSensorPasos()

                // Actualizar también en Firestore
                val perfil = userRepo.getPersonaProfile()
                if (perfil != null) {
                    userRepo.updatePersonaProfile(perfil.copy(contadorPasosActivo = false))
                }

                Log.d("PasosVM", "Contador forzado a detenerse")
            } catch (e: Exception) {
                Log.e("PasosVM", "Error al forzar detención", e)
            }
        }
    }

    fun limpiarError() {
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        // Detener sensor al destruir el ViewModel
        detenerSensorPasos()
    }
}