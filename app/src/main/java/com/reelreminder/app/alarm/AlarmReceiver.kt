package com.reelreminder.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.reelreminder.app.ReelReminderApplication
import com.reelreminder.app.data.ScheduleType
import com.reelreminder.app.notifications.NotificationHelper
import com.reelreminder.app.util.NextTriggerCalculator
import java.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_FIRE) return

        val reelId = intent.getLongExtra(EXTRA_REEL_ID, -1L)
        if (reelId <= 0L) return
        val isSnooze = intent.getBooleanExtra(EXTRA_IS_SNOOZE, false)

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as ReelReminderApplication
                val reel = app.repository.get(reelId) ?: return@launch
                if (reel.paused) return@launch

                if (isSnooze) {
                    // A snooze is a one-off echo of an existing notification.
                    runCatching { NotificationHelper.showReminder(context, reel) }
                    return@launch
                }

                if (reel.done) return@launch

                runCatching { NotificationHelper.showReminder(context, reel) }

                when (reel.scheduleType) {
                    ScheduleType.ONCE -> {
                        // A one-time reminder is complete as soon as it fires.
                        app.repository.markDone(reel.id)
                    }

                    ScheduleType.DAILY, ScheduleType.WEEKLY -> {
                        // Base the next occurrence on the scheduled time, not
                        // just "now", so an early or late alarm cannot skip a
                        // whole day.
                        val scheduledBase = maxOf(
                            System.currentTimeMillis(),
                            reel.nextTriggerAt
                        )
                        val next = NextTriggerCalculator.nextAfter(
                            reel = reel,
                            from = Instant.ofEpochMilli(scheduledBase).plusSeconds(1)
                        )
                        app.repository.updateNextTrigger(reel.id, next)
                        AlarmScheduler(context).schedule(
                            reel = reel.copy(nextTriggerAt = next),
                            triggerAt = next
                        )
                    }
                }
            } catch (throwable: Throwable) {
                Log.e(TAG, "Unable to handle reminder alarm", throwable)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "AlarmReceiver"
        const val ACTION_FIRE = "com.reelreminder.app.action.FIRE_REMINDER"
        const val EXTRA_REEL_ID = "extra_reel_id"
        const val EXTRA_IS_SNOOZE = "extra_is_snooze"
    }
}
