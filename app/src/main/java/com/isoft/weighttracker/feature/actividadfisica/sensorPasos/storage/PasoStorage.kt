package com.isoft.weighttracker.feature.actividadfisica.sensorPasos.storage

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import java.util.*

private val Context.dataStore by preferencesDataStore(name = "paso_storage")

object PasoStorage {
    private val STEP_BASE = intPreferencesKey("step_base")
    private val FECHA_BASE = longPreferencesKey("fecha_base")
    private val PASOS_HOY = intPreferencesKey("pasos_hoy")
    private val SINCRONIZADO = booleanPreferencesKey("sincronizado")
    private val CONTADOR_ACTIVO = booleanPreferencesKey("contador_activo")

    suspend fun guardarBase(context: Context, base: Int) {
        context.dataStore.edit {
            it[STEP_BASE] = base
            it[FECHA_BASE] = hoyMedianoche()
        }
    }

    suspend fun leerBase(context: Context): Int {
        return context.dataStore.data.first()[STEP_BASE] ?: 0
    }

    suspend fun guardarPasosHoy(context: Context, pasos: Int) {
        context.dataStore.edit {
            it[PASOS_HOY] = pasos
            it[SINCRONIZADO] = false
        }
    }

    suspend fun leerPasosHoy(context: Context): Int {
        return context.dataStore.data.first()[PASOS_HOY] ?: 0
    }

    suspend fun marcarComoSincronizado(context: Context) {
        context.dataStore.edit {
            it[SINCRONIZADO] = true
        }
    }

    suspend fun estaSincronizado(context: Context): Boolean {
        return context.dataStore.data.first()[SINCRONIZADO] ?: true
    }

    suspend fun marcarContadorActivo(context: Context, activo: Boolean) {
        context.dataStore.edit {
            it[CONTADOR_ACTIVO] = activo
        }
    }

    suspend fun contadorEstabaActivo(context: Context): Boolean {
        return context.dataStore.data.first()[CONTADOR_ACTIVO] ?: false
    }

    suspend fun esNuevoDia(context: Context): Boolean {
        val guardado = context.dataStore.data.first()[FECHA_BASE] ?: 0L
        return guardado != hoyMedianoche()
    }

    private fun hoyMedianoche(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}