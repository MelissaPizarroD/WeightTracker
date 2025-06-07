package com.isoft.weighttracker.feature.metas.model

data class Meta(
    val id: String? = null,
    val pesoInicial: Float = 0f, // Calculado automáticamente al crearla
    val pesoObjetivo: Float = 0f,
    val fechaInicio: Long = System.currentTimeMillis(),
    val fechaLimite: Long = 0L,
    val objetivo: String = "",
    val cumplida: Boolean = false,
    val activa: Boolean = true, // Nueva propiedad para controlar si la meta está activa
    val vencida: Boolean = false // Nueva propiedad para metas vencidas
)