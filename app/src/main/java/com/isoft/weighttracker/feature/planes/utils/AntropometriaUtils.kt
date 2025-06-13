package com.isoft.weighttracker.feature.planes.utils

import kotlin.math.pow

/**
 * Utilidades para cálculos antropométricos y clasificaciones médicas
 */
object AntropometriaUtils {

    /**
     * Clasifica el IMC según estándares de la OMS
     */
    fun clasificarIMC(imc: Float): String {
        return when {
            imc < 16.0 -> "Peso muy bajo (severo)"
            imc < 17.0 -> "Peso muy bajo (moderado)"
            imc < 18.5 -> "Peso bajo"
            imc < 25.0 -> "Peso normal"
            imc < 30.0 -> "Sobrepeso"
            imc < 35.0 -> "Obesidad clase I"
            imc < 40.0 -> "Obesidad clase II"
            else -> "Obesidad clase III (mórbida)"
        }
    }

    /**
     * Calcula el IMC dados peso y estatura
     */
    fun calcularIMC(pesoKg: Float, estaturaM: Float): Float {
        return pesoKg / (estaturaM.pow(2))
    }

    /**
     * Calcula el IMC dados peso y estatura en cm
     */
    fun calcularIMCConCm(pesoKg: Float, estaturaCm: Float): Float {
        val estaturaM = estaturaCm / 100f
        return calcularIMC(pesoKg, estaturaM)
    }

    /**
     * Clasifica el porcentaje de grasa corporal según edad y sexo
     */
    fun clasificarPorcentajeGrasa(porcentajeGrasa: Float, sexo: String, edad: Int): String {
        val esHombre = sexo.lowercase() == "masculino"

        return when {
            esHombre -> {
                when {
                    edad < 30 -> when {
                        porcentajeGrasa < 8 -> "Muy bajo"
                        porcentajeGrasa < 14 -> "Atlético"
                        porcentajeGrasa < 18 -> "Bueno"
                        porcentajeGrasa < 25 -> "Promedio"
                        else -> "Alto"
                    }
                    edad < 50 -> when {
                        porcentajeGrasa < 11 -> "Muy bajo"
                        porcentajeGrasa < 17 -> "Atlético"
                        porcentajeGrasa < 21 -> "Bueno"
                        porcentajeGrasa < 28 -> "Promedio"
                        else -> "Alto"
                    }
                    else -> when {
                        porcentajeGrasa < 13 -> "Muy bajo"
                        porcentajeGrasa < 19 -> "Atlético"
                        porcentajeGrasa < 23 -> "Bueno"
                        porcentajeGrasa < 30 -> "Promedio"
                        else -> "Alto"
                    }
                }
            }
            else -> { // Mujer
                when {
                    edad < 30 -> when {
                        porcentajeGrasa < 16 -> "Muy bajo"
                        porcentajeGrasa < 20 -> "Atlético"
                        porcentajeGrasa < 24 -> "Bueno"
                        porcentajeGrasa < 31 -> "Promedio"
                        else -> "Alto"
                    }
                    edad < 50 -> when {
                        porcentajeGrasa < 16 -> "Muy bajo"
                        porcentajeGrasa < 23 -> "Atlético"
                        porcentajeGrasa < 27 -> "Bueno"
                        porcentajeGrasa < 34 -> "Promedio"
                        else -> "Alto"
                    }
                    else -> when {
                        porcentajeGrasa < 16 -> "Muy bajo"
                        porcentajeGrasa < 25 -> "Atlético"
                        porcentajeGrasa < 30 -> "Bueno"
                        porcentajeGrasa < 37 -> "Promedio"
                        else -> "Alto"
                    }
                }
            }
        }
    }

    /**
     * Calcula el peso ideal según la fórmula de Robinson
     */
    fun calcularPesoIdeal(estaturaCm: Float, sexo: String): Float {
        val esHombre = sexo.lowercase() == "masculino"
        val alturaEnPulgadas = estaturaCm / 2.54f

        return if (esHombre) {
            52f + (1.9f * (alturaEnPulgadas - 60f))
        } else {
            49f + (1.7f * (alturaEnPulgadas - 60f))
        }
    }

    /**
     * Calcula el rango de peso saludable según IMC
     */
    fun calcularRangoPesoSaludable(estaturaCm: Float): Pair<Float, Float> {
        val estaturaM = estaturaCm / 100f
        val pesoMinimo = 18.5f * (estaturaM * estaturaM)
        val pesoMaximo = 24.9f * (estaturaM * estaturaM)
        return Pair(pesoMinimo, pesoMaximo)
    }

    /**
     * Evalúa si los datos antropométricos son recientes (menos de 30 días)
     */
    fun sonDatosRecientes(fechaMedicion: Long): Boolean {
        val diasTranscurridos = (System.currentTimeMillis() - fechaMedicion) / (24 * 60 * 60 * 1000)
        return diasTranscurridos <= 30
    }

    /**
     * Calcula los días transcurridos desde una medición
     */
    fun diasTranscurridos(fechaMedicion: Long): Int {
        return ((System.currentTimeMillis() - fechaMedicion) / (24 * 60 * 60 * 1000)).toInt()
    }

    /**
     * Genera recomendaciones basadas en IMC y composición corporal
     */
    fun generarRecomendacionesFisicas(
        imc: Float,
        porcentajeGrasa: Float?,
        sexo: String,
        edad: Int,
        objetivo: String
    ): List<String> {
        val recomendaciones = mutableListOf<String>()

        // Recomendaciones basadas en IMC
        when {
            imc < 18.5 -> {
                recomendaciones.add("⚠️ IMC bajo: Enfocar entrenamiento en ganancia de masa muscular")
                recomendaciones.add("🍽️ Aumentar ingesta calórica con alimentos nutritivos")
                recomendaciones.add("💪 Priorizar ejercicios de fuerza y resistencia")
            }
            imc > 30.0 -> {
                recomendaciones.add("⚠️ IMC alto: Combinar ejercicio cardiovascular con fuerza")
                recomendaciones.add("🏃 Incluir actividad cardiovascular regular")
                recomendaciones.add("⏰ Considerar sesiones más frecuentes pero menos intensas")
            }
            imc > 25.0 -> {
                recomendaciones.add("📈 Sobrepeso: Enfoque en pérdida de grasa manteniendo músculo")
                recomendaciones.add("🔥 Combinar cardio HIIT con ejercicios de fuerza")
            }
        }

        // Recomendaciones basadas en porcentaje de grasa
        porcentajeGrasa?.let { grasa ->
            val clasificacionGrasa = clasificarPorcentajeGrasa(grasa, sexo, edad)
            when (clasificacionGrasa) {
                "Alto" -> {
                    recomendaciones.add("🔥 % grasa alto: Priorizar ejercicio cardiovascular")
                    recomendaciones.add("⚡ Incluir entrenamiento HIIT 2-3 veces por semana")
                }
                "Muy bajo" -> {
                    recomendaciones.add("⚠️ % grasa muy bajo: Cuidar no perder más grasa esencial")
                    recomendaciones.add("💪 Enfocar en mantenimiento y ganancia muscular")
                }
            }
        }

        // Recomendaciones basadas en edad
        when {
            edad > 50 -> {
                recomendaciones.add("👴 +50 años: Incluir ejercicios de equilibrio y flexibilidad")
                recomendaciones.add("🦴 Priorizar ejercicios de impacto para salud ósea")
                recomendaciones.add("⏱️ Permitir más tiempo de recuperación entre sesiones")
            }
            edad < 25 -> {
                recomendaciones.add("💚 Edad joven: Aprovechar alta capacidad de recuperación")
                recomendaciones.add("🏃 Puede manejar mayor volumen e intensidad")
            }
        }

        return recomendaciones
    }

    /**
     * Determina si se requiere supervisión médica basado en datos antropométricos
     */
    fun requiereSupervisionMedica(
        imc: Float,
        edad: Int,
        antecedentesMedicos: String
    ): Pair<Boolean, String> {

        val requiereSupervision = when {
            imc > 35.0 -> true
            imc < 16.0 -> true
            edad > 65 -> true
            antecedentesMedicos.isNotEmpty() -> {
                val antecedentes = antecedentesMedicos.lowercase()
                antecedentes.contains("diabetes") ||
                        antecedentes.contains("hipertension") ||
                        antecedentes.contains("cardiaco") ||
                        antecedentes.contains("lesion") ||
                        antecedentes.contains("cirugia")
            }
            else -> false
        }

        val razon = when {
            imc > 35.0 -> "IMC > 35: Obesidad severa requiere supervisión médica"
            imc < 16.0 -> "IMC < 16: Peso muy bajo requiere evaluación médica"
            edad > 65 -> "Edad > 65 años: Recomendable evaluación médica previa"
            antecedentesMedicos.isNotEmpty() -> "Antecedentes médicos requieren supervisión especializada"
            else -> "No se requiere supervisión médica especial"
        }

        return Pair(requiereSupervision, razon)
    }
}