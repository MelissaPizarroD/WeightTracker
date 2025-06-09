package com.isoft.weighttracker.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.isoft.weighttracker.MainActivity
import com.isoft.weighttracker.R
import com.isoft.weighttracker.feature.reporteAvance.model.TipoReporte

object ReportesNotificationHelper {

    // 🎯 Canales específicos para reportes
    private const val RETRO_CHANNEL_ID = "retroalimentacion_channel"
    private const val RETRO_CHANNEL_NAME = "Retroalimentaciones"
    private const val RETRO_CHANNEL_DESCRIPTION = "Nuevas retroalimentaciones de profesionales"

    private const val REPORTE_CHANNEL_ID = "reportes_channel"
    private const val REPORTE_CHANNEL_NAME = "Reportes de Avance"
    private const val REPORTE_CHANNEL_DESCRIPTION = "Notificaciones sobre reportes de avance"

    private const val PROFESIONAL_CHANNEL_ID = "profesional_channel"
    private const val PROFESIONAL_CHANNEL_NAME = "Notificaciones Profesionales"
    private const val PROFESIONAL_CHANNEL_DESCRIPTION = "Notificaciones para profesionales"

    // 🎨 IDs únicos para diferentes tipos de notificaciones
    private const val RETRO_NOTIFICATION_ID = 1000
    private const val REPORTE_NOTIFICATION_ID = 2000
    private const val PROFESIONAL_NOTIFICATION_ID = 3000

    /**
     * 💬 Notifica al usuario cuando recibe una nueva retroalimentación
     */
    fun notificarNuevaRetroalimentacion(
        context: Context,
        reporteId: String,
        nombreProfesional: String,
        rolProfesional: String,
        contenidoPreview: String = ""
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createRetroChannelIfNeeded(notificationManager)

        // 🎯 Intent específico para abrir el reporte
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "detalleReporte")
            putExtra("reporteId", reporteId)
            putExtra("from_notification", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            reporteId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 🎨 Emoji específico por rol
        val emojiRol = when (rolProfesional.lowercase()) {
            "nutricionista" -> "🥗"
            "entrenador" -> "💪"
            "medico", "médico" -> "👨‍⚕️"
            "psicologo", "psicólogo" -> "🧠"
            else -> "👨‍💼"
        }

        val titulo = "Nueva retroalimentación $emojiRol"
        val mensaje = "$nombreProfesional ($rolProfesional) ha comentado tu reporte"
        val mensajeExtendido = if (contenidoPreview.isNotBlank()) {
            "$mensaje\n\n\"${contenidoPreview.take(100)}${if (contenidoPreview.length > 100) "..." else ""}\""
        } else {
            mensaje
        }

        // 🔔 Construir notificación con estilo expandido
        val notification = NotificationCompat.Builder(context, RETRO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensajeExtendido))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setGroup("retroalimentaciones")
            .addAction(
                R.drawable.ic_notification,
                "Ver reporte",
                pendingIntent
            )
            .build()

        notificationManager.notify(RETRO_NOTIFICATION_ID + reporteId.hashCode(), notification)
    }

    /**
     * 📊 Notifica al usuario cuando su reporte se guarda exitosamente
     */
    fun notificarReporteCreado(
        context: Context,
        tipoReporte: TipoReporte,
        reporteId: String,
        profesionalesAsociados: Int = 0
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createReporteChannelIfNeeded(notificationManager)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "detalleReporte")
            putExtra("reporteId", reporteId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            reporteId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val emojiTipo = when (tipoReporte) {
            TipoReporte.DIARIO -> "📅"
            TipoReporte.SEMANAL -> "📊"
            TipoReporte.QUINCENAL -> "📈"
            TipoReporte.MENSUAL -> "📆"
        }

        val titulo = "Reporte $emojiTipo creado exitosamente"
        val mensaje = if (profesionalesAsociados > 0) {
            "Tu reporte ${tipoReporte.name.lowercase()} ha sido enviado a $profesionalesAsociados profesional(es)"
        } else {
            "Tu reporte ${tipoReporte.name.lowercase()} ha sido guardado"
        }

        val notification = NotificationCompat.Builder(context, REPORTE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setGroup("reportes")
            .build()

        notificationManager.notify(REPORTE_NOTIFICATION_ID + reporteId.hashCode(), notification)
    }

    /**
     * 👨‍⚕️ Notifica a profesionales cuando hay un nuevo reporte disponible
     */
    fun notificarProfesionalNuevoReporte(
        context: Context,
        nombreUsuario: String,
        tipoReporte: TipoReporte,
        reporteId: String? = null
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createProfesionalChannelIfNeeded(notificationManager)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "reportesUsuarios")
            reporteId?.let { putExtra("reporteId", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            nombreUsuario.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val emojiTipo = when (tipoReporte) {
            TipoReporte.DIARIO -> "📅"
            TipoReporte.SEMANAL -> "📊"
            TipoReporte.QUINCENAL -> "📈"
            TipoReporte.MENSUAL -> "📆"
        }

        val titulo = "Nuevo reporte disponible $emojiTipo"
        val mensaje = "$nombreUsuario ha creado un reporte ${tipoReporte.name.lowercase()}"

        val notification = NotificationCompat.Builder(context, PROFESIONAL_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$mensaje\n\nToca para revisar y agregar retroalimentación"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setGroup("reportes_profesional")
            .addAction(
                R.drawable.ic_notification,
                "Revisar",
                pendingIntent
            )
            .build()

        notificationManager.notify(PROFESIONAL_NOTIFICATION_ID + nombreUsuario.hashCode(), notification)
    }

    /**
     * 🧹 Cancela notificaciones específicas (útil para limpiar)
     */
    fun cancelarNotificacionesReporte(context: Context, reporteId: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(RETRO_NOTIFICATION_ID + reporteId.hashCode())
        notificationManager.cancel(REPORTE_NOTIFICATION_ID + reporteId.hashCode())
    }

    // 🔧 Funciones privadas para crear canales
    private fun createRetroChannelIfNeeded(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val existingChannel = notificationManager.getNotificationChannel(RETRO_CHANNEL_ID)
            if (existingChannel == null) {
                val channel = NotificationChannel(
                    RETRO_CHANNEL_ID,
                    RETRO_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = RETRO_CHANNEL_DESCRIPTION
                    enableVibration(true)
                    enableLights(true)
                    setShowBadge(true)
                    vibrationPattern = longArrayOf(0, 250, 250, 250)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    private fun createReporteChannelIfNeeded(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val existingChannel = notificationManager.getNotificationChannel(REPORTE_CHANNEL_ID)
            if (existingChannel == null) {
                val channel = NotificationChannel(
                    REPORTE_CHANNEL_ID,
                    REPORTE_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = REPORTE_CHANNEL_DESCRIPTION
                    enableVibration(true)
                    setShowBadge(true)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    private fun createProfesionalChannelIfNeeded(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val existingChannel = notificationManager.getNotificationChannel(PROFESIONAL_CHANNEL_ID)
            if (existingChannel == null) {
                val channel = NotificationChannel(
                    PROFESIONAL_CHANNEL_ID,
                    PROFESIONAL_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = PROFESIONAL_CHANNEL_DESCRIPTION
                    enableVibration(true)
                    enableLights(true)
                    setShowBadge(true)
                    vibrationPattern = longArrayOf(0, 500, 250, 500)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
}