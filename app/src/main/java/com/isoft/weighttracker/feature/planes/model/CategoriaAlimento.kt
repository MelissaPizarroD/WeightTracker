package com.isoft.weighttracker.feature.planes.model

/**
 * Modelo para representar una categoría específica de alimentos
 * en un plan nutricional (ej: frutas, verduras, carnes, etc.)
 */
data class CategoriaAlimento(
    var frecuenciaDiaria: String = "", // "4-6 raciones", "≥ 2", etc. (frecuencia personalizada)
    var racionesPorDia: Int = 0, // Número de raciones diarias
    var tipoespecifico: String = "", // Para categorías como leche (Leche, Yogur, Queso), carnes, etc.
    var pesoPorRacion: Double = 0.0, // Peso en gramos o ml por ración
    var racionesPorSemana: Int = 0, // Para categorías semanales como pescado, legumbres
    var alternarConOtros: Boolean = false, // Para carnes/huevos - alternar entre diferentes tipos
    var observaciones: String = "", // Observaciones específicas de la categoría
    var activo: Boolean = true // Si esta categoría está activa en el plan
) {
    // Constructor vacío requerido por Firebase
    constructor() : this("", 0, "", 0.0, 0, false, "", true)
}