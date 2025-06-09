package com.isoft.weighttracker.feature.reporteAvance.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
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

    // Listener para cambios en tiempo real
    private var historialListener: ListenerRegistration? = null
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

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

    // ✅ NUEVO: Estado para diagnóstico
    private val _diagnostico = MutableStateFlow<String>("")
    val diagnostico: StateFlow<String> = _diagnostico

    // ✅ FUNCIÓN MEJORADA: Configurar listener en tiempo real con metadata changes
    private fun configurarListenerHistorial() {
        val uid = auth.currentUser?.uid ?: return

        // Cancelar listener anterior si existe
        historialListener?.remove()

        Log.d(TAG, "🔄 Configurando listener en tiempo real para historial...")

        historialListener = db.collection("users")
            .document(uid)
            .collection("reportes_avance")
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ Error en listener de historial", error)
                    _error.value = "Error al actualizar historial en tiempo real: ${error.message}"
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    Log.d(TAG, "🔄 Snapshot recibido:")
                    Log.d(TAG, "   - Documentos: ${snapshot.documents.size}")
                    Log.d(TAG, "   - Desde cache: ${snapshot.metadata.isFromCache}")

                    val reportes = snapshot.documents.mapNotNull { document ->
                        try {
                            val reporte = document.toObject(ReporteAvance::class.java)
                            if (reporte != null) {
                                Log.d(TAG, "📄 Reporte listener: ${reporte.id} - Retroalimentaciones: ${reporte.retroalimentaciones.size}")
                                // ✅ Log detallado para debugging
                                reporte.retroalimentaciones.forEach { retro ->
                                    Log.d(TAG, "     💬 ${retro.contenido.take(30)}... (${retro.fecha})")
                                }
                            }
                            reporte
                        } catch (e: Exception) {
                            Log.e(TAG, "❌ Error al convertir documento en listener", e)
                            null
                        }
                    }.sortedByDescending { it.fechaCreacion }

                    Log.d(TAG, "✅ Historial actualizado via listener: ${reportes.size} reportes")
                    val reportesConRetro = reportes.count { it.retroalimentaciones.isNotEmpty() }
                    Log.d(TAG, "📊 Reportes con retroalimentaciones via listener: $reportesConRetro")

                    _historial.value = reportes
                }
            }
    }

    fun cargarHistorial() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d(TAG, "🚀 Iniciando carga de historial...")

                // ✅ Configurar listener en tiempo real
                configurarListenerHistorial()

                // ✅ También hacer una carga inicial desde servidor para asegurar datos frescos
                Log.d(TAG, "📥 Cargando datos iniciales desde servidor...")
                val lista = repository.obtenerHistorial(forzarDesdeServidor = true)
                Log.d(TAG, "📊 Historial inicial cargado: ${lista.size} reportes")

                _historial.value = lista
                _error.value = null
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al cargar historial", e)
                _error.value = "Error al cargar el historial de reportes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ✅ NUEVA FUNCIÓN: Forzar actualización completa
    fun forzarActualizacionCompleta() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d(TAG, "🔄 Forzando actualización completa desde servidor...")

                // Cancelar y reconfigurar listener
                historialListener?.remove()
                configurarListenerHistorial()

                // Obtener datos frescos desde servidor
                val lista = repository.obtenerHistorial(forzarDesdeServidor = true)
                Log.d(TAG, "📊 Actualización forzada: ${lista.size} reportes")

                val reportesConRetro = lista.count { it.retroalimentaciones.isNotEmpty() }
                Log.d(TAG, "📊 Reportes con retroalimentaciones (forzado): $reportesConRetro")

                _historial.value = lista
                _error.value = null
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error en actualización forzada", e)
                _error.value = "Error al actualizar: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ✅ NUEVA FUNCIÓN: Ejecutar diagnóstico
    fun ejecutarDiagnostico() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🔍 Ejecutando diagnóstico completo...")
                val resultado = repository.diagnosticoCompleto()
                _diagnostico.value = resultado
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error en diagnóstico", e)
                _diagnostico.value = "Error en diagnóstico: ${e.message}"
            }
        }
    }

    fun cargarReportePorId(id: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d(TAG, "📄 Cargando reporte con ID: $id")

                val reporte = repository.obtenerReporte(id)
                if (reporte != null) {
                    Log.d(TAG, "✅ Reporte cargado exitosamente: ${reporte.id}")
                    Log.d(TAG, "📊 Retroalimentaciones en reporte: ${reporte.retroalimentaciones.size}")
                    _reporteActual.value = reporte
                    _error.value = null
                } else {
                    Log.w(TAG, "❌ No se encontró el reporte con ID: $id")
                    _error.value = "No se encontró el reporte solicitado"
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al cargar reporte por ID", e)
                _error.value = "Error al cargar el reporte: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun guardarReporte(reporte: ReporteAvance) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d(TAG, "💾 Guardando reporte...")

                val exito = repository.guardarReporte(reporte)
                Log.d(TAG, "📊 Resultado del guardado: $exito")

                _estadoGuardado.value = exito
                if (!exito) {
                    _error.value = "No se pudo guardar el reporte"
                }
                // El listener se encargará de la actualización automática
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al guardar reporte", e)
                _error.value = "Error inesperado al guardar el reporte: ${e.message}"
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
                Log.d(TAG, "💬 Agregando retroalimentación al reporte: $idReporte")

                val exito = repository.agregarRetroalimentacion(idReporte, retro)
                if (exito) {
                    Log.d(TAG, "✅ Retroalimentación agregada exitosamente")
                    // Recargar el reporte actual
                    cargarReportePorId(idReporte)
                    // El listener se encargará de actualizar el historial
                } else {
                    _error.value = "Error al agregar retroalimentación"
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error inesperado al agregar retroalimentación", e)
                _error.value = "Error inesperado al agregar retroalimentación: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cargarReportesPorTipo(tipoReporte: TipoReporte) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d(TAG, "🏷️ Cargando reportes de tipo: ${tipoReporte.name}")

                val reportes = repository.obtenerReportesPorTipo(tipoReporte)
                Log.d(TAG, "📊 Reportes por tipo cargados: ${reportes.size}")

                _historial.value = reportes
                _error.value = null
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al cargar reportes por tipo", e)
                _error.value = "Error al cargar reportes de tipo ${tipoReporte.name}: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cargarReportesDeUsuario(usuarioId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d(TAG, "👤 Cargando reportes del usuario: $usuarioId")

                val reportes = repository.obtenerReportesDeUsuario(usuarioId)
                Log.d(TAG, "📊 Reportes del usuario cargados: ${reportes.size}")

                _historial.value = reportes
                _error.value = null
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al cargar reportes del usuario", e)
                _error.value = "Error al cargar los reportes del usuario: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ✅ NUEVA FUNCIÓN: Actualización manual del historial
    fun actualizarHistorial() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🔄 Forzando actualización manual del historial...")
                val lista = repository.obtenerHistorial(forzarDesdeServidor = true)
                Log.d(TAG, "📊 Historial actualizado manualmente: ${lista.size} reportes")

                val reportesConRetro = lista.count { it.retroalimentaciones.isNotEmpty() }
                Log.d(TAG, "📊 Reportes con retroalimentaciones (manual): $reportesConRetro")

                _historial.value = lista
                _error.value = null
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al actualizar historial manualmente", e)
                _error.value = "Error al actualizar el historial: ${e.message}"
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

    fun limpiarDiagnostico() {
        _diagnostico.value = ""
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "🧹 ViewModel limpiado - Removiendo listener")
        historialListener?.remove()
    }
}