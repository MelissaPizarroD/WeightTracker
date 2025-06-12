package com.isoft.weighttracker.feature.planes.model

import java.util.UUID

/**
 * Modelo actualizado para Plan Nutricional basado en categorías de alimentos
 * Reemplaza el modelo anterior que usaba comidas individuales
 */
data class PlanNutricional(
    var id: String = UUID.randomUUID().toString(),
    var fechaCreacion: Long = System.currentTimeMillis(),
    var usuarioId: String = "",
    var profesionalId: String = "",
    var nombreProfesional: String = "",
    var estado: EstadoPlan = EstadoPlan.ACTIVO,

    // Configuración del plan
    var frecuencia: String = "Lunes a Sábado", // "Lunes a Sábado", "Lunes a Domingo", "Lunes a Viernes"
    var repeticion: String = "diaria", // "cada 3 dias", "cada 2 dias", "diaria"

    // 🥔 Categorías de alimentos - Grupos principales (SIN ACEITE DE OLIVA)
    var patatasArrozPanPasta: CategoriaAlimento = CategoriaAlimento(),
    var verdurasHortalizas: CategoriaAlimento = CategoriaAlimento(),
    var frutas: CategoriaAlimento = CategoriaAlimento(),
    var lecheDerivados: CategoriaAlimento = CategoriaAlimento(),
    var pescados: CategoriaAlimento = CategoriaAlimento(),
    var carnesMagrasAvesHuevos: CategoriaAlimento = CategoriaAlimento(),
    var legumbres: CategoriaAlimento = CategoriaAlimento(),
    var frutoSecos: CategoriaAlimento = CategoriaAlimento(),

    // ❌ Consumo ocasional y moderado
    var consumoOcasional: ConsumoOcasional = ConsumoOcasional(),

    // Metadatos del plan
    var fechaActivacion: Long? = null,
    var fechaDesactivacion: Long? = null,
    var observacionesGenerales: String = "" // Observaciones generales del plan completo
) {
    // Constructor vacío requerido por Firebase
    constructor() : this(
        id = UUID.randomUUID().toString(),
        fechaCreacion = System.currentTimeMillis(),
        usuarioId = "",
        profesionalId = "",
        nombreProfesional = "",
        estado = EstadoPlan.ACTIVO,
        frecuencia = "Lunes a Sábado",
        repeticion = "diaria",
        patatasArrozPanPasta = CategoriaAlimento(),
        verdurasHortalizas = CategoriaAlimento(),
        frutas = CategoriaAlimento(),
        lecheDerivados = CategoriaAlimento(),
        pescados = CategoriaAlimento(),
        carnesMagrasAvesHuevos = CategoriaAlimento(),
        legumbres = CategoriaAlimento(),
        frutoSecos = CategoriaAlimento(),
        consumoOcasional = ConsumoOcasional(),
        fechaActivacion = null,
        fechaDesactivacion = null,
        observacionesGenerales = ""
    )
}