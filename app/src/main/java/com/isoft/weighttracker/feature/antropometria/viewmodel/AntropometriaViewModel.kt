package com.isoft.weighttracker.feature.antropometria.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isoft.weighttracker.feature.antropometria.model.Antropometria
import com.isoft.weighttracker.feature.antropometria.data.AntropometriaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.pow

class AntropometriaViewModel : ViewModel() {

    private val repository = AntropometriaRepository()

    private val _registros = MutableStateFlow<List<Antropometria>>(emptyList())
    val registros: StateFlow<List<Antropometria>> = _registros

    private val _alerta = MutableStateFlow<String?>(null)
    val alerta: StateFlow<String?> = _alerta

    //para estados de la ui
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun clearAlerta() {
        _alerta.value = null
    }

    fun cargarRegistros() {
        viewModelScope.launch {
            val data = repository.obtenerRegistros()
            _registros.value = data
        }
    }

    private val _registrosDeUsuario = MutableStateFlow<List<Antropometria>>(emptyList())
    val registrosDeUsuario: StateFlow<List<Antropometria>> = _registrosDeUsuario

    fun cargarRegistrosDeUsuario(usuarioId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val data = repository.obtenerRegistrosDeUsuario(usuarioId)
            _registrosDeUsuario.value = data
            _isLoading.value = false
        }
    }

    fun guardarRegistroNuevo(
        peso: Float,
        cintura: Float,
        cuello: Float,
        cadera: Float?,
        estatura: Float,
        sexo: String,
        edad: Int,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val imc = peso / ((estatura / 100f).pow(2))

            val porcentajeGrasa = try {
                when (sexo.lowercase()) {
                    "masculino" -> {
                        495 / (
                                1.0324 - 0.19077 * log10((cintura - cuello).toDouble()) +
                                        0.15456 * log10(estatura.toDouble())
                                ) - 450
                    }

                    "femenino" -> {
                        if (cadera != null) {
                            495 / (
                                    1.29579 - 0.35004 * log10((cintura + cadera - cuello).toDouble()) +
                                            0.22100 * log10(estatura.toDouble())
                                    ) - 450
                        } else 0.0
                    }

                    else -> 0.0
                }
            } catch (e: Exception) {
                _alerta.value = "❌ Error en los cálculos. Revisa las medidas ingresadas."
                return@launch
            }

            val porcentajeGrasaFloat = porcentajeGrasa.toFloat()
            val diagnostico = diagnosticarGrasa(sexo, edad, porcentajeGrasaFloat)

            val alertaList = mutableListOf<String>()
            val ultimoRegistro = _registros.value.firstOrNull()

            if (ultimoRegistro != null) {
                val diferencia = peso - ultimoRegistro.peso
                if (abs(diferencia) > 10f) {
                    alertaList.add("⚠️ Cambio de peso de ${"%.1f".format(diferencia)} kg desde tu último registro.")
                }

                val cambioGrasa = abs(porcentajeGrasaFloat - ultimoRegistro.porcentajeGrasa)
                if ((porcentajeGrasaFloat > 60f || porcentajeGrasaFloat < 3f) && cambioGrasa > 0.2f) {
                    alertaList.add("⚠️ El porcentaje de grasa (${String.format("%.1f", porcentajeGrasaFloat)}%) parece inusual.")
                }
            }

            if (cuello > cintura || cuello > (cadera ?: 999f)) {
                alertaList.add("⚠️ El valor de cuello parece mayor a otras medidas.")
            }

            if (imc < 10f || imc > 60f) {
                alertaList.add("⚠️ Tu IMC calculado (${String.format("%.1f", imc)}) está fuera de un rango saludable.")
            }

            if (alertaList.isNotEmpty()) {
                _alerta.value = alertaList.joinToString("\n")
                return@launch
            }

            val nuevo = Antropometria(
                fecha = System.currentTimeMillis(), // 💥 MUY IMPORTANTE
                peso = peso,
                cintura = cintura,
                cuello = cuello,
                cadera = cadera,
                imc = imc,
                porcentajeGrasa = porcentajeGrasaFloat
            )

            repository.guardarRegistro(nuevo)

            onSuccess() // ✅ Primero navegar (experiencia rápida)
            cargarRegistros() // ✅ Luego actualizar la lista (en background)
        }
    }

    fun actualizarRegistro(registro: Antropometria, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val ok = repository.actualizarRegistro(registro)
            if (ok) {
                onSuccess()
                cargarRegistros()
            }
        }
    }

    suspend fun eliminarRegistro(id: String): Boolean {
        return repository.eliminarRegistro(id).also {
            if (it) cargarRegistros()
        }
    }

    private fun diagnosticarGrasa(sexo: String, edad: Int, grasa: Float): String {
        return when (sexo.lowercase()) {
            "masculino" -> when {
                edad in 15..39 -> when {
                    grasa < 8 -> "Bajo en grasa"
                    grasa < 20 -> "Saludable"
                    grasa < 25 -> "Sobrepeso"
                    else -> "Obesidad"
                }
                edad in 40..59 -> when {
                    grasa < 11 -> "Bajo en grasa"
                    grasa < 22 -> "Saludable"
                    grasa < 28 -> "Sobrepeso"
                    else -> "Obesidad"
                }
                edad in 60..79 -> when {
                    grasa < 13 -> "Bajo en grasa"
                    grasa < 25 -> "Saludable"
                    grasa < 30 -> "Sobrepeso"
                    else -> "Obesidad"
                }
                else -> "Edad fuera de rango"
            }

            "femenino" -> when {
                edad in 15..39 -> when {
                    grasa < 16 -> "Bajo en grasa"
                    grasa < 28 -> "Saludable"
                    grasa < 39 -> "Sobrepeso"
                    else -> "Obesidad"
                }
                edad in 40..59 -> when {
                    grasa < 18 -> "Bajo en grasa"
                    grasa < 30 -> "Saludable"
                    grasa < 40 -> "Sobrepeso"
                    else -> "Obesidad"
                }
                edad in 60..79 -> when {
                    grasa < 20 -> "Bajo en grasa"
                    grasa < 32 -> "Saludable"
                    grasa < 42 -> "Sobrepeso"
                    else -> "Obesidad"
                }
                else -> "Edad fuera de rango"
            }

            else -> "Sexo no reconocido"
        }
    }
}