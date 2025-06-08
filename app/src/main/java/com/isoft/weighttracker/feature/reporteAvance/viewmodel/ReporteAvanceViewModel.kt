package com.isoft.weighttracker.feature.reporteAvance.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isoft.weighttracker.feature.reporteAvance.data.ReportesAvanceRepository
import com.isoft.weighttracker.feature.reporteAvance.model.ReporteAvance
import com.isoft.weighttracker.feature.reporteAvance.model.Retroalimentacion
import com.isoft.weighttracker.feature.reporteAvance.model.TipoReporte
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReporteAvanceViewModel : ViewModel() {

    private val repository = ReportesAvanceRepository()
    private val TAG = "ReporteAvanceViewModel"

    private val _historial = MutableStateFlow<List<ReporteAvance>>(emptyList())
    val historial: StateFlow<List<ReporteAvance>> = _historial

    private val _reporteActual = MutableStateFlow<ReporteAvance?>(null)
    val reporteActual: StateFlow<ReporteAvance?> = _reporteActual

    private val _estadoGuardado = MutableStateFlow<Boolean?>(null)
    val estadoGuardado: StateFlow<Boolean?> = _estadoGuardado

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun cargarHistorial() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d(TAG, "Iniciando carga de historial...")

                val lista = repository.obtenerHistorial()
                Log.d(TAG, "Historial cargado: ${lista.size} reportes")

                _historial.value = lista
                _error.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar historial", e)
                _error.value = "Error al cargar el historial de reportes"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cargarReportePorId(id: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d(TAG, "Cargando reporte con ID: $id")

                val reporte = repository.obtenerReporte(id)
                if (reporte != null) {
                    Log.d(TAG, "Reporte cargado exitosamente: ${reporte.id}")
                    _reporteActual.value = reporte
                    _error.value = null
                } else {
                    Log.w(TAG, "No se encontró el reporte con ID: $id")
                    _error.value = "No se encontró el reporte solicitado"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar reporte por ID", e)
                _error.value = "Error al cargar el reporte"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun guardarReporte(reporte: ReporteAvance) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d(TAG, "Guardando reporte...")

                val exito = repository.guardarReporte(reporte)
                Log.d(TAG, "Resultado del guardado: $exito")

                _estadoGuardado.value = exito
                if (exito) {
                    // Recargar el historial para mostrar el nuevo reporte
                    cargarHistorial()
                } else {
                    _error.value = "No se pudo guardar el reporte"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al guardar reporte", e)
                _error.value = "Error inesperado al guardar el reporte"
                _estadoGuardado.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun agregarRetroalimentacion(idReporte: String, retro: Retroalimentacion) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d(TAG, "Agregando retroalimentación al reporte: $idReporte")

                val exito = repository.agregarRetroalimentacion(idReporte, retro)
                if (exito) {
                    Log.d(TAG, "Retroalimentación agregada exitosamente")
                    // Recargar el reporte actual para mostrar la nueva retroalimentación
                    cargarReportePorId(idReporte)
                    // También recargar el historial
                    cargarHistorial()
                } else {
                    _error.value = "Error al agregar retroalimentación"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error inesperado al agregar retroalimentación", e)
                _error.value = "Error inesperado al agregar retroalimentación"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cargarReportesPorTipo(tipoReporte: TipoReporte) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d(TAG, "Cargando reportes de tipo: ${tipoReporte.name}")

                val reportes = repository.obtenerReportesPorTipo(tipoReporte)
                Log.d(TAG, "Reportes por tipo cargados: ${reportes.size}")

                _historial.value = reportes
                _error.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar reportes por tipo", e)
                _error.value = "Error al cargar reportes de tipo ${tipoReporte.name}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun obtenerEstadisticasGenerales(): Map<String, Any> {
        val reportes = _historial.value
        if (reportes.isEmpty()) return emptyMap()

        val estadisticas = mutableMapOf<String, Any>()

        // Total de reportes
        estadisticas["totalReportes"] = reportes.size

        // Promedio de calorías
        val caloriasLista = reportes.map { it.caloriasQuemadas }
        if (caloriasLista.isNotEmpty()) {
            estadisticas["caloriasPromedio"] = caloriasLista.average()
        }

        // Promedio de pasos
        val pasosLista = reportes.map { it.pasosTotales }
        if (pasosLista.isNotEmpty()) {
            estadisticas["pasosPromedio"] = pasosLista.average()
        }

        // Promedio de progreso
        val progresoLista = reportes.mapNotNull { it.progresoMeta?.porcentajeProgreso }
        if (progresoLista.isNotEmpty()) {
            estadisticas["progresoPromedio"] = progresoLista.average()
        }

        // Peso actual y anterior
        reportes.firstOrNull()?.antropometria?.firstOrNull()?.peso?.let { peso ->
            estadisticas["pesoActual"] = peso
        }

        reportes.lastOrNull()?.antropometria?.firstOrNull()?.peso?.let { peso ->
            estadisticas["pesoAnterior"] = peso
        }

        return estadisticas
    }

    fun limpiarEstadoGuardado() {
        _estadoGuardado.value = null
    }

    fun limpiarError() {
        _error.value = null
    }

    fun limpiarReporteActual() {
        _reporteActual.value = null
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel limpiado")
    }
}