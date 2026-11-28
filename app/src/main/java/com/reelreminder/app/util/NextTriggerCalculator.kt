package com.reelreminder.app.util

import com.reelreminder.app.data.EndType
import com.reelreminder.app.data.Reel
import com.reelreminder.app.data.RepeatUnit
import com.reelreminder.app.data.ScheduleType
import com.reelreminder.app.data.daySet
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object NextTriggerCalculator {
    private const val ONE_YEAR_SECONDS = 365L * 24 * 60 * 60

    /**
     * Returns the next epoch millis for the supplied schedule, strictly after [now]
     * (except ONCE which returns the chosen date+time so UI can validate past dates).
     * Handles month-end clamping (31st -> 28/30), leap years (29 Feb -> 28 Feb),
     * Custom N units, Ends (Never / On date / After N times), and DST transitions via ZonedDateTime.
     */
    fun nextTriggerFor(
        type: ScheduleType,
        time: LocalTime,
        days: Set<Int>,
        onceDate: LocalDate?,
        now: Instant = Instant.now(),
        zone: ZoneId = ZoneId.systemDefault(),
        repeatInterval: Int = 1,
        repeatUnit: RepeatUnit = RepeatUnit.DAYS,
        endType: EndType = EndType.NEVER,
        endDate: LocalDate? = null,
        endAfterCount: Int? = null,
        firedCount: Int = 0
    ): Long? {
        val safeInterval = repeatInterval.coerceAtLeast(1)
        if (endType == EndType.AFTER_COUNT && endAfterCount != null && firedCount >= endAfterCount) {
            return null
        }

        val nowZoned = now.atZone(zone)
        val anchorDate = onceDate ?: nowZoned.toLocalDate()

        val candidateZoned: ZonedDateTime? = when (type) {
            ScheduleType.ONCE -> onceDate?.atTime(time)?.atZone(zone)

            ScheduleType.DAILY -> {
                var c = anchorDate.atTime(time).atZone(zone)
                while (!c.toInstant().isAfter(now)) {
                    c = c.plusDays(1)
                }
                c
            }

            ScheduleType.WEEKLY -> {
                if (days.isEmpty()) {
                    null
                } else {
                    var found: ZonedDateTime? = null
                    val startDate = if (anchorDate.isAfter(nowZoned.toLocalDate())) anchorDate else nowZoned.toLocalDate()
                    for (offset in 0..7) {
                        val d = startDate.plusDays(offset.toLong())
                        if (d.dayOfWeek.value in days) {
                            val c = d.atTime(time).atZone(zone)
                            if (c.toInstant().isAfter(now)) {
                                found = c
                                break
                            }
                        }
                    }
                    found
                }
            }

            ScheduleType.MONTHLY -> {
                var step = 0L
                var c = anchorDate.atTime(time).atZone(zone)
                while (!c.toInstant().isAfter(now)) {
                    step++
                    c = anchorDate.plusMonths(step).atTime(time).atZone(zone)
                }
                c
            }

            ScheduleType.YEARLY -> {
                var step = 0L
                var c = anchorDate.atTime(time).atZone(zone)
                while (!c.toInstant().isAfter(now)) {
                    step++
                    c = anchorDate.plusYears(step).atTime(time).atZone(zone)
                }
                c
            }

            ScheduleType.CUSTOM -> {
                var step = 0L
                var c = anchorDate.atTime(time).atZone(zone)
                while (!c.toInstant().isAfter(now)) {
                    step++
                    val nextDate = when (repeatUnit) {
                        RepeatUnit.DAYS -> anchorDate.plusDays(step * safeInterval)
                        RepeatUnit.WEEKS -> anchorDate.plusWeeks(step * safeInterval)
                        RepeatUnit.MONTHS -> anchorDate.plusMonths(step * safeInterval)
                        RepeatUnit.YEARS -> anchorDate.plusYears(step * safeInterval)
                    }
                    c = nextDate.atTime(time).atZone(zone)
                }
                c
            }
        }

        if (candidateZoned == null) return null
        if (type != ScheduleType.ONCE && endType == EndType.ON_DATE && endDate != null) {
            if (candidateZoned.toLocalDate().isAfter(endDate)) {
                return null
            }
        }
        return candidateZoned.toInstant().toEpochMilli()
    }

    fun nextAfter(
        reel: Reel,
        from: Instant = Instant.now(),
        zone: ZoneId = ZoneId.systemDefault()
    ): Long {
        val time = TimeUtils.parseTime(reel.time) ?: LocalTime.of(9, 0)
        val anchorDate = if (reel.nextTriggerAt > 0L) {
            Instant.ofEpochMilli(reel.nextTriggerAt).atZone(zone).toLocalDate()
        } else {
            from.atZone(zone).toLocalDate()
        }
        val endDate = reel.endDateIso?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

        return when (reel.scheduleType) {
            ScheduleType.ONCE -> {
                if (reel.nextTriggerAt > from.toEpochMilli()) {
                    reel.nextTriggerAt
                } else {
                    from.plusSeconds(5).toEpochMilli()
                }
            }

            else -> nextTriggerFor(
                type = reel.scheduleType,
                time = time,
                days = reel.daySet(),
                onceDate = anchorDate,
                now = from,
                zone = zone,
                repeatInterval = reel.repeatInterval,
                repeatUnit = reel.repeatUnit,
                endType = reel.endType,
                endDate = endDate,
                endAfterCount = reel.endAfterCount,
                firedCount = reel.firedCount
            ) ?: 0L
        }
    }

    /**
     * Builds the human-readable live summary string for Issue 10, e.g.:
     * "Starts Fri, 27 Nov 2026 at 8:00 pm, repeats every 5 years. Never ends."
     */
    fun formatSummary(
        startDate: LocalDate,
        time: LocalTime,
        type: ScheduleType,
        days: Set<Int>,
        repeatInterval: Int,
        repeatUnit: RepeatUnit,
        endType: EndType,
        endDate: LocalDate?,
        endAfterCount: Int?,
        locale: Locale = Locale.ENGLISH
    ): String {
        val dateFmt = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", locale)
        val timeFmt = DateTimeFormatter.ofPattern("h:mm a", locale)
        val dateText = startDate.format(dateFmt)
        val timeText = time.format(timeFmt).lowercase(locale)

        val repeatText = when (type) {
            ScheduleType.ONCE -> "one-time reminder"
            ScheduleType.DAILY -> "repeats daily"
            ScheduleType.WEEKLY -> {
                val names = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                val chosen = days.sorted().mapNotNull { names.getOrNull(it - 1) }.joinToString(", ")
                if (chosen.isBlank()) "repeats weekly" else "repeats weekly ($chosen)"
            }
            ScheduleType.MONTHLY -> "repeats monthly"
            ScheduleType.YEARLY -> "repeats yearly"
            ScheduleType.CUSTOM -> {
                val n = repeatInterval.coerceAtLeast(1)
                val unit = if (n == 1) repeatUnit.singular else repeatUnit.label.lowercase(locale)
                "repeats every $n $unit"
            }
        }

        val endsText = if (type == ScheduleType.ONCE) {
            ""
        } else {
            when (endType) {
                EndType.NEVER -> " Never ends."
                EndType.ON_DATE -> {
                    val d = endDate?.format(dateFmt) ?: "selected date"
                    " Ends on $d."
                }
                EndType.AFTER_COUNT -> {
                    val n = (endAfterCount ?: 1).coerceAtLeast(1)
                    " Ends after $n ${if (n == 1) "time" else "times"}."
                }
            }
        }

        return "Starts $dateText at $timeText, $repeatText.$endsText"
    }
}
