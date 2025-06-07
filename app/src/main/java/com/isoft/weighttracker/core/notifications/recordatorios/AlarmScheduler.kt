package com.isoft.weighttracker.core.notifications.recordatorios

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.isoft.weighttracker.core.notifications.recordatorios.NotificationReceiver
import java.util.Calendar
import java.util.Locale

object AlarmScheduler {

    fun programarRepeticion(context: Context, frecuencia: String, hora: Int, minuto: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("frecuencia", frecuencia)
            putExtra("hora", hora)
            putExtra("minuto", minuto)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = calcularProximaFecha(frecuencia, hora, minuto)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()
        ) {
            Log.w("⛔️AlarmScheduler", "No se puede programar alarma exacta, falta permiso")
            Toast.makeText(
                context,
                "⚠️ Permiso de alarmas exactas denegado. Actívalo en configuración.",
                Toast.LENGTH_LONG
            ).show()

            // Opcional: llevar al usuario a la configuración
            val intentConfig = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            intentConfig.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intentConfig)

            return
        }

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
            Log.d("✅AlarmScheduler", "Alarma programada para ${calendar.time}")
        } catch (e: SecurityException) {
            Log.e("⛔️AlarmScheduler", "No se pudo programar la alarma: ${e.message}")
            Toast.makeText(
                context,
                "❌ Error: No se pudo programar el recordatorio.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun calcularProximaFecha(frecuencia: String, hora: Int, minuto: Int): Calendar {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, minuto)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // 🔁 SIEMPRE sumamos desde ahora el intervalo de la frecuencia
        when (frecuencia.lowercase(Locale.getDefault())) {
            "diaria" -> calendar.add(Calendar.DAY_OF_YEAR, 1)
            "semanal" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            "quincenal" -> calendar.add(Calendar.DAY_OF_YEAR, 14)
            "mensual" -> calendar.add(Calendar.MONTH, 1)
            else -> calendar.add(Calendar.DAY_OF_YEAR, 1) // fallback
        }

        return calendar
    }

    fun cancelarAlarma(context: Context) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)

        Log.d("🔕AlarmScheduler", "Alarma cancelada")
    }
}