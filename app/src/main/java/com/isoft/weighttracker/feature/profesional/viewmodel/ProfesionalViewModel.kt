package com.isoft.weighttracker.feature.profesional.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.isoft.weighttracker.core.data.UserRepository
import com.isoft.weighttracker.core.model.User
import com.isoft.weighttracker.feature.reporteAvance.data.ReportesAvanceRepository
import com.isoft.weighttracker.feature.reporteAvance.model.ReporteAvance
import com.isoft.weighttracker.feature.reporteAvance.model.Retroalimentacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfesionalViewModel(
    private val userRepo: UserRepository = UserRepository(),
    private val reportesRepo: ReportesAvanceRepository = ReportesAvanceRepository()
) : ViewModel() {

    private val TAG = "ProfesionalViewModel"

    // Estados existentes
    private val _usuariosAsociados = MutableStateFlow<List<User>>(emptyList())
    val usuariosAsociados: StateFlow<List<User>> = _usuariosAsociados

    // Nuevos estados para reportes
    private val _reportesUsuario = MutableStateFlow<List<ReporteAvance>>(emptyList())
    val reportesUsuario: StateFlow<List<ReporteAvance>> = _reportesUsuario

    private val _reporteActual = MutableStateFlow<ReporteAvance?>(null)
    val reporteActual: StateFlow<ReporteAvance?> = _reporteActual

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Función existente
    fun cargarUsuariosAsociados(tipo: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        viewModelScope.launch {
            val usuarios = userRepo.getUsuariosAsociados(tipo, uid)
            _usuariosAsociados.value = usuarios
        }
    }

    // Nuevas funciones para reportes
    fun cargarReportesDeUsuario(usuarioId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d(TAG, "Cargando reportes del usuario: $usuarioId")

                val reportes = reportesRepo.obtenerReportesDeUsuario(usuarioId)

                // ✅ NUEVO FILTRO: Mostrar todos los reportes, pero organizar por estado
                val reportesOrganizados = reportes.sortedWith(
                    compareBy<ReporteAvance> { it.retroalimentaciones.isNotEmpty() }
                        .thenByDescending { it.fechaCreacion }
                )

                Log.d(TAG, "Reportes cargados: ${reportesOrganizados.size}")
                _reportesUsuario.value = reportesOrganizados
                _error.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar reportes del usuario", e)
                _error.value = "Error al cargar los reportes del usuario"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cargarReportePorId(reporteId: String, usuarioId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d(TAG, "Cargando reporte: $reporteId del usuario: $usuarioId")

                val reporte = reportesRepo.obtenerReporteDeUsuario(reporteId, usuarioId)
                if (reporte != null) {
                    Log.d(TAG, "Reporte cargado exitosamente")
                    _reporteActual.value = reporte
                    _error.value = null
                } else {
                    Log.w(TAG, "No se encontró el reporte")
                    _error.value = "No se encontró el reporte solicitado"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar reporte", e)
                _error.value = "Error al cargar el reporte"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun agregarRetroalimentacion(reporteId: String, usuarioId: String, retroalimentacion: Retroalimentacion) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d(TAG, "Agregando retroalimentación al reporte: $reporteId")

                val exito = reportesRepo.agregarRetroalimentacionAUsuario(reporteId, usuarioId, retroalimentacion)
                if (exito) {
                    Log.d(TAG, "Retroalimentación agregada exitosamente")
                    // Recargar el reporte para mostrar la nueva retroalimentación
                    cargarReportePorId(reporteId, usuarioId)
                    _error.value = null
                } else {
                    _error.value = "Error al agregar retroalimentación"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al agregar retroalimentación", e)
                _error.value = "Error inesperado al agregar retroalimentación"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun limpiarError() {
        _error.value = null
    }

    fun limpiarReporteActual() {
        _reporteActual.value = null
    }
}