package com.isoft.weighttracker.feature.actividadfisica.sensorPasos.service

import android.app.*
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.isoft.weighttracker.R
import com.isoft.weighttracker.feature.actividadfisica.sensorPasos.storage.PasoStorage
import com.isoft.weighttracker.feature.actividadfisica.sensorPasos.worker.StepSyncWorker
import kotlinx.coroutines.*
import kotlin.math.abs

class PasosBackgroundService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private var stepsSinceBoot = 0
    private var lastStepCount = 0

    // Evitar WakeLock completo, usar detección inteligente
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Batching para reducir escrituras
    private var pendingSteps = 0
    private var lastSaveTime = 0L
    private val SAVE_INTERVAL = 30_000L // Guardar cada 30 segundos máximo

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        // Programar WorkManager para sincronización
        StepSyncWorker.programar(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        stepCounterSensor?.let { sensor ->
            // Usar SENSOR_DELAY_NORMAL para ahorrar batería
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d("PasosService", "Sensor registrado exitosamente")
        } ?: run {
            Log.w("PasosService", "Sensor no disponible, deteniendo servicio")
            stopSelf()
        }

        // START_STICKY para que se reinicie automáticamente
        return START_STICKY
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        serviceScope.cancel()
        StepSyncWorker.cancelar(this)
        super.onDestroy()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            stepsSinceBoot = event.values[0].toInt()

            // Solo procesar si hay cambio significativo
            if (stepsSinceBoot != lastStepCount) {
                lastStepCount = stepsSinceBoot
                procesarPasos()
            }
        }
    }

    private fun procesarPasos() {
        serviceScope.launch {
            try {
                val currentTime = System.currentTimeMillis()
                val base = if (PasoStorage.esNuevoDia(this@PasosBackgroundService)) {
                    PasoStorage.guardarBase(this@PasosBackgroundService, stepsSinceBoot)
                    stepsSinceBoot
                } else {
                    val savedBase = PasoStorage.leerBase(this@PasosBackgroundService)
                    // Manejar reinicio del dispositivo
                    if (stepsSinceBoot < savedBase) {
                        PasoStorage.guardarBase(this@PasosBackgroundService, stepsSinceBoot)
                        stepsSinceBoot
                    } else {
                        savedBase
                    }
                }

                val stepsToday = maxOf(0, stepsSinceBoot - base)
                pendingSteps = stepsToday

                // Guardar solo si han pasado 30 segundos o hay cambio significativo (>10 pasos)
                val shouldSave = currentTime - lastSaveTime > SAVE_INTERVAL ||
                        abs(stepsToday - PasoStorage.leerPasosHoy(this@PasosBackgroundService)) > 10

                if (shouldSave) {
                    PasoStorage.guardarPasosHoy(this@PasosBackgroundService, stepsToday)
                    lastSaveTime = currentTime
                }

                withContext(Dispatchers.Main) {
                    updateNotification(stepsToday)
                }
            } catch (e: Exception) {
                Log.e("PasosService", "Error procesando pasos", e)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d("PasosService", "Precisión del sensor: $accuracy")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Contador de Pasos",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Contador de pasos en segundo plano"
                setShowBadge(false)
                enableVibration(false)
                setSound(null, null)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Contador de Pasos")
            .setContentText("Contando pasos...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setShowWhen(false)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(steps: Int) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pasos hoy")
            .setContentText("$steps pasos")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setShowWhen(false)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager?.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val CHANNEL_ID = "pasos_channel"
        private const val NOTIFICATION_ID = 1
    }
}