package com.isoft.weighttracker.feature.comidas.model

data class Comida(
    val id: String? = null,
    val fecha: Long = System.currentTimeMillis(),
    val comida: String = "",
    val comidaDelDia: String = "",
    val calorias: Int = 0
)