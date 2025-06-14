package com.isoft.weighttracker.core.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "", // "persona", "entrenador", "nutricionista"
    val photoUrl: String? = null
)