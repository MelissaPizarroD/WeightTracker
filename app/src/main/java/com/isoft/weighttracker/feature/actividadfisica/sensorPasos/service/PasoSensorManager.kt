package com.isoft.weighttracker.feature.actividadfisica.sensorPasos.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.isoft.weighttracker.feature.actividadfisica.sensorPasos.storage.PasoStorage
import kotlinx.coroutines.*
// No necesitamos AtomicBoolean, usaremos una variable normal con @Volatile

class PasoSensorManager(
    private val context: Context,
    private val onPasosActualizados: (Int) -> Unit
) : SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var stepCounterSensor: Sensor? = null
    private var stepDetectorSensor: Sensor? = null
    private var usingStepCounter = true

    // Control de estado para evitar eventos después de detener
    @Volatile
    private var isActive = false
    private var job: Job? = null

    fun iniciar() {
        if (isActive) {
            Log.w("PasoSensor", "Sensor ya está activo, ignorando inicialización")
            return
        }

        try {
            sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

            // Intentar con STEP_COUNTER primero
            stepCounterSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            stepDetectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

            if (stepCounterSensor != null) {
                Log.d("PasoSensor", "Usando sensor TYPE_STEP_COUNTER")
                val registered = sensorManager?.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL)
                if (registered == true) {
                    usingStepCounter = true
                    isActive = true
                    Log.d("PasoSensor", "Sensor STEP_COUNTER registrado exitosamente")
                } else {
                    Log.e("PasoSensor", "Error al registrar STEP_COUNTER")
                }
            } else if (stepDetectorSensor != null) {
                Log.d("PasoSensor", "Usando sensor TYPE_STEP_DETECTOR (menos preciso)")
                val registered = sensorManager?.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL)
                if (registered == true) {
                    usingStepCounter = false
                    isActive = true
                    Log.d("PasoSensor", "Sensor STEP_DETECTOR registrado exitosamente")
                } else {
                    Log.e("PasoSensor", "Error al registrar STEP_DETECTOR")
                }
            } else {
                Log.w("PasoSensor", "No se encontró ningún sensor de pasos")
            }
        } catch (e: Exception) {
            Log.e("PasoSensor", "Error al inicializar sensor", e)
            isActive = false
        }
    }

    fun detener() {
        Log.d("PasoSensor", "Deteniendo sensor de pasos...")

        // Marcar como inactivo PRIMERO
        isActive = false

        try {
            // Cancelar job activo si existe
            job?.cancel()
            job = null

            // Desregistrar listener
            sensorManager?.unregisterListener(this)

            // Limpiar referencias
            sensorManager = null
            stepCounterSensor = null
            stepDetectorSensor = null

            Log.d("PasoSensor", "Sensor de pasos detenido correctamente")
        } catch (e: Exception) {
            Log.e("PasoSensor", "Error al detener sensor", e)
        }
    }

    fun tieneSensor(): Boolean {
        return try {
            val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            manager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null ||
                    manager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null
        } catch (e: Exception) {
            Log.e("PasoSensor", "Error verificando sensores", e)
            false
        }
    }

    fun estaActivo(): Boolean {
        return isActive
    }

    override fun onSensorChanged(event: SensorEvent) {
        // Verificar si todavía estamos activos antes de procesar
        if (!isActive) {
            Log.d("PasoSensor", "Evento recibido pero sensor ya está inactivo, ignorando")
            return
        }

        val pasosActuales = event.values[0].toInt()
        Log.d("PasoSensor", "Evento de sensor recibido. Tipo: ${event.sensor.type}, Valor: $pasosActuales")

        // Cancelar job anterior si existe
        job?.cancel()

        job = CoroutineScope(Dispatchers.IO).launch {
            try {
                // Verificar nuevamente si seguimos activos
                if (!isActive) {
                    Log.d("PasoSensor", "Sensor inactivo durante procesamiento, cancelando")
                    return@launch
                }

                if (usingStepCounter) {
                    // Lógica para STEP_COUNTER
                    val base = if (PasoStorage.esNuevoDia(context)) {
                        Log.d("PasoSensor", "Nuevo día detectado, guardando nueva base")
                        PasoStorage.guardarBase(context, pasosActuales)
                        pasosActuales
                    } else {
                        val savedBase = PasoStorage.leerBase(context)
                        if (pasosActuales < savedBase) {
                            Log.d("PasoSensor", "Dispositivo reiniciado, ajustando base")
                            PasoStorage.guardarBase(context, pasosActuales)
                            pasosActuales
                        } else {
                            savedBase
                        }
                    }

                    val pasosHoy = pasosActuales - base
                    Log.d("PasoSensor", "Total: $pasosActuales, Base: $base, Hoy: $pasosHoy")

                    // Verificar una vez más antes de actualizar UI
                    if (isActive) {
                        withContext(Dispatchers.Main) {
                            if (isActive) {
                                onPasosActualizados(pasosHoy)
                            }
                        }
                    }
                } else {
                    // Lógica para STEP_DETECTOR (suma incremental)
                    val pasosHoy = PasoStorage.leerBase(context) + pasosActuales
                    PasoStorage.guardarBase(context, pasosHoy)

                    if (isActive) {
                        withContext(Dispatchers.Main) {
                            if (isActive) {
                                onPasosActualizados(pasosHoy)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) {
                    Log.d("PasoSensor", "Procesamiento cancelado correctamente")
                } else {
                    Log.e("PasoSensor", "Error procesando pasos", e)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (isActive) {
            Log.d("PasoSensor", "Precisión cambiada: $accuracy")
        }
    }
}