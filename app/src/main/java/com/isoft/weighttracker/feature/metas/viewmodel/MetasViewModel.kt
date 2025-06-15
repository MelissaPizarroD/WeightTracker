package com.isoft.weighttracker.feature.metas.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isoft.weighttracker.feature.antropometria.data.AntropometriaRepository
import com.isoft.weighttracker.feature.metas.data.MetasRepository
import com.isoft.weighttracker.feature.metas.model.Meta
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

data class ProgresoMeta(
    var pesoActual: Float = 0f,
    var porcentajeProgreso: Float = 0f,
    var pesoRestante: Float = 0f,
    var diasRestantes: Long = 0L,
    var enProgreso: Boolean = false
)
class MetasViewModel : ViewModel() {

    private val metasRepository = MetasRepository()
    private val antropometriaRepository = AntropometriaRepository()

    private val _metas = MutableStateFlow<List<Meta>>(emptyList())
    val metas: StateFlow<List<Meta>> = _metas.asStateFlow()

    private val _metaActiva = MutableStateFlow<Meta?>(null)
    val metaActiva: StateFlow<Meta?> = _metaActiva.asStateFlow()

    private val _progreso = MutableStateFlow<ProgresoMeta?>(null)
    val progreso: StateFlow<ProgresoMeta?> = _progreso.asStateFlow()

    private val _alerta = MutableStateFlow<String?>(null)
    val alerta: StateFlow<String?> = _alerta.asStateFlow()

    private val _eventoMeta = MutableStateFlow<String?>(null)
    val eventoMeta: StateFlow<String?> = _eventoMeta.asStateFlow()

    //Para pantalla de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun clearEventoMeta() {
        _eventoMeta.value = null
    }

    private fun notificarEvento(texto: String) {
        _eventoMeta.value = texto
    }

    fun clearAlerta() {
        _alerta.value = null
    }

    fun cargarMetas() {
        viewModelScope.launch {
            _isLoading.value = true
            val metas = metasRepository.obtenerMetas()
            _metas.value = metas
            val metaActiva = metas.firstOrNull { it.activa && !it.cumplida }
            _metaActiva.value = metaActiva
            Log.d("MetasViewModel", "Meta activa: ${metaActiva?.id}")
            metaActiva?.let { calcularProgreso(it) }
            _isLoading.value = false
        }
    }

    // Función específica para cargar solo la meta activa
    fun cargarMetaActiva() {
        viewModelScope.launch {
            try {
                Log.d("MetasViewModel", "🚀 === INICIANDO CARGA META ACTIVA ===")

                val metaActiva = metasRepository.obtenerMetaActiva()

                Log.d("MetasViewModel", "📊 Meta obtenida del repositorio: $metaActiva")
                Log.d("MetasViewModel", "📊 Meta ID: ${metaActiva?.id}")
                Log.d("MetasViewModel", "📊 Meta activa flag: ${metaActiva?.activa}")
                Log.d("MetasViewModel", "📊 Meta cumplida flag: ${metaActiva?.cumplida}")
                Log.d("MetasViewModel", "📊 Meta objetivo: ${metaActiva?.objetivo}")

                _metaActiva.value = metaActiva

                Log.d("MetasViewModel", "📊 StateFlow actualizado - valor actual: ${_metaActiva.value}")
                Log.d("MetasViewModel", "✅ === FIN CARGA META ACTIVA ===")

                metaActiva?.let {
                    Log.d("MetasViewModel", "🎯 Calculando progreso para meta: ${it.id}")
                    calcularProgreso(it)
                } ?: Log.w("MetasViewModel", "⚠️ No hay meta activa para calcular progreso")

            } catch (e: Exception) {
                Log.e("MetasViewModel", "❌ Error al cargar meta activa", e)
                _metaActiva.value = null
            }
        }
    }

    // También agregar logs a cargarProgreso()
    fun cargarProgreso() {
        viewModelScope.launch {
            try {
                Log.d("MetasViewModel", "🚀 === INICIANDO CARGA PROGRESO ===")

                val metaActiva = _metaActiva.value
                Log.d("MetasViewModel", "📊 Meta activa actual en StateFlow: $metaActiva")

                if (metaActiva != null) {
                    Log.d("MetasViewModel", "🎯 Calculando progreso para meta existente: ${metaActiva.id}")
                    calcularProgreso(metaActiva)
                } else {
                    Log.w("MetasViewModel", "⚠️ No hay meta activa en StateFlow, intentando cargarla...")
                    // Si no hay meta activa, intentar cargarla primero
                    val meta = metasRepository.obtenerMetaActiva()
                    Log.d("MetasViewModel", "📊 Meta obtenida del repositorio (segunda llamada): $meta")

                    if (meta != null) {
                        _metaActiva.value = meta
                        Log.d("MetasViewModel", "🎯 Calculando progreso para meta recién cargada: ${meta.id}")
                        calcularProgreso(meta)
                    } else {
                        Log.w("MetasViewModel", "❌ No se pudo obtener meta activa del repositorio")
                        _progreso.value = null
                    }
                }

                Log.d("MetasViewModel", "📊 Progreso final: ${_progreso.value}")
                Log.d("MetasViewModel", "✅ === FIN CARGA PROGRESO ===")

            } catch (e: Exception) {
                Log.e("MetasViewModel", "❌ Error al cargar progreso", e)
                _progreso.value = null
            }
        }
    }


    private suspend fun calcularProgreso(meta: Meta) {
        val registros = antropometriaRepository.obtenerRegistros()
        val registrosFiltrados = registros.filter { it.fecha >= meta.fechaInicio }
        val ultimoRegistro = registrosFiltrados.firstOrNull()

        if (ultimoRegistro == null) {
            _progreso.value = null
            return
        }

        val pesoActual = ultimoRegistro.peso
        val pesoInicial = meta.pesoInicial
        val pesoObjetivo = meta.pesoObjetivo

        val ahora = System.currentTimeMillis()
        val diasRestantes = ((meta.fechaLimite - ahora) / (24 * 60 * 60 * 1000)).coerceAtLeast(0)

        val direccionCorrecta = pesoObjetivo - pesoInicial

        // ✅ CORRECCIÓN: Siempre incluir el peso inicial en el cálculo del "peor peso"
        val peorPeso = when {
            direccionCorrecta < 0 -> {
                // Para BAJAR de peso: el peor peso es el más ALTO registrado
                val maxRegistros = registrosFiltrados.maxOfOrNull { it.peso } ?: pesoInicial
                maxOf(maxRegistros, pesoInicial) // Asegurar que el peso inicial sea considerado
            }
            direccionCorrecta > 0 -> {
                // Para SUBIR de peso: el peor peso es el más BAJO registrado
                val minRegistros = registrosFiltrados.minOfOrNull { it.peso } ?: pesoInicial
                minOf(minRegistros, pesoInicial) // Asegurar que el peso inicial sea considerado
            }
            else -> pesoInicial
        }

        val avance = when {
            direccionCorrecta < 0 && peorPeso > pesoObjetivo -> (peorPeso - pesoActual) / (peorPeso - pesoObjetivo)
            direccionCorrecta > 0 && peorPeso < pesoObjetivo -> (pesoActual - peorPeso) / (pesoObjetivo - peorPeso)
            direccionCorrecta == 0f -> 1f
            else -> 0f
        }

        val porcentajeProgreso = (avance.coerceIn(0f, 1f)) * 100f
        val pesoRestante = abs(pesoObjetivo - pesoActual)

        val enProgreso = when {
            direccionCorrecta > 0f -> pesoActual < pesoObjetivo - 0.5f
            direccionCorrecta < 0f -> pesoActual > pesoObjetivo + 0.5f
            else -> false
        }

        _progreso.value = ProgresoMeta(
            pesoActual = pesoActual,
            porcentajeProgreso = porcentajeProgreso,
            pesoRestante = pesoRestante,
            diasRestantes = diasRestantes,
            enProgreso = enProgreso
        )

        val objetivoCumplido = when {
            direccionCorrecta > 0f -> pesoActual >= pesoObjetivo - 0.5f
            direccionCorrecta < 0f -> pesoActual <= pesoObjetivo + 0.5f
            else -> true
        }

        if (!meta.cumplida && objetivoCumplido) {
            marcarMetaComoCumplida(meta.id ?: "")
        }

        if (meta.activa && !meta.cumplida && diasRestantes <= 0) {
            marcarMetaComoVencida(meta.id ?: "")
        }
    }

    fun marcarMetaComoCumplida(id: String) {
        viewModelScope.launch {
            val marcada = metasRepository.marcarMetaComoCumplida(id)
            if (marcada) {
                notificarEvento("🎯 ¡Meta cumplida!")
                delay(1000)
                cargarMetas()
            }
        }
    }

    fun marcarMetaComoVencida(id: String) {
        viewModelScope.launch {
            val marcada = metasRepository.marcarMetaComoVencida(id)
            if (marcada) {
                notificarEvento("⏰ Tu meta venció")
                delay(1000)
                cargarMetas()
            }
        }
    }

    fun guardarNuevaMeta(
        pesoObjetivo: Float,
        fechaLimite: Long,
        objetivo: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val ultimoPeso = obtenerUltimoPeso() ?: run {
                _alerta.value = "❌ Necesitas al menos un registro antropométrico."
                return@launch
            }

            if (objetivo.lowercase() == "bajar" && pesoObjetivo >= ultimoPeso) {
                _alerta.value = "⚠️ El objetivo debe ser menor al peso actual (${ultimoPeso} kg)."
                return@launch
            }

            if (objetivo.lowercase() == "subir" && pesoObjetivo <= ultimoPeso) {
                _alerta.value = "⚠️ El objetivo debe ser mayor al peso actual (${ultimoPeso} kg)."
                return@launch
            }

            val diferencia = abs(pesoObjetivo - ultimoPeso)
            if (diferencia > 50f) {
                _alerta.value = "⚠️ Diferencia muy grande (${String.format("%.1f", diferencia)} kg)"
                return@launch
            }

            if (fechaLimite <= System.currentTimeMillis()) {
                _alerta.value = "⚠️ La fecha debe ser futura."
                return@launch
            }

            val diasDisponibles = (fechaLimite - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)
            val pesoSemana = diferencia / (diasDisponibles / 7.0)
            if (pesoSemana > 2.0) {
                _alerta.value = "⚠️ Meta muy exigente (${String.format("%.1f", pesoSemana)} kg/semana)"
                return@launch
            }

            val nuevaMeta = Meta(
                pesoInicial = ultimoPeso,
                pesoObjetivo = pesoObjetivo,
                fechaLimite = fechaLimite,
                objetivo = objetivo,
                cumplida = false,
                activa = true
            )

            val metaGuardada = metasRepository.guardarMetaConId(nuevaMeta)
            if (metaGuardada != null) {
                onSuccess()
                cargarMetas()
            } else {
                _alerta.value = "❌ Error al guardar la meta"
            }
        }
    }

    fun detenerMeta(id: String) {
        viewModelScope.launch {
            if (metasRepository.detenerMeta(id)) cargarMetas()
        }
    }

    fun reactivarMeta(id: String) {
        viewModelScope.launch {
            if (metasRepository.reactivarMeta(id)) {
                cargarMetas()
            } else {
                _alerta.value = "❌ Error al reactivar meta"
            }
        }
    }

    fun actualizarFechaLimite(id: String, nuevaFecha: Long) {
        viewModelScope.launch {
            if (metasRepository.actualizarFechaLimite(id, nuevaFecha)) {
                _alerta.value = "✔️ Fecha límite actualizada"
                cargarMetas()
            } else {
                _alerta.value = "❌ Error al actualizar fecha"
            }
        }
    }

    fun extenderFechaLimite(id: String, nuevaFecha: Long) {
        viewModelScope.launch {
            if (metasRepository.extenderFechaLimite(id, nuevaFecha)) {
                cargarMetas()
            } else {
                _alerta.value = "❌ Error al extender fecha"
            }
        }
    }

    fun eliminarMeta(id: String) {
        viewModelScope.launch {
            if (metasRepository.eliminarMeta(id)) {
                cargarMetas()
            } else {
                _alerta.value = "❌ Error al eliminar meta"
            }
        }
    }

    suspend fun eliminarMetaInternal(id: String): Boolean {
        return metasRepository.eliminarMeta(id).also {
            if (it) cargarMetas()
        }
    }

    suspend fun obtenerUltimoPeso(): Float? {
        return antropometriaRepository.obtenerRegistros().firstOrNull()?.peso
    }

    fun calcularRecomendacionSemanal(pesoActual: Float, pesoObjetivo: Float, diasDisponibles: Long): String {
        val diferencia = abs(pesoObjetivo - pesoActual)
        val semanas = diasDisponibles / 7.0
        val pesoSemana = if (semanas > 0) diferencia / semanas else diferencia.toDouble()

        return when {
            pesoSemana <= 0.5 -> "🟢 Muy alcanzable (${String.format("%.1f", pesoSemana)} kg/sem)"
            pesoSemana <= 1.0 -> "🟡 Moderada (${String.format("%.1f", pesoSemana)} kg/sem)"
            pesoSemana <= 2.0 -> "🟠 Ambiciosa (${String.format("%.1f", pesoSemana)} kg/sem)"
            else -> "🔴 Muy difícil (${String.format("%.1f", pesoSemana)} kg/sem)"
        }
    }
}