package com.reelreminder.app.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.reelreminder.app.data.Reel

class AlarmScheduler(context: Context) {
    private val appContext = context.applicationContext
    private val alarmManager =
        appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun canScheduleExact(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    fun schedule(reel: Reel, triggerAt: Long = reel.nextTriggerAt) {
        if (triggerAt <= 0L) return
        setAlarm(firePendingIntent(reel.id), triggerAt)
    }

    fun scheduleSnooze(reelId: Long, triggerAt: Long) {
        if (triggerAt <= 0L) return
        setAlarm(snoozePendingIntent(reelId), triggerAt)
    }

    fun cancel(reelId: Long) {
        alarmManager.cancel(firePendingIntent(reelId))
        alarmManager.cancel(snoozePendingIntent(reelId))
    }

    private fun setAlarm(pendingIntent: PendingIntent, triggerAt: Long) {
        val safeTrigger = maxOf(triggerAt, System.currentTimeMillis() + 1_000L)
        if (canScheduleExact()) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    safeTrigger,
                    pendingIntent
                )
                return
            } catch (_: SecurityException) {
                // Exact-alarm access was revoked between the check and this call.
            } catch (_: IllegalStateException) {
                // Some OEM builds reject exact alarms. Fall through to an inexact alarm.
            }
        }
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, safeTrigger, pendingIntent)
    }

    private fun firePendingIntent(reelId: Long): PendingIntent {
        val intent = Intent(appContext, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_FIRE
            putExtra(AlarmReceiver.EXTRA_REEL_ID, reelId)
            putExtra(AlarmReceiver.EXTRA_IS_SNOOZE, false)
        }
        return PendingIntent.getBroadcast(
            appContext,
            requestCode(reelId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun snoozePendingIntent(reelId: Long): PendingIntent {
        val intent = Intent(appContext, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_FIRE
            putExtra(AlarmReceiver.EXTRA_REEL_ID, reelId)
            putExtra(AlarmReceiver.EXTRA_IS_SNOOZE, true)
        }
        return PendingIntent.getBroadcast(
            appContext,
            snoozeRequestCode(reelId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        fun requestCode(reelId: Long): Int =
            (reelId xor (reelId ushr 32)).toInt()

        fun snoozeRequestCode(reelId: Long): Int =
            requestCode(reelId) xor 0x40000000
    }
}
