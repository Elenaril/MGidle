package com.eggmonsters.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.eggmonsters.app.MainActivity

object NotificationHelper {

    private const val CHANNEL_ID = "eggmonsters_care"
    private const val CHANNEL_NAME = "Cuidados de tu criatura"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Avisos cuando tu monstruo necesita atención"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun notifyHungry(context: Context, creatureName: String) {
        show(
            context,
            id = 1001,
            title = "$creatureName tiene hambre 🍽️",
            body = "¡Vuelve y alimenta a tu criatura antes de que se ponga triste!"
        )
    }

    fun notifyLowEnergy(context: Context, creatureName: String) {
        show(
            context,
            id = 1002,
            title = "$creatureName está agotado 😴",
            body = "Necesita dormir un rato para recuperar energía."
        )
    }

    fun notifyDirty(context: Context, creatureName: String) {
        show(
            context,
            id = 1003,
            title = "$creatureName está sucio 🧼",
            body = "Un poco de limpieza le subirá la felicidad."
        )
    }

    fun notifyMissYou(context: Context, creatureName: String) {
        show(
            context,
            id = 1004,
            title = "$creatureName te echa de menos 💕",
            body = "Hace rato que no lo acaricias. ¡Ven a jugar!"
        )
    }

    fun notifyReadyToEvolve(context: Context, creatureName: String) {
        show(
            context,
            id = 1005,
            title = "¡$creatureName puede evolucionar! ⭐",
            body = "Sigue cuidándolo para ver su siguiente forma."
        )
    }

    private fun show(context: Context, id: Int, title: String, body: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_my_calendar) // icono del sistema por simplicidad
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (_: SecurityException) {
            // Permiso de notificaciones denegado
        }
    }
}
