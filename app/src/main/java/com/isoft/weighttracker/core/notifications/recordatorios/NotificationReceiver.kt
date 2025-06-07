package com.isoft.weighttracker.core.notifications.recordatorios

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.isoft.weighttracker.MainActivity
import com.isoft.weighttracker.R

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val frecuencia = intent?.getStringExtra("frecuencia") ?: "semanal"
        val hora = intent?.getIntExtra("hora", 10) ?: 10
        val minuto = intent?.getIntExtra("minuto", 0) ?: 0

        val channelId = "registro_channel"
        val nombreCanal = "Recordatorio de registro"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                nombreCanal,
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("¡Es hora de tu medición!")
            .setContentText("Recuerda registrar tus datos antropométricos.")
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1001, builder.build())

        // 🔁 Reprograma para la próxima vez
        AlarmScheduler.programarRepeticion(context, frecuencia, hora, minuto)
    }
}