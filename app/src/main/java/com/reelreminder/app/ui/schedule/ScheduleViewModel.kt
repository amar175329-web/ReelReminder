package com.reelreminder.app.ui.schedule

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.reelreminder.app.ReelReminderApplication
import com.reelreminder.app.alarm.AlarmScheduler
import com.reelreminder.app.data.Reel
import com.reelreminder.app.data.ReelRepository
import com.reelreminder.app.data.ScheduleType
import com.reelreminder.app.data.daySet
import com.reelreminder.app.util.NextTriggerCalculator
import com.reelreminder.app.util.TimeUtils
import com.reelreminder.app.util.UrlExtractor
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class UrlError {
    EMPTY,
    INVALID
}

data class ScheduleUiState(
    val loading: Boolean = true,
    val reelId: Long? = null,
    val url: String = "",
    val note: String = "",
    val type: ScheduleType = ScheduleType.ONCE,
    /** UTC midnight millis, matching Material 3's DatePicker contract. */
    val dateUtcMillis: Long = TimeUtils.localDateToUtcMillis(LocalDate.now().plusDays(1)),
    val hour: Int = 20,
    val minute: Int = 0,
    val selectedDays: Set<Int> = emptySet(),
    val paused: Boolean = false,
    val createdAt: Long? = null,
    val urlError: UrlError? = null,
    val onceInPast: Boolean = false,
    val noDaysSelected: Boolean = false,
    val previewAt: Long? = null,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val needsExactAlarm: Boolean = false
)

class ScheduleViewModel(
    private val repository: ReelRepository,
    private val scheduler: AlarmScheduler
) : ViewModel() {

    private val _state = MutableStateFlow(ScheduleUiState())
    val state: StateFlow<ScheduleUiState> = _state.asStateFlow()

    private var loadJob: Job? = null
    private var saveJob: Job? = null

    fun start(editingId: Long?, sharedUrl: String?) {
        loadJob?.cancel()
        saveJob?.cancel()
        _state.value = ScheduleUiState(loading = true)

        loadJob = viewModelScope.launch {
            if (editingId != null) {
                val reel = repository.get(editingId)
                if (reel == null) {
                    _state.value = ScheduleUiState(loading = false)
                    return@launch
                }

                val time = TimeUtils.parseTime(reel.time) ?: LocalTime.of(20, 0)
                val dateUtcMillis =
                    if (reel.scheduleType == ScheduleType.ONCE && reel.nextTriggerAt > 0) {
                        TimeUtils.localDateToUtcMillis(
                            TimeUtils.localDateFromEpoch(reel.nextTriggerAt)
                        )
                    } else {
                        TimeUtils.localDateToUtcMillis(defaultDateTime().toLocalDate())
                    }

                _state.value = ScheduleUiState(
                    loading = false,
                    reelId = reel.id,
                    url = reel.url,
                    note = reel.note,
                    type = reel.scheduleType,
                    dateUtcMillis = dateUtcMillis,
                    hour = time.hour,
                    minute = time.minute,
                    selectedDays = reel.daySet(),
                    paused = reel.paused,
                    createdAt = reel.createdAt
                )
            } else {
                val default = defaultDateTime()
                _state.value = ScheduleUiState(
                    loading = false,
                    url = sharedUrl.orEmpty(),
                    type = ScheduleType.ONCE,
                    dateUtcMillis = TimeUtils.localDateToUtcMillis(default.toLocalDate()),
                    hour = default.hour,
                    minute = default.minute,
                    selectedDays = setOf(LocalDate.now().dayOfWeek.value)
                )
            }
            recomputePreview()
        }
    }

    fun updateUrl(value: String) {
        _state.update { it.copy(url = value, urlError = null) }
    }

    fun updateNote(value: String) {
        _state.update { it.copy(note = value) }
    }

    fun updateType(type: ScheduleType) {
        _state.update { current ->
            current.copy(
                type = type,
                noDaysSelected = false,
                selectedDays = if (type == ScheduleType.WEEKLY && current.selectedDays.isEmpty()) {
                    setOf(LocalDate.now().dayOfWeek.value)
                } else {
                    current.selectedDays
                }
            )
        }
        recomputePreview()
    }

    fun updateDate(dateUtcMillis: Long) {
        _state.update { it.copy(dateUtcMillis = dateUtcMillis) }
        recomputePreview()
    }

    fun updateTime(hour: Int, minute: Int) {
        _state.update { it.copy(hour = hour, minute = minute) }
        recomputePreview()
    }

    fun toggleDay(day: Int) {
        _state.update { current ->
            val days = if (day in current.selectedDays) {
                current.selectedDays - day
            } else {
                current.selectedDays + day
            }
            current.copy(selectedDays = days, noDaysSelected = false)
        }
        recomputePreview()
    }

    fun save() {
        val current = _state.value
        if (current.loading || current.isSaving) return

        val trimmedUrl = current.url.trim()
        val urlError = when {
            trimmedUrl.isEmpty() -> UrlError.EMPTY
            !UrlExtractor.isValidHttpUrl(trimmedUrl) -> UrlError.INVALID
            else -> null
        }

        val zone = ZoneId.systemDefault()
        val now = Instant.now()
        val time = LocalTime.of(current.hour, current.minute)
        val next = when (current.type) {
            ScheduleType.ONCE -> NextTriggerCalculator.nextTriggerFor(
                type = ScheduleType.ONCE,
                time = time,
                days = emptySet(),
                onceDate = TimeUtils.utcMillisToLocalDate(current.dateUtcMillis),
                now = now,
                zone = zone
            )

            ScheduleType.DAILY -> NextTriggerCalculator.nextTriggerFor(
                type = ScheduleType.DAILY,
                time = time,
                days = emptySet(),
                onceDate = null,
                now = now,
                zone = zone
            )

            ScheduleType.WEEKLY -> if (current.selectedDays.isEmpty()) {
                null
            } else {
                NextTriggerCalculator.nextTriggerFor(
                    type = ScheduleType.WEEKLY,
                    time = time,
                    days = current.selectedDays,
                    onceDate = null,
                    now = now,
                    zone = zone
                )
            }
        }

        val onceInPast =
            current.type == ScheduleType.ONCE &&
                (next == null || next <= now.toEpochMilli())
        val noDays = current.type == ScheduleType.WEEKLY && current.selectedDays.isEmpty()

        if (urlError != null || onceInPast || noDays || next == null) {
            _state.update {
                it.copy(
                    urlError = urlError,
                    onceInPast = onceInPast,
                    noDaysSelected = noDays
                )
            }
            return
        }

        _state.update {
            it.copy(
                isSaving = true,
                urlError = null,
                onceInPast = false,
                noDaysSelected = false
            )
        }

        saveJob = viewModelScope.launch {
            try {
                val reel = Reel(
                    id = current.reelId ?: 0L,
                    url = trimmedUrl,
                    note = current.note.trim(),
                    scheduleType = current.type,
                    time = TimeUtils.formatTimeValue(current.hour, current.minute),
                    daysOfWeek = if (current.type == ScheduleType.WEEKLY) {
                        current.selectedDays.sorted().joinToString(",")
                    } else {
                        ""
                    },
                    nextTriggerAt = next,
                    done = false,
                    paused = false,
                    createdAt = current.createdAt ?: System.currentTimeMillis()
                )

                val savedId = if (current.reelId == null) {
                    repository.insert(reel)
                } else {
                    repository.update(reel)
                    current.reelId
                }

                val saved = repository.get(savedId)
                if (saved == null) {
                    _state.update { it.copy(isSaving = false) }
                    return@launch
                }

                scheduler.cancel(saved.id)
                if (!saved.done && !saved.paused) {
                    scheduler.schedule(saved, saved.nextTriggerAt)
                }

                _state.update {
                    it.copy(
                        isSaving = false,
                        saved = true,
                        needsExactAlarm = !scheduler.canScheduleExact()
                    )
                }
            } catch (throwable: Throwable) {
                Log.e(TAG, "Unable to save reminder", throwable)
                _state.update { it.copy(isSaving = false) }
            }
        }
    }

    fun consumeSaved() {
        _state.update { it.copy(saved = false) }
    }

    private fun recomputePreview() {
        val current = _state.value
        if (current.loading) return

        val zone = ZoneId.systemDefault()
        val now = Instant.now()
        val time = LocalTime.of(current.hour, current.minute)
        val preview = when (current.type) {
            ScheduleType.ONCE -> NextTriggerCalculator.nextTriggerFor(
                type = ScheduleType.ONCE,
                time = time,
                days = emptySet(),
                onceDate = TimeUtils.utcMillisToLocalDate(current.dateUtcMillis),
                now = now,
                zone = zone
            )

            ScheduleType.DAILY -> NextTriggerCalculator.nextTriggerFor(
                type = ScheduleType.DAILY,
                time = time,
                days = emptySet(),
                onceDate = null,
                now = now,
                zone = zone
            )

            ScheduleType.WEEKLY -> if (current.selectedDays.isEmpty()) {
                null
            } else {
                NextTriggerCalculator.nextTriggerFor(
                    type = ScheduleType.WEEKLY,
                    time = time,
                    days = current.selectedDays,
                    onceDate = null,
                    now = now,
                    zone = zone
                )
            }
        }

        _state.update {
            it.copy(
                previewAt = preview,
                onceInPast = current.type == ScheduleType.ONCE &&
                    (preview == null || preview <= now.toEpochMilli()),
                noDaysSelected = current.type == ScheduleType.WEEKLY &&
                    current.selectedDays.isEmpty()
            )
        }
    }

    private fun defaultDateTime(): LocalDateTime {
        val now = LocalDateTime.now(ZoneId.systemDefault())
        val todayAtEight = now.toLocalDate().atTime(20, 0)
        return if (now.isBefore(todayAtEight)) todayAtEight else todayAtEight.plusDays(1)
    }

    class Factory(
        private val app: ReelReminderApplication
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ScheduleViewModel(
                repository = app.repository,
                scheduler = AlarmScheduler(app)
            ) as T
        }
    }

    companion object {
        private const val TAG = "ScheduleViewModel"
    }
}
