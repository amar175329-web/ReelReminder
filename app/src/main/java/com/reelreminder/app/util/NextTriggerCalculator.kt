package com.reelreminder.app.util

import com.reelreminder.app.data.Reel
import com.reelreminder.app.data.ScheduleType
import com.reelreminder.app.data.daySet
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

object NextTriggerCalculator {
    private const val ONE_YEAR_SECONDS = 365L * 24 * 60 * 60

    /**
     * Returns the next epoch millis for the supplied schedule, strictly after [now].
     * Once schedules simply return the chosen date + time (which the UI validates).
     */
    fun nextTriggerFor(
        type: ScheduleType,
        time: LocalTime,
        days: Set<Int>,
        onceDate: LocalDate?,
        now: Instant = Instant.now(),
        zone: ZoneId = ZoneId.systemDefault()
    ): Long? {
        val nowZoned = now.atZone(zone)
        return when (type) {
            ScheduleType.ONCE -> onceDate
                ?.atTime(time)
                ?.atZone(zone)
                ?.toInstant()
                ?.toEpochMilli()

            ScheduleType.DAILY -> {
                var candidate = nowZoned.toLocalDate().atTime(time).atZone(zone)
                if (!candidate.toInstant().isAfter(now)) {
                    candidate = candidate.plusDays(1)
                }
                candidate.toInstant().toEpochMilli()
            }

            ScheduleType.WEEKLY -> {
                if (days.isEmpty()) return null
                for (offset in 0..7) {
                    val date = nowZoned.toLocalDate().plusDays(offset.toLong())
                    if (date.dayOfWeek.value in days) {
                        val candidate = date.atTime(time).atZone(zone)
                        if (candidate.toInstant().isAfter(now)) {
                            return candidate.toInstant().toEpochMilli()
                        }
                    }
                }
                null
            }
        }
    }

    /**
     * Recomputes the next trigger for an existing reminder. Used after a fire,
     * after reboot, and when a paused reminder is resumed.
     *
     * For a one-time reminder whose moment has already passed we schedule a few
     * seconds from now so a missed reminder is not silently lost.
     */
    fun nextAfter(
        reel: Reel,
        from: Instant = Instant.now(),
        zone: ZoneId = ZoneId.systemDefault()
    ): Long {
        val time = TimeUtils.parseTime(reel.time) ?: LocalTime.of(9, 0)
        return when (reel.scheduleType) {
            ScheduleType.ONCE -> {
                if (reel.nextTriggerAt > from.toEpochMilli()) {
                    reel.nextTriggerAt
                } else {
                    from.plusSeconds(5).toEpochMilli()
                }
            }

            ScheduleType.DAILY -> nextTriggerFor(
                type = ScheduleType.DAILY,
                time = time,
                days = emptySet(),
                onceDate = null,
                now = from,
                zone = zone
            ) ?: from.plusSeconds(ONE_YEAR_SECONDS).toEpochMilli()

            ScheduleType.WEEKLY -> {
                val days = reel.daySet()
                if (days.isEmpty()) {
                    // Invalid legacy data: park the reminder instead of
                    // re-firing it every minute.
                    from.plusSeconds(ONE_YEAR_SECONDS).toEpochMilli()
                } else {
                    nextTriggerFor(
                        type = ScheduleType.WEEKLY,
                        time = time,
                        days = days,
                        onceDate = null,
                        now = from,
                        zone = zone
                    ) ?: from.plusSeconds(ONE_YEAR_SECONDS).toEpochMilli()
                }
            }
        }
    }
}
