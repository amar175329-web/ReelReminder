package com.reelreminder.app.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.reelreminder.app.R
import com.reelreminder.app.alarm.NotificationActionReceiver
import com.reelreminder.app.data.Reel
import com.reelreminder.app.util.UrlExtractor

object NotificationHelper {
    const val CHANNEL_ID = "reel_reminders"

    fun createChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_channel_description)
            enableVibration(true)
            vibrationPattern = longArrayOf(0L, 250L, 200L, 250L)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setShowBadge(true)
        }
        manager.createNotificationChannel(channel)
    }

    fun showReminder(context: Context, reel: Reel) {
        createChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        val title = reel.note.ifBlank {
            context.getString(R.string.notification_default_title)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(reel.url)
            .setStyle(NotificationCompat.BigTextStyle().bigText(reel.url))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(ContextCompat.getColor(context, R.color.notification_color))
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
            .setAutoCancel(true)
            .addAction(
                0,
                context.getString(R.string.action_snooze),
                actionPendingIntent(context, reel.id, NotificationActionReceiver.ACTION_SNOOZE)
            )
            .addAction(
                0,
                context.getString(R.string.action_done),
                actionPendingIntent(context, reel.id, NotificationActionReceiver.ACTION_DONE)
            )

        if (UrlExtractor.isValidHttpUrl(reel.url)) {
            val openIntent = Intent(Intent.ACTION_VIEW, Uri.parse(reel.url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val contentIntent = PendingIntent.getActivity(
                context,
                contentRequestCode(reel.id),
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.setContentIntent(contentIntent)
        }

        manager.notify(notificationId(reel.id), builder.build())
    }

    fun cancel(context: Context, reelId: Long) {
        NotificationManagerCompat.from(context).cancel(notificationId(reelId))
    }

    private fun actionPendingIntent(
        context: Context,
        reelId: Long,
        action: String
    ): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            this.action = action
            putExtra(NotificationActionReceiver.EXTRA_REEL_ID, reelId)
        }
        return PendingIntent.getBroadcast(
            context,
            actionRequestCode(reelId, action),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun notificationId(reelId: Long): Int =
        (reelId xor (reelId ushr 32)).toInt()

    private fun contentRequestCode(reelId: Long): Int =
        notificationId(reelId) xor 0x20000000

    private fun actionRequestCode(reelId: Long, action: String): Int =
        notificationId(reelId) xor action.hashCode()
}
