package com.reelreminder.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.reelreminder.app.ReelReminderApplication
import com.reelreminder.app.util.NextTriggerCalculator
import java.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Rebuilds every active alarm after a reboot, app update, manual clock change,
 * or timezone change. AlarmManager alarms do not survive these events.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in supportedActions) return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as ReelReminderApplication
                val scheduler = AlarmScheduler(context)
                val now = Instant.now()
                val from = now.plusSeconds(1)
                val recomputeAll = intent.action == Intent.ACTION_TIME_CHANGED ||
                    intent.action == Intent.ACTION_TIMEZONE_CHANGED

                app.repository.getActive().forEach { reel ->
                    val next = if (!recomputeAll && reel.nextTriggerAt > now.toEpochMilli()) {
                        // The stored absolute trigger is still in the future.
                        reel.nextTriggerAt
                    } else {
                        NextTriggerCalculator.nextAfter(reel = reel, from = from)
                    }
                    if (next != reel.nextTriggerAt) {
                        app.repository.updateNextTrigger(reel.id, next)
                    }
                    scheduler.schedule(reel.copy(nextTriggerAt = next), next)
                }
            } catch (throwable: Throwable) {
                Log.e(TAG, "Unable to restore alarms", throwable)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
        private val supportedActions = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED
        )
    }
}
