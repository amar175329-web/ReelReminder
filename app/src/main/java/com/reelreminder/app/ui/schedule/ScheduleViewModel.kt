package com.reelreminder.app.ui.schedule

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.reelreminder.app.ReelReminderApplication
import com.reelreminder.app.alarm.AlarmScheduler
import com.reelreminder.app.data.EndType
import com.reelreminder.app.data.Reel
import com.reelreminder.app.data.ReelRepository
import com.reelreminder.app.data.RepeatUnit
import com.reelreminder.app.data.ScheduleType
import com.reelreminder.app.data.daySet
import com.reelreminder.app.util.NextTriggerCalculator
import com.reelreminder.app.util.ShortcodeParser
import com.reelreminder.app.util.TimeUtils
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
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
    val categoryId: Long = 1L,
    val coverUri: String? = null,
    val type: ScheduleType = ScheduleType.ONCE,
    /** UTC midnight millis, matching Material 3's DatePicker contract. Always non-blank. */
    val dateUtcMillis: Long = TimeUtils.localDateToUtcMillis(LocalDate.now()),
    val hour: Int = LocalTime.now().plusHours(1).hour,
    val minute: Int = 0,
    val selectedDays: Set<Int> = emptySet(),
    val repeatInterval: Int = 1,
    val repeatUnit: RepeatUnit = RepeatUnit.WEEKS,
    val endType: EndType = EndType.NEVER,
    val endDateUtcMillis: Long = TimeUtils.localDateToUtcMillis(LocalDate.now().plusMonths(3)),
    val endAfterCount: Int = 5,
    val paused: Boolean = false,
    val createdAt: Long? = null,
    val urlError: UrlError? = null,
    val onceInPast: Boolean = false,
    val noDaysSelected: Boolean = false,
    val previewAt: Long? = null,
    val summaryText: String = "",
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

    fun start(editingId: Long?, sharedUrl: String?, defaultCategory: Long = 1L) {
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

                val time = TimeUtils.parseTime(reel.time) ?: defaultDateTime().toLocalTime()
                val dateUtcMillis = if (reel.nextTriggerAt > 0) {
                    TimeUtils.localDateToUtcMillis(TimeUtils.localDateFromEpoch(reel.nextTriggerAt))
                } else {
                    TimeUtils.localDateToUtcMillis(defaultDateTime().toLocalDate())
                }
                val endDateMillis = reel.endDateIso
                    ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                    ?.let { TimeUtils.localDateToUtcMillis(it) }
                    ?: TimeUtils.localDateToUtcMillis(LocalDate.now().plusMonths(3))

                _state.value = ScheduleUiState(
                    loading = false,
                    reelId = reel.id,
                    url = reel.url,
                    note = reel.note,
                    categoryId = reel.categoryId,
                    coverUri = reel.coverUri,
                    type = reel.scheduleType,
                    dateUtcMillis = dateUtcMillis,
                    hour = time.hour,
                    minute = time.minute,
                    selectedDays = reel.daySet(),
                    repeatInterval = reel.repeatInterval.coerceAtLeast(1),
                    repeatUnit = reel.repeatUnit,
                    endType = reel.endType,
                    endDateUtcMillis = endDateMillis,
                    endAfterCount = (reel.endAfterCount ?: 5).coerceAtLeast(1),
                    paused = reel.paused,
                    createdAt = reel.createdAt
                )
            } else {
                val default = defaultDateTime()
                _state.value = ScheduleUiState(
                    loading = false,
                    url = sharedUrl.orEmpty(),
                    categoryId = defaultCategory,
                    type = ScheduleType.ONCE,
                    dateUtcMillis = TimeUtils.localDateToUtcMillis(default.toLocalDate()),
                    hour = default.hour,
                    minute = default.minute,
                    selectedDays = setOf(default.toLocalDate().dayOfWeek.value),
                    repeatInterval = 1,
                    repeatUnit = RepeatUnit.WEEKS,
                    endType = EndType.NEVER,
                    endDateUtcMillis = TimeUtils.localDateToUtcMillis(default.toLocalDate().plusMonths(3)),
                    endAfterCount = 5
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

    fun updateCategory(categoryId: Long) {
        _state.update { it.copy(categoryId = categoryId) }
    }

    fun updateCoverUri(path: String?) {
        _state.update { it.copy(coverUri = path) }
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

    fun applyQuickInOneHour() {
        val target = LocalDateTime.now(ZoneId.systemDefault()).plusHours(1).truncatedTo(ChronoUnit.MINUTES)
        _state.update {
            it.copy(
                dateUtcMillis = TimeUtils.localDateToUtcMillis(target.toLocalDate()),
                hour = target.hour,
                minute = target.minute
            )
        }
        recomputePreview()
    }

    fun applyQuickTonight8Pm() {
        val now = LocalDateTime.now(ZoneId.systemDefault())
        val tonight = now.toLocalDate().atTime(20, 0)
        val target = if (tonight.isAfter(now)) tonight else tonight.plusDays(1)
        _state.update {
            it.copy(
                dateUtcMillis = TimeUtils.localDateToUtcMillis(target.toLocalDate()),
                hour = 20,
                minute = 0
            )
        }
        recomputePreview()
    }

    fun applyQuickTomorrow7Am() {
        val target = LocalDate.now(ZoneId.systemDefault()).plusDays(1)
        _state.update {
            it.copy(
                dateUtcMillis = TimeUtils.localDateToUtcMillis(target),
                hour = 7,
                minute = 0
            )
        }
        recomputePreview()
    }

    fun updateRepeatInterval(interval: Int) {
        _state.update { it.copy(repeatInterval = interval.coerceAtLeast(1)) }
        recomputePreview()
    }

    fun updateRepeatUnit(unit: RepeatUnit) {
        _state.update { it.copy(repeatUnit = unit) }
        recomputePreview()
    }

    fun applyCustomPreset(interval: Int, unit: RepeatUnit) {
        _state.update {
            it.copy(
                type = ScheduleType.CUSTOM,
                repeatInterval = interval.coerceAtLeast(1),
                repeatUnit = unit
            )
        }
        recomputePreview()
    }

    fun updateEndType(endType: EndType) {
        _state.update { it.copy(endType = endType) }
        recomputePreview()
    }

    fun updateEndDate(dateUtcMillis: Long) {
        _state.update { it.copy(endDateUtcMillis = dateUtcMillis) }
        recomputePreview()
    }

    fun updateEndAfterCount(count: Int) {
        _state.update { it.copy(endAfterCount = count.coerceAtLeast(1)) }
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
        val parsedReel = ShortcodeParser.parse(trimmedUrl)
        val urlError = when {
            trimmedUrl.isEmpty() -> UrlError.EMPTY
            parsedReel == null -> UrlError.INVALID
            else -> null
        }

        val zone = ZoneId.systemDefault()
        val now = Instant.now()
        val startDate = TimeUtils.utcMillisToLocalDate(current.dateUtcMillis)
        val endDate = if (current.endType == EndType.ON_DATE) {
            TimeUtils.utcMillisToLocalDate(current.endDateUtcMillis)
        } else {
            null
        }
        val time = LocalTime.of(current.hour, current.minute)
        val next = NextTriggerCalculator.nextTriggerFor(
            type = current.type,
            time = time,
            days = current.selectedDays,
            onceDate = startDate,
            now = now,
            zone = zone,
            repeatInterval = current.repeatInterval,
            repeatUnit = current.repeatUnit,
            endType = current.endType,
            endDate = endDate,
            endAfterCount = if (current.endType == EndType.AFTER_COUNT) current.endAfterCount else null
        )

        val onceInPast = current.type == ScheduleType.ONCE &&
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
                val canonicalUrl = parsedReel?.canonicalUrl ?: trimmedUrl
                val reel = Reel(
                    id = current.reelId ?: 0L,
                    url = canonicalUrl,
                    note = current.note.trim(),
                    categoryId = current.categoryId,
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
                    createdAt = current.createdAt ?: System.currentTimeMillis(),
                    repeatInterval = current.repeatInterval.coerceAtLeast(1),
                    repeatUnit = current.repeatUnit,
                    endType = current.endType,
                    endDateIso = endDate?.toString(),
                    endAfterCount = if (current.endType == EndType.AFTER_COUNT) current.endAfterCount else null,
                    coverUri = current.coverUri
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
        val startDate = TimeUtils.utcMillisToLocalDate(current.dateUtcMillis)
        val endDate = if (current.endType == EndType.ON_DATE) {
            TimeUtils.utcMillisToLocalDate(current.endDateUtcMillis)
        } else {
            null
        }
        val time = LocalTime.of(current.hour, current.minute)
        val preview = NextTriggerCalculator.nextTriggerFor(
            type = current.type,
            time = time,
            days = current.selectedDays,
            onceDate = startDate,
            now = now,
            zone = zone,
            repeatInterval = current.repeatInterval,
            repeatUnit = current.repeatUnit,
            endType = current.endType,
            endDate = endDate,
            endAfterCount = if (current.endType == EndType.AFTER_COUNT) current.endAfterCount else null
        )
        val summary = NextTriggerCalculator.formatSummary(
            startDate = startDate,
            time = time,
            type = current.type,
            days = current.selectedDays,
            repeatInterval = current.repeatInterval,
            repeatUnit = current.repeatUnit,
            endType = current.endType,
            endDate = endDate,
            endAfterCount = if (current.endType == EndType.AFTER_COUNT) current.endAfterCount else null
        )

        _state.update {
            it.copy(
                previewAt = preview,
                summaryText = summary,
                onceInPast = current.type == ScheduleType.ONCE &&
                    (preview == null || preview <= now.toEpochMilli()),
                noDaysSelected = current.type == ScheduleType.WEEKLY &&
                    current.selectedDays.isEmpty()
            )
        }
    }

    /** Issue 9: Default = next full hour, never blank. */
    private fun defaultDateTime(): LocalDateTime {
        return LocalDateTime.now(ZoneId.systemDefault())
            .plusHours(1)
            .truncatedTo(ChronoUnit.HOURS)
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
