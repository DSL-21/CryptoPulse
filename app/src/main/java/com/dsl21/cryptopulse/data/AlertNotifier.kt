package com.dsl21.cryptopulse.data

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.dsl21.cryptopulse.MainActivity

object AlertNotifier {
    private const val CHANNEL_ID = "cryptopulse_price_alerts"

    fun hasNotificationPermission(context: Context): Boolean {
        return Build.VERSION.SDK_INT < 33 ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    fun notifyPriceAlert(context: Context, hit: PriceAlertHit) {
        if (!hasNotificationPermission(context)) return

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "CryptoPulse price alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "One-time local notifications when a saved price threshold is reached."
                }
            )
        }

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            hit.rule.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val condition = if (hit.rule.direction == AlertDirection.ABOVE) "reached or exceeded" else "fell to or below"
        val message = hit.rule.symbol + " " + condition + " " +
            format(hit.rule.targetPrice) + " " + hit.quoteCurrency +
            ". Current: " + format(hit.currentPrice) + " " + hit.quoteCurrency + "."

        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("CryptoPulse price alert")
            .setContentText(message)
            .setStyle(Notification.BigTextStyle().bigText(message))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(hit.rule.id.hashCode(), notification)
    }

    private fun format(value: Double): String =
        java.text.DecimalFormat("0.########").format(value)
}
