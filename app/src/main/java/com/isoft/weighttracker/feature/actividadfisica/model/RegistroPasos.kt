package com.isoft.weighttracker.feature.actividadfisica.model

data class RegistroPasos(
    val id: String? = null,
    val fecha: Long = System.currentTimeMillis(),
    val pasos: Int = 0
)