package com.isoft.weighttracker.feature.asociar.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.isoft.weighttracker.core.model.User
import com.isoft.weighttracker.feature.asociar.data.AsociarProfesionalRepository
import com.isoft.weighttracker.feature.asociar.data.ProfesionalAsociadoRepository
import com.isoft.weighttracker.feature.asociar.data.ProfesionalCompleto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AsociarProfesionalViewModel(
    private val asociarRepo: AsociarProfesionalRepository = AsociarProfesionalRepository(),
    private val asociadoRepo: ProfesionalAsociadoRepository = ProfesionalAsociadoRepository()
) : ViewModel() {

    private val _estado = MutableStateFlow<String?>(null)
    val estado: StateFlow<String?> = _estado

    // ✅ MANTENER para compatibilidad
    private val _asociados = MutableStateFlow<Map<String, User>>(emptyMap())
    val asociados: StateFlow<Map<String, User>> = _asociados

    // ✅ NUEVO - StateFlow para información completa
    private val _asociadosCompletos = MutableStateFlow<Map<String, ProfesionalCompleto>>(emptyMap())
    val asociadosCompletos: StateFlow<Map<String, ProfesionalCompleto>> = _asociadosCompletos

    fun clearEstado() {
        _estado.value = null
    }

    fun cargarProfesionalesAsociados() {
        viewModelScope.launch {
            Log.d("AsociarProfesional", "🔍 Iniciando carga de profesionales asociados...")

            try {
                // ✅ NUEVO - Cargar información completa
                val resultadoCompleto = asociadoRepo.obtenerAsociadosCompletos()
                Log.d("AsociarProfesional", "📋 Resultado completo obtenido: $resultadoCompleto")
                Log.d("AsociarProfesional", "📊 Cantidad de asociados: ${resultadoCompleto.size}")

                resultadoCompleto.forEach { (tipo, profesionalCompleto) ->
                    val user = profesionalCompleto.user
                    val profile = profesionalCompleto.profesionalProfile
                    Log.d("AsociarProfesional", "👤 $tipo: ${user.name} (${user.email}) - UID: ${user.uid}")
                    Log.d("AsociarProfesional", "📋 Especialidad: ${profile?.especialidad ?: "Sin especialidad"}")
                    Log.d("AsociarProfesional", "🆔 Código: ${profile?.idProfesional ?: "Sin código"}")
                }

                _asociadosCompletos.value = resultadoCompleto

                // ✅ MANTENER compatibilidad
                _asociados.value = resultadoCompleto.mapValues { it.value.user }

                Log.d("AsociarProfesional", "✅ Estado actualizado correctamente")

            } catch (e: Exception) {
                Log.e("AsociarProfesional", "❌ Error al cargar asociados: ${e.message}", e)
            }
        }
    }

    fun asociarProfesional(codigo: String, tipo: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            Log.d("AsociarProfesional", "🔗 Intentando asociar: $codigo como $tipo")

            val currentUser = FirebaseAuth.getInstance().currentUser ?: run {
                _estado.value = "❌ Usuario no autenticado"
                return@launch
            }

            _estado.value = "🔍 Buscando profesional..."

            val profe = asociarRepo.buscarProfesionalPorCodigo(codigo.trim(), tipo)

            if (profe == null) {
                Log.w("AsociarProfesional", "❌ Profesional no encontrado con código: $codigo")
                _estado.value = "❌ Profesional no encontrado"
                return@launch
            }

            Log.d("AsociarProfesional", "👤 Profesional encontrado: ${profe.name} - UID: ${profe.uid}")

            val ok = asociarRepo.asociarProfesional(currentUser.uid, tipo, profe.uid)
            Log.d("AsociarProfesional", "💾 Resultado de asociación: $ok")

            if (ok) {
                _estado.value = "✅ Profesional asociado"
                Log.d("AsociarProfesional", "🔄 Recargando lista de asociados...")
                cargarProfesionalesAsociados()
                onSuccess()
            } else {
                _estado.value = "⚠️ Error al asociar"
            }
        }
    }

    fun eliminarAsociacion(tipo: String) {
        viewModelScope.launch {
            Log.d("AsociarProfesional", "🗑️ Eliminando asociación: $tipo")

            val ok = asociadoRepo.eliminarAsociacion(tipo)

            if (ok) {
                _estado.value = "✅ Profesional eliminado"
                Log.d("AsociarProfesional", "🔄 Recargando lista después de eliminar...")
                cargarProfesionalesAsociados()
            } else {
                _estado.value = "⚠️ Error al eliminar"
            }
        }
    }
}