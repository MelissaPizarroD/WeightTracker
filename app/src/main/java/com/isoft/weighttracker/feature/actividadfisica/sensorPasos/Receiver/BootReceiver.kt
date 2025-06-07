package com.isoft.weighttracker.feature.actividadfisica.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.isoft.weighttracker.feature.actividadfisica.sensorPasos.service.PasosBackgroundService
import com.isoft.weighttracker.feature.actividadfisica.sensorPasos.storage.PasoStorage
import com.isoft.weighttracker.feature.actividadfisica.sensorPasos.worker.StepSyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Dispositivo reiniciado, restaurando servicios")

            CoroutineScope(Dispatchers.IO).launch {
                // Verificar si el contador estaba activo antes del reinicio
                val estabaActivo = PasoStorage.contadorEstabaActivo(context)

                if (estabaActivo) {
                    // Reiniciar servicio de pasos con compatibilidad de API
                    val serviceIntent = Intent(context, PasosBackgroundService::class.java)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }

                    // Programar WorkManager
                    StepSyncWorker.programar(context)
                }
            }
        }
    }
}