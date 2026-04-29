package pe.aioo.openmoa.hardware

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import pe.aioo.openmoa.R
import pe.aioo.openmoa.settings.SettingsPreferences

object OverlayPermissionNotifier {

    private const val CHANNEL_ID = "openmoa_overlay_permission"
    private const val NOTIFICATION_ID = 4212

    fun notifyOnce(context: Context) {
        if (Settings.canDrawOverlays(context)) return
        if (SettingsPreferences.getOverlayPermissionNotified(context)) return
        SettingsPreferences.setOverlayPermissionNotified(context, true)

        if (canPostNotifications(context)) {
            postNotification(context)
        } else {
            val appCtx = context.applicationContext
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(
                    appCtx,
                    R.string.overlay_permission_notification_message,
                    Toast.LENGTH_LONG,
                ).show()
            }
        }
    }

    private fun canPostNotifications(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun postNotification(context: Context) {
        ensureChannel(context)

        val overlayIntent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}"),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getActivity(context, 0, overlayIntent, flags)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.overlay_permission_notification_title))
            .setContentText(context.getString(R.string.overlay_permission_notification_message))
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    context.getString(R.string.overlay_permission_notification_message)
                )
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.overlay_permission_notification_title),
            NotificationManager.IMPORTANCE_HIGH,
        )
        manager.createNotificationChannel(channel)
    }

    fun cancel(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(NOTIFICATION_ID)
    }
}
