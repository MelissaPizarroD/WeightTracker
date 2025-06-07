package com.isoft.weighttracker.feature.actividadfisica.sensorPasos.worker

import android.content.Context
import androidx.work.*
import com.isoft.weighttracker.feature.actividadfisica.data.ActividadFisicaRepository
import com.isoft.weighttracker.feature.actividadfisica.sensorPasos.storage.PasoStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class StepSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            // Leer pasos almacenados localmente
            val pasos = PasoStorage.leerPasosHoy(applicationContext)

            if (pasos > 0) {
                val repository = ActividadFisicaRepository()
                val success = repository.registrarPasosHoy(pasos)

                if (success) {
                    // Marcar como sincronizado
                    PasoStorage.marcarComoSincronizado(applicationContext)
                    Result.success()
                } else {
                    Result.retry()
                }
            } else {
                Result.success()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "step_sync_work"

        fun programar(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true) // Solo cuando batería no esté baja
                .build()

            val work = PeriodicWorkRequestBuilder<StepSyncWorker>(
                repeatInterval = 2, // Cada 2 horas (mínimo permitido es 15min)
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    work
                )
        }

        fun cancelar(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}