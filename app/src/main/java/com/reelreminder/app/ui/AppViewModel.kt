package com.reelreminder.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.reelreminder.app.ReelReminderApplication
import com.reelreminder.app.alarm.AlarmScheduler
import com.reelreminder.app.data.Reel
import com.reelreminder.app.notifications.NotificationHelper
import com.reelreminder.app.util.NextTriggerCalculator
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as ReelReminderApplication
    private val repository = app.repository
    private val scheduler = AlarmScheduler(app)

    val reels: StateFlow<List<Reel>> = repository.reels.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    private val _pendingShareUrl = MutableStateFlow<String?>(null)
    val pendingShareUrl: StateFlow<String?> = _pendingShareUrl.asStateFlow()

    fun onSharedUrl(url: String) {
        _pendingShareUrl.value = url
    }

    fun consumeSharedUrl() {
        _pendingShareUrl.value = null
    }

    fun togglePause(reel: Reel) {
        val shouldPause = !reel.paused
        viewModelScope.launch {
            repository.setPaused(reel.id, shouldPause)
            if (shouldPause) {
                scheduler.cancel(reel.id)
            } else {
                val updated = repository.get(reel.id) ?: return@launch
                if (updated.done) return@launch
                val next = NextTriggerCalculator.nextAfter(
                    reel = updated,
                    from = Instant.now().plusSeconds(1)
                )
                repository.updateNextTrigger(updated.id, next)
                scheduler.schedule(updated.copy(nextTriggerAt = next), next)
            }
        }
    }

    fun delete(reel: Reel) {
        viewModelScope.launch {
            scheduler.cancel(reel.id)
            NotificationHelper.cancel(app, reel.id)
            repository.delete(reel)
        }
    }

    /**
     * Called after exact-alarm access is granted and on app start. It also
     * repairs alarms that were lost when the process was force-stopped.
     */
    fun rescheduleAll() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val from = Instant.ofEpochMilli(now).plusSeconds(1)
            repository.getActive().forEach { reel ->
                // Keep a still-valid future trigger; only recompute missed ones.
                // This avoids skipping a reminder when the app opens right at
                // its scheduled minute.
                val next = if (reel.nextTriggerAt > now) {
                    reel.nextTriggerAt
                } else {
                    NextTriggerCalculator.nextAfter(reel = reel, from = from)
                }
                if (next != reel.nextTriggerAt) {
                    repository.updateNextTrigger(reel.id, next)
                }
                scheduler.schedule(reel.copy(nextTriggerAt = next), next)
            }
        }
    }
}
