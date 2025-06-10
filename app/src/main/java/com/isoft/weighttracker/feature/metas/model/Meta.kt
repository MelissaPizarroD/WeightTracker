package com.isoft.weighttracker.feature.metas.model

data class Meta(
    var id: String? = null,
    var pesoInicial: Float = 0f, // Calculado automáticamente al crearla
    var pesoObjetivo: Float = 0f,
    var fechaInicio: Long = System.currentTimeMillis(),
    var fechaLimite: Long = 0L,
    var objetivo: String = "",
    var cumplida: Boolean = false,
    var activa: Boolean = true, // Nueva propiedad para controlar si la meta está activa
    var vencida: Boolean = false // Nueva propiedad para metas vencidas
) {
    // Constructor sin argumentos para Firebase
    constructor() : this(
        id = null,
        pesoInicial = 0f,
        pesoObjetivo = 0f,
        fechaInicio = System.currentTimeMillis(),
        fechaLimite = 0L,
        objetivo = "",
        cumplida = false,
        activa = true,
        vencida = false
    )
}