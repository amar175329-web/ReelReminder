package com.reelreminder.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.reelreminder.app.R
import com.reelreminder.app.ReelReminderApplication
import com.reelreminder.app.notifications.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != ACTION_SNOOZE && action != ACTION_DONE) return

        val reelId = intent.getLongExtra(EXTRA_REEL_ID, -1L)
        if (reelId <= 0L) return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as ReelReminderApplication
                val scheduler = AlarmScheduler(context)

                when (action) {
                    ACTION_SNOOZE -> {
                        val reel = app.repository.get(reelId) ?: return@launch
                        if (reel.paused) return@launch
                        scheduler.scheduleSnooze(
                            reelId = reel.id,
                            triggerAt = System.currentTimeMillis() + SNOOZE_MILLIS
                        )
                        NotificationHelper.cancel(context, reel.id)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                R.string.snoozed_confirmation,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    ACTION_DONE -> {
                        app.repository.markDone(reelId)
                        scheduler.cancel(reelId)
                        NotificationHelper.cancel(context, reelId)
                    }
                }
            } catch (throwable: Throwable) {
                Log.e(TAG, "Unable to handle notification action", throwable)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "NotificationAction"
        private const val SNOOZE_MILLIS = 10 * 60 * 1000L

        const val ACTION_SNOOZE = "com.reelreminder.app.action.SNOOZE"
        const val ACTION_DONE = "com.reelreminder.app.action.DONE"
        const val EXTRA_REEL_ID = "extra_reel_id"
    }
}
